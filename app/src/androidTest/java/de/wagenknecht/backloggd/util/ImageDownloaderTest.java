package de.wagenknecht.backloggd.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ImageDownloaderTest {

    // httpbin returns a known stable PNG; swap to any Backloggd image URL
    // (e.g. a game cover from your wishlist) to test against the real site.
    private static final String VALID_IMAGE_URL = "https://httpbin.org/image/png";

    private static final String NOT_FOUND_URL =
            "https://www.backloggd.com/this-path-does-not-exist-12345.png";

    private static final String MALFORMED_URL = "not-a-real-url";

    @Test
    public void downloadDownsampled_returnsBitmapForValidUrl() {
        Bitmap bitmap = ImageDownloader.downloadDownsampled(VALID_IMAGE_URL);
        assertNotNull("Expected non-null bitmap from a valid image URL", bitmap);
        assertTrue("Width should be > 0", bitmap.getWidth() > 0);
        assertTrue("Height should be > 0", bitmap.getHeight() > 0);
        assertTrue("Bitmap should be downsampled to <= 512px wide",
                bitmap.getWidth() <= 512);
        assertTrue("Bitmap should be downsampled to <= 512px tall",
                bitmap.getHeight() <= 512);
    }

    @Test
    public void downloadDownsampled_returnsNullFor404() {
        assertNull(ImageDownloader.downloadDownsampled(NOT_FOUND_URL));
    }

    @Test
    public void downloadDownsampled_returnsNullForMalformedUrl() {
        assertNull(ImageDownloader.downloadDownsampled(MALFORMED_URL));
    }
}
