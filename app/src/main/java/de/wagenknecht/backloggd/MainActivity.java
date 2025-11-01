package de.wagenknecht.backloggd;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWeb;
    private static final String BASE_URL = "https://backloggd.com/";
    private static final String PREFS = "backloggd_prefs";
    private static final String KEY_AUTO_TRANSLATE = "auto_translate_enabled";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        myWeb = findViewById(R.id.myWeb);
        WebSettings settings = myWeb.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Keep navigation inside the WebView
        myWeb.setWebViewClient(new WebViewClient());

        // Register context menu to allow toggling translate later (long-press -> "Translate" toggle)
        registerForContextMenu(myWeb);

        // Load either normal site or Google Translate proxy depending on preference
        loadAppropriateUrl();

        // Handle back gesture
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (myWeb.canGoBack()) {
                    myWeb.goBack();
                } else {
                    setEnabled(false);
                }
            }
        });

        // Ask once on first run if user wants auto-translate enabled
        maybeAskEnableAutoTranslate();
    }

    private void loadAppropriateUrl() {
        boolean autoTranslate = prefs.getBoolean(KEY_AUTO_TRANSLATE, false);
        if (autoTranslate) {
            // Google Translate web UI wrapper. sl=auto (source auto-detect), tl=en (target English)
            String proxy = "https://translate.google.com/translate?sl=auto&tl=en&u=" + BASE_URL;
            myWeb.loadUrl(proxy);
        } else {
            myWeb.loadUrl(BASE_URL);
        }
    }

    private void maybeAskEnableAutoTranslate() {
        // Only ask if preference not set (use contains)
        if (!prefs.contains(KEY_AUTO_TRANSLATE)) {
            new AlertDialog.Builder(this)
                .setTitle("Auto-translate")
                .setMessage("Enable automatic translation to English using Google Translate when foreign content is detected? This uses Google Translate web UI.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, true).apply();
                    Toast.makeText(MainActivity.this, "Auto-translate enabled", Toast.LENGTH_SHORT).show();
                    loadAppropriateUrl();
                })
                .setNegativeButton("Don't enable", (dialog, which) -> {
                    prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, false).apply();
                    Toast.makeText(MainActivity.this, "Auto-translate disabled", Toast.LENGTH_SHORT).show();
                    loadAppropriateUrl();
                })
                .setCancelable(false)
                .show();
        }
    }

    // Simple context menu to toggle translate on long press of the WebView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == myWeb) {
            menu.setHeaderTitle("Web options");
            boolean enabled = prefs.getBoolean(KEY_AUTO_TRANSLATE, false);
            menu.add(0, 1, 0, enabled ? "Disable Auto-translate" : "Enable Auto-translate");
            menu.add(0, 2, 1, "Reload page");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            boolean enabled = prefs.getBoolean(KEY_AUTO_TRANSLATE, false);
            prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, !enabled).apply();
            Toast.makeText(this, "Auto-translate " + (!enabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            loadAppropriateUrl();
            return true;
        } else if (item.getItemId() == 2) {
            myWeb.reload();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}