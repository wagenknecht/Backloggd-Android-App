package de.wagenknecht.backloggd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWeb;
    private LinearLayout errorLayout;
    private ImageButton settingsButton;
    private boolean receivedError = false;
    private static final String TAG = "MainActivity";

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
                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        });
    }

    private void updateUiForUrl(String url) {
        Log.d(TAG, "Current URL: " + url);
        if ("https://backloggd.com/settings/".equals(url)) {
            settingsButton.setVisibility(View.VISIBLE);
        } else {
            settingsButton.setVisibility(View.GONE);
        }
    }
}