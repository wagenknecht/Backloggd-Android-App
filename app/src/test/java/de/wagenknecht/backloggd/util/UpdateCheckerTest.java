package de.wagenknecht.backloggd.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UpdateCheckerTest {

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
