package de.wagenknecht.backloggd;

import static de.wagenknecht.backloggd.ApiConstants.GITHUB_RELEASES_LATEST;
import static de.wagenknecht.backloggd.ApiConstants.GITHUB_REPO_URL;
import static de.wagenknecht.backloggd.ApiConstants.BACKLOGGD_URL;
import static de.wagenknecht.backloggd.ApiConstants.LOGIN_URL;
import static de.wagenknecht.backloggd.ApiConstants.LOGOUT_URL;
import static de.wagenknecht.backloggd.ApiConstants.NOTIFICATION_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import de.wagenknecht.backloggd.util.UpdateChecker;
import de.wagenknecht.backloggd.util.UsernameHelper;
import de.wagenknecht.backloggd.worker.NotificationCheckWorker;
import de.wagenknecht.backloggd.worker.WishlistCheckerWorker;

public class MainActivity extends AppCompatActivity {
    private WebView myWeb;
    private LinearLayout errorLayout;
    private BottomNavigationView bottomNav;
    private DrawerLayout drawerLayout;
    private NavigationView drawerNav;
    private LinearProgressIndicator pageProgress;
    private SwipeRefreshLayout swipeRefresh;
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
        bottomNav = findViewById(R.id.bottomNav);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerNav = findViewById(R.id.drawerNav);
        pageProgress = findViewById(R.id.pageProgress);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        Button retryButton = findViewById(R.id.retryButton);

        swipeRefresh.setColorSchemeResources(R.color.back_pink);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.back_secondary);
        swipeRefresh.setOnRefreshListener(() -> myWeb.reload());

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

        myWeb.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isBackloggdHost(uri)) {
                    return false;
                }

                openExternally(uri);
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
                    swipeRefresh.setRefreshing(false);
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
                swipeRefresh.setRefreshing(false);
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

        // Only on a fresh start, so a rotation does not check again.
        if (savedInstanceState == null) {
            UpdateChecker.checkAsync(this, this::showUpdateDialog);
        }
        askNotificationPermission();
        NotificationCheckWorker.schedule(this);
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
            } else if (id == R.id.drawer_logout) {
                UsernameHelper.clearCached(this);
                myWeb.loadUrl(LOGOUT_URL);
            } else if (id == R.id.drawer_feedback) {
                Intent feedback = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:dev.wagenknecht@gmail.com"));
                feedback.putExtra(Intent.EXTRA_SUBJECT, "Backloggd");
                try {
                    startActivity(feedback);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.feedback_no_email_app, Toast.LENGTH_SHORT).show();
                }
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

    /**
     * @param apkUrl direct link to the release's APK, or null to fall back to the releases page.
     */
    private void showUpdateDialog(String newVersion, @Nullable String apkUrl) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        String downloadUrl = apkUrl != null ? apkUrl : GITHUB_RELEASES_LATEST;
        new AlertDialog.Builder(this)
                .setTitle(R.string.update_available_title)
                .setMessage(getString(R.string.update_available_message, newVersion))
                .setPositiveButton(R.string.update_download,
                        (dialog, which) -> openExternally(Uri.parse(downloadUrl)))
                .setNegativeButton(R.string.update_later, null)
                .show();
    }

    /** Hands a URI to another app, telling the user when nothing can handle it. */
    private void openExternally(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (android.content.ActivityNotFoundException e) {
            Log.w(TAG, "No app can handle " + uri, e);
            Toast.makeText(this, R.string.no_app_for_link, Toast.LENGTH_SHORT).show();
        }
    }
}
