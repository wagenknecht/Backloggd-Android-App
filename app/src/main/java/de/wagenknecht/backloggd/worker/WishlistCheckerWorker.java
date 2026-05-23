package de.wagenknecht.backloggd.worker;

import static de.wagenknecht.backloggd.ApiConstants.BACKLOGGD_URL;
import static de.wagenknecht.backloggd.ApiConstants.SETTINGS_URL;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;

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

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.wagenknecht.backloggd.MainActivity;
import de.wagenknecht.backloggd.R;
import de.wagenknecht.backloggd.util.BackloggdRequest;
import de.wagenknecht.backloggd.util.ImageDownloader;

public class WishlistCheckerWorker extends Worker {

    private static final String TAG = "WishlistCheckerWorker";
    private static final String CHANNEL_ID = "BACKLOGGD_WISHLIST_RELEASE";
    public static final String WORK_NAME = "WishlistChecker";

    public WishlistCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker started. Checking wishlist for games releasing today.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = prefs.getString("backloggd_username", null);

        String cookies = CookieManager.getInstance().getCookie(BACKLOGGD_URL);

        if (username == null || username.isEmpty()) {
            Log.d(TAG, "Username not found, trying to fetch from settings page.");

            if (cookies == null || cookies.isEmpty()) {
                Log.w(TAG, "Could not get cookies. User is probably not logged in. Aborting.");
                return Result.success();
            }

            try {
                Connection.Response response = BackloggdRequest
                        .forUrl(getApplicationContext(), SETTINGS_URL, cookies)
                        .execute();
                if (response.statusCode() == 404) {
                    Log.w(TAG, "User not logged in, settings page returned 404. Retrying in 30 minutes.");
                    scheduleRetry(getApplicationContext(), 30, TimeUnit.MINUTES);
                    return Result.failure();
                }
                Document settingsDoc = response.parse();
                // Find the input field with id 'user_username' and extract its 'value' attribute.
                Element usernameInput = settingsDoc.selectFirst("input#user_username");
                if (usernameInput != null) {
                    username = usernameInput.val();
                    prefs.edit().putString("backloggd_username", username).apply();
                    Log.d(TAG, "Found and saved username: " + username);
                } else {
                    Log.w(TAG, "Could not find username on settings page. Retrying in 30 minutes.");
                    scheduleRetry(getApplicationContext(), 30, TimeUnit.MINUTES);
                    return Result.failure();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to Backloggd settings page.", e);
                return Result.retry();
            }
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String wishlistUrl = BACKLOGGD_URL + "/u/" + username + "/wishlist/release/type:wishlist;release_year:" + currentYear;

        try {
            Connection.Response wishlistResponse = BackloggdRequest
                    .forUrl(getApplicationContext(), wishlistUrl, cookies)
                    .execute();
            int statusCode = wishlistResponse.statusCode();
            if (statusCode != 200) {
                Log.w(TAG, "Wishlist request returned status " + statusCode + ". Body excerpt: "
                        + wishlistResponse.body().substring(0, Math.min(500, wishlistResponse.body().length())));
                return Result.retry();
            }
            Document doc = wishlistResponse.parse();
            Elements gameElements = doc.select("#user-games-library-container .rating-hover");

            if (gameElements.isEmpty()) {
                Log.d(TAG, "No games found on the wishlist page for the current year.");
            } else {
                for (Element gameElement : gameElements) {
                    String gameTitle = gameElement.select(".game-text-centered").text();
                    String releaseDateStr = gameElement.select(".release-below p").text();
                    String imageUrl = gameElement.select("img.card-img").attr("src");

                    if (gameTitle.isEmpty() || releaseDateStr.isEmpty()) {
                        continue;
                    }

                    if (isReleasedToday(releaseDateStr)) {
                        SharedPreferences releasePrefs = getApplicationContext().getSharedPreferences("wishlist_release_dates", Context.MODE_PRIVATE);
                        String storedDate = releasePrefs.getString(gameTitle, "");

                        if (!releaseDateStr.equals(storedDate)) {
                            Log.d(TAG, "Found game releasing today: " + gameTitle);
                            Bitmap gameCoverBitmap = ImageDownloader.downloadDownsampled(imageUrl);
                            showPushNotification("Releasing Today!", gameTitle + " is out now!", gameTitle, gameCoverBitmap);
                            releasePrefs.edit().putString(gameTitle, releaseDateStr).apply();
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

    private boolean isReleasedToday(String releaseDateStr) {
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
                return false;
            }
        }

        Calendar today = Calendar.getInstance();
        Calendar releaseCal = Calendar.getInstance();
        releaseCal.setTime(releaseDate);

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

    private static void scheduleRetry(Context context, long delay, TimeUnit unit) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WishlistCheckerWorker.class)
                .setInitialDelay(delay, unit)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE, // Replace any existing retry
                workRequest);

        Log.d(TAG, "Wishlist checker worker retry scheduled in " + delay + " " + unit.toString().toLowerCase());
    }
}
