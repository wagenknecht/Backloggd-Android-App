package de.wagenknecht.backloggd.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.wagenknecht.backloggd.MainActivity;
import de.wagenknecht.backloggd.R;

public class WishlistCheckerWorker extends Worker {

    private static final String TAG = "WishlistCheckerWorker";
    private static final String CHANNEL_ID = "BACKLOGGD_WISHLIST_RELEASE";
    public static final String WORK_NAME = "WishlistChecker";
    //todo username and year are hardcode
    private static final String WISHLIST_URL = "https://backloggd.com/u/Tysk/wishlist/release/type:wishlist;release_year:2025";

    public WishlistCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker started. Checking wishlist for games releasing today.");

        try {
            Document doc = Jsoup.connect(WISHLIST_URL).get();
            Elements gameElements = doc.select("#user-games-library-container .rating-hover");

            if (gameElements.isEmpty()) {
                Log.d(TAG, "No games found on the wishlist page.");
            } else {
                for (Element gameElement : gameElements) {
                    String gameTitle = gameElement.select(".game-text-centered").text();
                    String releaseDateStr = gameElement.select(".release-below p").text();
                    String imageUrl = gameElement.select("img.card-img").attr("src");

                    if (gameTitle.isEmpty() || releaseDateStr.isEmpty()) {
                        continue;
                    }

                    if (isReleasedToday(releaseDateStr)) {
                        SharedPreferences prefs = getApplicationContext().getSharedPreferences("wishlist_release_dates", Context.MODE_PRIVATE);
                        String storedDate = prefs.getString(gameTitle, "");

                        if (!releaseDateStr.equals(storedDate)) {
                            Log.d(TAG, "Found game releasing today: " + gameTitle);
                            Bitmap gameCoverBitmap = getBitmapFromUrl(imageUrl);
                            showPushNotification("Releasing Today!", gameTitle + " is out now!", gameTitle, gameCoverBitmap);
                            prefs.edit().putString(gameTitle, releaseDateStr).apply();
                        } else {
                            Log.d(TAG, "Already notified for today's release of " + gameTitle);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to Backloggd and parse wishlist.", e);
            return Result.retry();
        }

        scheduleNextWorker(getApplicationContext());
        return Result.success();
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image for notification", e);
            return null;
        }
    }

    private boolean isReleasedToday(String releaseDateStr) {
        // Handle multiple date formats from the website
        SimpleDateFormat formatWithComma = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat formatWithoutComma = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        Date releaseDate;

        try {
            releaseDate = formatWithComma.parse(releaseDateStr);
        } catch (ParseException e) {
            try {
                releaseDate = formatWithoutComma.parse(releaseDateStr);
            } catch (ParseException e2) {
                Log.w(TAG, "Could not parse date: " + releaseDateStr);
                return false; // Skip if date is in an unknown format
            }
        }

        Calendar today = Calendar.getInstance();
        Calendar releaseCal = Calendar.getInstance();
        releaseCal.setTime(releaseDate);
        Log.d(TAG, today.get(Calendar.YEAR) + "-" + today.get(Calendar.MONTH) + "-" + today.get(Calendar.DAY_OF_MONTH));
        Log.d(TAG, releaseCal.get(Calendar.YEAR) + "-" + releaseCal.get(Calendar.MONTH) + "-" + releaseCal.get(Calendar.DAY_OF_MONTH));
        return today.get(Calendar.YEAR) == releaseCal.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == releaseCal.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == releaseCal.get(Calendar.DAY_OF_MONTH);
    }

    private void showPushNotification(String title, String contentText, String gameTitle, Bitmap gameCover) {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        int notificationId = gameTitle.hashCode();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (gameCover != null) {
            builder.setLargeIcon(gameCover);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot send notification. Permission not granted.");
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Wishlist Game Releases";
            String description = "Notifications for when games on your wishlist get a release date.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void scheduleNextWorker(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int hour = prefs.getInt("daily_notification_hour", 9);
        int minute = prefs.getInt("daily_notification_minute", 0);

        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, hour);
        nextRun.set(Calendar.MINUTE, minute);
        nextRun.set(Calendar.SECOND, 0);

        if (nextRun.before(Calendar.getInstance())) {
            nextRun.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = nextRun.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WishlistCheckerWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest);

        Log.d(TAG, "Wishlist checker worker scheduled to run at " + nextRun.getTime());
    }
}
