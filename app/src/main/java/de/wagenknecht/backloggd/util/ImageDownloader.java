package de.wagenknecht.backloggd.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ImageDownloader {

    private static final String TAG = "ImageDownloader";
    private static final int TARGET_BITMAP_SIZE_PX = 512;
    private static final int MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024;

    private ImageDownloader() {}

    @Nullable
    public static Bitmap downloadDownsampled(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            byte[] data;
            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[8192];
                int total = 0;
                int n;
                while ((n = input.read(chunk)) > 0) {
                    total += n;
                    if (total > MAX_DOWNLOAD_BYTES) {
                        Log.w(TAG, "Image exceeds " + MAX_DOWNLOAD_BYTES + " bytes, aborting: " + imageUrl);
                        return null;
                    }
                    buffer.write(chunk, 0, n);
                }
                data = buffer.toByteArray();
            }

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds, TARGET_BITMAP_SIZE_PX);
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image: " + imageUrl, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqSize) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while (height / (inSampleSize * 2) >= reqSize && width / (inSampleSize * 2) >= reqSize) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }
}
