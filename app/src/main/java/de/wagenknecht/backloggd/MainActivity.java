package de.wagenknecht.backloggd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout; // Import für LinearLayout

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWeb;
    private LinearLayout errorLayout; // Layout für die Fehleransicht
    private boolean receivedError = false; // Zustand für den Fehlerfall

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWeb = findViewById(R.id.myWeb);
        errorLayout = findViewById(R.id.errorLayout);
        // Button zum Neuladen
        Button retryButton = findViewById(R.id.retryButton);

        myWeb.getSettings().setJavaScriptEnabled(true);
        myWeb.getSettings().setDomStorageEnabled(true);

        // OnClickListener für den "Erneut versuchen"-Button
        retryButton.setOnClickListener(v -> {
            // Blende die Fehleransicht aus und lade die URL neu
            myWeb.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            myWeb.reload();
        });

        myWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Setze den Fehlerzustand bei jedem neuen Ladeversuch zurück
                receivedError = false;
                // Verstecke die Fehleransicht beim Start eines neuen Ladeversuchs
                errorLayout.setVisibility(View.GONE);
                myWeb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                // Fehler nur für die Hauptseite behandeln, um Fehler bei Sub-Ressourcen zu ignorieren
                if (request.isForMainFrame()) {
                    receivedError = true; // Fehlerzustand setzen
                    myWeb.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE); // Zeige das Fehler-Layout an
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Zeige die WebView nur an, wenn KEIN Fehler aufgetreten ist.
                if (!receivedError) {
                    myWeb.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                }
            }
        });

        myWeb.loadUrl("https://backloggd.com/");

        // Handle back gesture
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
}
