package de.wagenknecht.backloggd.util;

import static de.wagenknecht.backloggd.ApiConstants.BACKLOGGD_URL;

import android.content.Context;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public final class BackloggdRequest {

    private BackloggdRequest() {}

    public static Connection forUrl(@NonNull Context context, @NonNull String url, @Nullable String cookies) {
        Connection conn = Jsoup.connect(url)
                .userAgent(WebSettings.getDefaultUserAgent(context))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", BACKLOGGD_URL + "/")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-User", "?1")
                .ignoreHttpErrors(true);
        if (cookies != null && !cookies.isEmpty()) {
            conn.header("Cookie", cookies);
        }
        return conn;
    }
}
