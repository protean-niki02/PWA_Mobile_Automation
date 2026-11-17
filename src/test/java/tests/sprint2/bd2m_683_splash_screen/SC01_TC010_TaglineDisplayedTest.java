package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

/**
 * SC_01_TC_010 - Verify that the "Of the Industry . By the Industry . For the Nation" tagline is displayed on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check tagline
 * Expected:      "Of the Industry . By the Industry . For the Nation" tagline should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The three phrases must appear in order; the separators between them ("." or "·") and
 * letter case may differ. Waits up to 20s in case the tagline is on a later carousel slide.
 */
public class SC01_TC010_TaglineDisplayedTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_010: Verify that the \"Of the Industry . By the Industry . For the Nation\" tagline is displayed on the splash screen.")
    public void verifyTaglineIsDisplayed() {
        SplashLandingScreen screen = openSplash();

        boolean shown = waitUntilShown(screen::showsTagline, 20_000);
        if (shown && screen.tagline().count() > 0 && screen.bringIntoView(screen.tagline())) {
            saveEvidence("tagline.png", ScreenPixels.captureRow(page, screen.tagline().boundingBox(), 12));
        }
        screenshot("tagline");
        Assert.assertTrue(shown, "Tagline \"" + SplashLandingScreen.TAGLINE
            + "\" is not displayed on the splash screen. Screen text: " + screen.semanticsText());
    }
}
