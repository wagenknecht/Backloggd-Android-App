package de.wagenknecht.backloggd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import de.wagenknecht.backloggd.worker.NotificationCheckWorker;
import de.wagenknecht.backloggd.worker.WishlistCheckerWorker;

public class MainActivity extends AppCompatActivity {

    private WebView myWeb;
    private LinearLayout errorLayout;
    private ImageButton settingsButton;
    private boolean receivedError = false;
    private static final String TAG = "MainActivity";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/wagenknecht/Backloggd-Android-App/tags";

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted.");
                } else {
                    Log.w(TAG, "Notification permission denied.");
                }
            });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        errorLayout = findViewById(R.id.errorLayout);
        settingsButton = findViewById(R.id.settingsButton);
        Button retryButton = findViewById(R.id.retryButton);

        myWeb.getSettings().setJavaScriptEnabled(true);
        myWeb.getSettings().setDomStorageEnabled(true);

        retryButton.setOnClickListener(v -> {
            myWeb.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            myWeb.reload();
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        myWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                receivedError = false;
                errorLayout.setVisibility(View.GONE);
                myWeb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    receivedError = true;
                    myWeb.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!receivedError) {
                    myWeb.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                }
                updateUiForUrl(url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                updateUiForUrl(url);
            }
        });

        myWeb.loadUrl("https://backloggd.com/");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (myWeb.canGoBack()) {
                    myWeb.goBack();
                }
            }
        });

        checkForUpdates();
        askNotificationPermission();
        startNotificationWorker();
        WishlistCheckerWorker.scheduleNextWorker(this);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String urlToLoad = intent.getStringExtra("urlToLoad");
        if (urlToLoad != null) {
            Log.d(TAG, "Intent received with URL: " + urlToLoad);
            myWeb.loadUrl(urlToLoad);
        }
    }

    private void startNotificationWorker() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long interval = Long.parseLong(prefs.getString("notification_interval", "15"));

        if (interval == -1) {
            WorkManager.getInstance(this).cancelUniqueWork("NotificationCheck");
            Log.d(TAG, "Notification worker cancelled by user setting.");
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationCheckWorker.class, interval, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NotificationCheck",
                ExistingPeriodicWorkPolicy.UPDATE,
                notificationWorkRequest);

        Log.d(TAG, "Notification worker scheduled for every " + interval + " minutes.");
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission.");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void updateUiForUrl(String url) {
        Log.d(TAG, "Current URL: " + url);
        if ("https://backloggd.com/settings/".equals(url)) {
            settingsButton.setVisibility(View.VISIBLE);
        } else {
            settingsButton.setVisibility(View.GONE);
        }
    }

    private void checkForUpdates() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, GITHUB_API_URL, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject latestTag = response.getJSONObject(0);
                            String latestVersion = latestTag.getString("name");
                            String currentVersion = getCurrentVersionName(this);

                            Log.d(TAG, "Latest version on GitHub: " + latestVersion);
                            Log.d(TAG, "Current app version: " + currentVersion);

                            if (currentVersion != null && !latestVersion.equals(currentVersion)) {
                                showUpdateDialog(latestVersion);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON for update check", e);
                    }
                },
                error -> Log.e(TAG, "Error checking for updates", error)
        );
        queue.add(request);
    }

    private void showUpdateDialog(String newVersion) {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("A new version (" + newVersion + ") is available. Would you like to download it?")
                .setPositiveButton("Download", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wagenknecht/Backloggd-Android-App/releases/latest"));
                    startActivity(browserIntent);
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private String getCurrentVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get package name", e);
            return null;
        }
    }
}