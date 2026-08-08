package de.wagenknecht.backloggd.util;

import static de.wagenknecht.backloggd.ApiConstants.GITHUB_LATEST_RELEASE_API_URL;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Compares the installed version against the latest GitHub release. Every failure path stays
 * silent on purpose: a failed update check should never bother the user.
 */
public final class UpdateChecker {

    private static final String TAG = "UpdateChecker";
    private static final int TIMEOUT_MS = 15000;

    public interface Callback {
        /**
         * @param version tag name of the newer release
         * @param apkUrl  direct link to that release's APK, or null when it has none
         */
        @MainThread
        void onUpdateAvailable(@NonNull String version, @Nullable String apkUrl);
    }

    private UpdateChecker() {}

    /**
     * Looks for a newer release in the background. The callback runs on the main thread and only
     * fires when an update actually exists.
     */
    public static void checkAsync(@NonNull Context context, @NonNull Callback callback) {
        String installedVersion = getInstalledVersion(context);
        if (installedVersion == null) {
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            JSONObject release = fetchLatestRelease();
            if (release == null) {
                return;
            }

            String latestVersion = release.optString("tag_name", "");
            if (latestVersion.isEmpty()) {
                Log.w(TAG, "Latest release has no tag_name, skipping update check.");
                return;
            }

            Log.d(TAG, "Latest release on GitHub: " + latestVersion);
            Log.d(TAG, "Installed app version: " + installedVersion);

            if (!isNewerVersion(latestVersion, installedVersion)) {
                return;
            }

            String apkUrl = findApkAssetUrl(release);
            mainHandler.post(() -> callback.onUpdateAvailable(latestVersion, apkUrl));
        }).start();
    }

    @Nullable
    private static String getInstalledVersion(@NonNull Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not read the installed version.", e);
            return null;
        }
    }

    /** Fetches the latest GitHub release as JSON, or null if that fails for any reason. */
    @WorkerThread
    @Nullable
    private static JSONObject fetchLatestRelease() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(GITHUB_LATEST_RELEASE_API_URL).openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            // The GitHub API rejects requests without a User-Agent.
            connection.setRequestProperty("User-Agent", "Backloggd-Android-App");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Update check returned status " + status);
                return null;
            }

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            return new JSONObject(body.toString());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error checking for updates", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Direct download URL of the release's APK asset, or null when it has none. */
    @Nullable
    private static String findApkAssetUrl(@NonNull JSONObject release) {
        JSONArray assets = release.optJSONArray("assets");
        if (assets == null) {
            return null;
        }
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null || !asset.optString("name", "").endsWith(".apk")) {
                continue;
            }
            String downloadUrl = asset.optString("browser_download_url", "");
            if (!downloadUrl.isEmpty()) {
                return downloadUrl;
            }
        }
        return null;
    }

    /**
     * Compares dot-separated version numbers, tolerating an optional "v" prefix and a differing
     * number of segments ("2.1" counts as newer than "2.0.3"). Only a strictly newer remote
     * version returns true, so locally built versions ahead of the last release stay quiet.
     */
    @VisibleForTesting
    static boolean isNewerVersion(@NonNull String remote, @NonNull String local) {
        String[] remoteParts = stripVersionPrefix(remote).split("\\.");
        String[] localParts = stripVersionPrefix(local).split("\\.");
        int segments = Math.max(remoteParts.length, localParts.length);
        for (int i = 0; i < segments; i++) {
            int remotePart = versionPart(remoteParts, i);
            int localPart = versionPart(localParts, i);
            if (remotePart != localPart) {
                return remotePart > localPart;
            }
        }
        return false;
    }

    private static String stripVersionPrefix(String version) {
        String trimmed = version.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    /** Leading digits of the given segment, or 0 for a missing or non-numeric one. */
    private static int versionPart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        String part = parts[index].trim();
        int end = 0;
        while (end < part.length() && Character.isDigit(part.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(part.substring(0, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
