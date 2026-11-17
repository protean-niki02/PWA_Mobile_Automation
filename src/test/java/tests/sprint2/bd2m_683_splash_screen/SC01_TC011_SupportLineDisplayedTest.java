package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

/**
 * SC_01_TC_011 - Verify that the "Start your journey to complete financial security." support line is displayed on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check support line
 * Expected:      "Start your journey to complete financial security." support line should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Letter case and the final full stop may differ. Waits up to 20s in case the line is on a
 * later carousel slide.
 */
public class SC01_TC011_SupportLineDisplayedTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_011: Verify that the \"Start your journey to complete financial security.\" support line is displayed on the splash screen.")
    public void verifySupportLineIsDisplayed() {
        SplashLandingScreen screen = openSplash();

        boolean shown = waitUntilShown(screen::showsSupportLine, 20_000);
        if (shown && screen.supportLine().count() > 0 && screen.bringIntoView(screen.supportLine())) {
            saveEvidence("support-line.png", ScreenPixels.captureRow(page, screen.supportLine().boundingBox(), 12));
        }
        screenshot("support-line");
        Assert.assertTrue(shown, "Support line \"" + SplashLandingScreen.SUPPORT_LINE
            + "\" is not displayed on the splash screen. Screen text: " + screen.semanticsText());
    }
}
