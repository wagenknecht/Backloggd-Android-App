package de.wagenknecht.backloggd;

import static de.wagenknecht.backloggd.ApiConstants.GITHUB_RELEASES_LATEST;
import static de.wagenknecht.backloggd.ApiConstants.GITHUB_REPO_URL;
import static de.wagenknecht.backloggd.ApiConstants.GITHUB_TAGS_API_URL;
import static de.wagenknecht.backloggd.ApiConstants.BACKLOGGD_URL;
import static de.wagenknecht.backloggd.ApiConstants.LOGIN_URL;
import static de.wagenknecht.backloggd.ApiConstants.LOGOUT_URL;
import static de.wagenknecht.backloggd.ApiConstants.NOTIFICATION_URL;

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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import de.wagenknecht.backloggd.util.UsernameHelper;
import de.wagenknecht.backloggd.worker.NotificationCheckWorker;
import de.wagenknecht.backloggd.worker.WishlistCheckerWorker;

public class MainActivity extends AppCompatActivity {
    private WebView myWeb;
    private LinearLayout errorLayout;
    private Button settingsButton;
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView drawerNav;
    private LinearProgressIndicator pageProgress;
    private boolean receivedError = false;
    private static final String TAG = "MainActivity";

    private static final String LOG_GAME_JS =
            "(function(){var el=document.getElementById('add-a-game');if(el)el.click();})();";

    private static final String INJECT_CSS_JS =
            "(function(){" +
            "if(document.getElementById('app-injected-style'))return;" +
            "var s=document.createElement('style');" +
            "s.id='app-injected-style';" +
            "s.textContent='" +
            ".navbar{display:none!important;}" +
            "body.app-show-search .navbar{display:flex!important;}" +
            "body.app-show-search .navbar-toggler{display:none!important;}" +
            "body.app-show-search #navbarSupportedContent{display:block!important;height:auto!important;flex-basis:100%!important;}" +
            "body.app-show-search .navbar-nav{display:none!important;}" +
            "body.app-show-search #add-a-game{display:none!important;}" +
            "body.app-show-search .navbar-brand{display:none!important;}" +
            "';(document.head||document.documentElement).appendChild(s);" +
            "})();";

