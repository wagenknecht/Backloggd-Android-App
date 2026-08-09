package de.wagenknecht.backloggd.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UpdateCheckerTest {

    @Test
    public void plainText_dropsBadgeImages() {
        String markdown = "[![Github All Releases](https://img.shields.io/x.svg)](https://example.com/a.apk)\n"
                + "\n# Changelog\n- fixed back press handling";
        assertEquals("Changelog\n• fixed back press handling", UpdateChecker.toPlainText(markdown));
    }

    @Test
    public void plainText_keepsLinkLabelsWithoutUrls() {
        assertEquals("Download 2.0 here",
                UpdateChecker.toPlainText("[**Download 2.0 here**](https://example.com/a.apk)"));
    }

    @Test
    public void plainText_stripsHeadingsAndEmphasis() {
        assertEquals("What's new\n\nBottom navigation bar with Home",
                UpdateChecker.toPlainText("## What's new\n\n**Bottom navigation bar** with Home"));
    }

    @Test
    public void plainText_turnsListMarkersIntoBullets() {
        assertEquals("• one\n• two\n• three",
                UpdateChecker.toPlainText("- one\n* two\n+ three"));
    }

    @Test
    public void plainText_handlesEmptyNotes() {
        assertEquals("", UpdateChecker.toPlainText(""));
    }

    @Test
    public void plainText_cutsOverlyLongNotes() {
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            huge.append("change ").append(i).append('\n');
        }
        String result = UpdateChecker.toPlainText(huge.toString());
        assertTrue("Should be capped, was " + result.length(), result.length() <= 2001);
        assertTrue("Should be marked as cut off", result.endsWith("…"));
    }

    @Test
    public void sameVersion_isNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("2.0", "2.0"));
        assertFalse(UpdateChecker.isNewerVersion("2.0", "2.0.0"));
    }

    @Test
    public void higherVersion_isNewer() {
        assertTrue(UpdateChecker.isNewerVersion("2.1", "2.0"));
        assertTrue(UpdateChecker.isNewerVersion("1.4.1", "1.4"));
    }

    @Test
    public void localAheadOfRelease_isNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("1.5", "2.0"));
        assertFalse(UpdateChecker.isNewerVersion("1.4", "1.4.1"));
    }

    @Test
    public void differingSegmentCounts_compareByValue() {
        assertTrue(UpdateChecker.isNewerVersion("2.1", "2.0.3"));
        assertFalse(UpdateChecker.isNewerVersion("2.0.3", "2.1"));
    }

    @Test
    public void comparesNumerically_notLexicographically() {
        assertTrue(UpdateChecker.isNewerVersion("10.0", "9.0"));
        assertFalse(UpdateChecker.isNewerVersion("9.0", "10.0"));
    }

    @Test
    public void toleratesVPrefix() {
        assertTrue(UpdateChecker.isNewerVersion("v2.1", "2.0"));
        assertFalse(UpdateChecker.isNewerVersion("v2.0", "v2.0"));
    }

    @Test
    public void unusableRemoteVersion_neverPrompts() {
        assertFalse(UpdateChecker.isNewerVersion("", "2.0"));
        assertFalse(UpdateChecker.isNewerVersion("garbage", "2.0"));
        assertFalse(UpdateChecker.isNewerVersion("2.0-beta", "2.0"));
    }
}
