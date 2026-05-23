package de.wagenknecht.backloggd.worker;

import static de.wagenknecht.backloggd.ApiConstants.NOTIFICATION_URL;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.wagenknecht.backloggd.ApiConstants;
import de.wagenknecht.backloggd.MainActivity;
import de.wagenknecht.backloggd.R;

public class NotificationCheckWorker extends Worker {

    private static final String TAG = "NotificationCheckWorker";
    private static final String CHANNEL_ID = "BACKLOGGD_NOTIFICATIONS";

    public NotificationCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker started. Checking for notifications.");

        String cookies = CookieManager.getInstance().getCookie(NOTIFICATION_URL);
        if (cookies == null || cookies.isEmpty()) {
            Log.w(TAG, "Could not get cookies. User is probably not logged in. Aborting.");
            return Result.success();
        }

        try {
            Document doc = Jsoup.connect(NOTIFICATION_URL)
                    .header("Cookie", cookies)
                    .get();

            Elements unreadNotifications = doc.select(".notification.unread");

            if (!unreadNotifications.isEmpty()) {
                Log.i(TAG, unreadNotifications.size() + " unread notifications found!");
                for (Element notification : unreadNotifications) {
                    String notificationText = notification.select(".notification-body p").text();
                    String imageUrl = notification.select(".avatar img").attr("src");
                    String iconClass = notification.select(".notification-icon i").attr("class");
                    String title;
                    if (iconClass.contains("fa-star")) {
                        title = "New Badge";
                    } else if (iconClass.contains("fa-user-friends")) {
                        title = "New Follower";
                    } else if (iconClass.contains("fa-heart")) {
                        title = "New Like";
                    } else {
                        title = "Backloggd";
                    }

                    Bitmap image = null;
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        image = getBitmapFromUrl(imageUrl);
                    }

                    if (!notificationText.isEmpty()) {
                        showPushNotification(title, notificationText, notificationText.hashCode(), image);
                    }
                }
            } else {
                Log.d(TAG, "No unread notifications.");
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch or parse notifications page.", e);
            return Result.failure();
        }
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading notification image", e);
            return null;
        }
    }

    private void showPushNotification(String title, String contentText, int notificationId, Bitmap image) {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("urlToLoad", NOTIFICATION_URL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (image != null) {
            builder.setLargeIcon(image);
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
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
            CharSequence name = "Backloggd Notifications";
            String description = "Channel for new backloggd.com notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}