    private static final String SEARCH_JS =
            "(function(){" +
            "document.body.classList.add('app-show-search');" +
            "var c=document.getElementById('navbarSupportedContent');" +
            "if(c){c.classList.add('show');c.classList.remove('collapse');}" +
            "setTimeout(function(){" +
            "var sels=['#nav-bar-search','input.search-bar','input[name=\"query\"]','input[type=\"search\"]'];" +
            "for(var i=0;i<sels.length;i++){var el=document.querySelector(sels[i]);" +
            "if(el){" +
            "el.scrollIntoView({block:'center'});" +
            "el.focus();" +
            "el.click();" +
            "el.addEventListener('blur',function(){" +
            "setTimeout(function(){document.body.classList.remove('app-show-search');},200);" +
            "},{once:true});" +
            "return;}}" +
            "},50);" +
            "})();";


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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        errorLayout = findViewById(R.id.errorLayout);
        settingsButton = findViewById(R.id.settingsButton);
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerNav = findViewById(R.id.drawerNav);
        pageProgress = findViewById(R.id.pageProgress);
        Button retryButton = findViewById(R.id.retryButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupBottomNav();

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isBackloggdHost(uri)) {
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }

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
                if (url != null && isBackloggdHost(Uri.parse(url))) {
                    view.evaluateJavascript(INJECT_CSS_JS, null);
                }
                updateUiForUrl(url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                updateUiForUrl(url);
            }
        });

        myWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    if (pageProgress.getVisibility() != View.VISIBLE) {
                        pageProgress.setVisibility(View.VISIBLE);
                    }
                    pageProgress.setProgressCompat(progress, true);
                } else {
                    pageProgress.setVisibility(View.GONE);
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (myWeb.canGoBack()) {
                    myWeb.goBack();
                } else {
                    finish();
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
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            myWeb.loadUrl(data.toString());
            return;
        }
        if (intent.hasExtra("urlToLoad")) {
            String urlToLoad = intent.getStringExtra("urlToLoad");
            if (urlToLoad != null) {
                myWeb.loadUrl(urlToLoad);
            }
            return;
        }
        if (intent.hasExtra("postLaunchAction")) {
            runPostLaunchAction(intent.getStringExtra("postLaunchAction"));
            return;
        }
        if (myWeb.getUrl() == null) {
            myWeb.loadUrl(BACKLOGGD_URL);
        }
    }

    private void runPostLaunchAction(String action) {
        if (action == null) return;
        switch (action) {
            case "home":
                myWeb.loadUrl(BACKLOGGD_URL);
                break;
            case "log_game":
                myWeb.evaluateJavascript(LOG_GAME_JS, null);
                break;
            case "search":
                triggerSearch();
                break;
            case "profile":
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u));
                break;
            case "more":
                drawerLayout.openDrawer(GravityCompat.START);
                break;
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

    private static boolean isBackloggdHost(Uri uri) {
        String host = uri.getHost();
        return "backloggd.com".equalsIgnoreCase(host) || "www.backloggd.com".equalsIgnoreCase(host);
    }

    private void updateUiForUrl(String url) {
        Log.d(TAG, "Current URL: " + url);
        if (url != null && isSettingsUrl(Uri.parse(url))) {
            settingsButton.setVisibility(View.VISIBLE);
        } else {
            settingsButton.setVisibility(View.GONE);
        }
        updateBottomNavSelection(url);
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                myWeb.loadUrl(BACKLOGGD_URL);
                return true;
            } else if (id == R.id.nav_log_game) {
                myWeb.evaluateJavascript(LOG_GAME_JS, null);
                return false;
            } else if (id == R.id.nav_search) {
                triggerSearch();
                return false;
            } else if (id == R.id.nav_profile) {
                withUsername(username -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + username));
                return true;
            } else if (id == R.id.nav_more) {
                drawerLayout.openDrawer(GravityCompat.START);
                return false;
            }
            return false;
        });

        setupDrawerNav();

        bottomNav.setOnItemReselectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                myWeb.loadUrl(BACKLOGGD_URL);
            } else if (id == R.id.nav_log_game) {
                myWeb.evaluateJavascript(LOG_GAME_JS, null);
            } else if (id == R.id.nav_search) {
                triggerSearch();
            } else if (id == R.id.nav_profile) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u));
            }
        });
    }

    private interface UsernameAction {
        void run(String username);
    }

    private void withUsername(UsernameAction action) {
        String cached = UsernameHelper.getCached(this);
        if (cached != null) {
            action.run(cached);
            return;
        }
        Toast.makeText(this, R.string.fetching_username, Toast.LENGTH_SHORT).show();
        UsernameHelper.fetchAsync(this, username -> {
            if (isFinishing() || isDestroyed()) return;
            if (username != null) {
                action.run(username);
            } else {
                myWeb.loadUrl(LOGIN_URL);
            }
        });
    }

    private void triggerSearch() {
        myWeb.requestFocus();
        myWeb.evaluateJavascript(SEARCH_JS, null);
        myWeb.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(myWeb, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void setupDrawerNav() {
        drawerNav.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.drawer_notifications) {
                myWeb.loadUrl(NOTIFICATION_URL);
            } else if (id == R.id.drawer_played) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/games/"));
            } else if (id == R.id.drawer_playing) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/playing/"));
            } else if (id == R.id.drawer_backlog) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/backlog/"));
            } else if (id == R.id.drawer_wishlist) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/wishlist/"));
            } else if (id == R.id.drawer_journal) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/journal/"));
            } else if (id == R.id.drawer_activity) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/activity/"));
            } else if (id == R.id.drawer_reviews) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/reviews/"));
            } else if (id == R.id.drawer_lists) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/lists/"));
            } else if (id == R.id.drawer_friends) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/following/"));
            } else if (id == R.id.drawer_likes) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/likes/"));
            } else if (id == R.id.drawer_stats) {
                withUsername(u -> myWeb.loadUrl(BACKLOGGD_URL + "/u/" + u + "/stats/"));
            } else if (id == R.id.drawer_backloggd_settings) {
                myWeb.loadUrl(BACKLOGGD_URL + "/settings/");
            } else if (id == R.id.drawer_app_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.drawer_login) {
                myWeb.loadUrl(LOGIN_URL);
            } else if (id == R.id.drawer_logout) {
                UsernameHelper.clearCached(this);
                myWeb.loadUrl(LOGOUT_URL);
            } else if (id == R.id.drawer_about) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL)));
            }
            return true;
        });
    }

    private void updateBottomNavSelection(String url) {
        if (url == null || bottomNav == null) return;
        Uri uri = Uri.parse(url);
        if (!isBackloggdHost(uri)) return;

        String path = uri.getPath();
        if (path == null) path = "/";

        Integer itemId = null;
        if (path.equals("/") || path.isEmpty()) {
            itemId = R.id.nav_home;
        } else if (path.startsWith("/search")) {
            itemId = R.id.nav_search;
        } else if (path.startsWith("/u/")) {
            String username = UsernameHelper.getCached(this);
            if (username != null
                    && (path.equals("/u/" + username) || path.startsWith("/u/" + username + "/"))) {
                itemId = R.id.nav_profile;
            }
        }

        android.view.Menu menu = bottomNav.getMenu();
        if (itemId != null) {
            if (bottomNav.getSelectedItemId() != itemId) {
                menu.findItem(itemId).setChecked(true);
            }
        } else {
            android.view.MenuItem current = menu.findItem(bottomNav.getSelectedItemId());
            if (current != null && current.isChecked()) {
                int groupId = current.getGroupId();
                menu.setGroupCheckable(groupId, true, false);
                current.setChecked(false);
                menu.setGroupCheckable(groupId, true, true);
            }
        }
    }

    private static boolean isSettingsUrl(Uri uri) {
        if (!isBackloggdHost(uri)) {
            return false;
        }
        String path = uri.getPath();
        return path != null && (path.equals("/settings") || path.startsWith("/settings/"));
    }

    private void checkForUpdates() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, GITHUB_TAGS_API_URL, null,
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
                        }                    } catch (JSONException e) {
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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_LATEST));
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
