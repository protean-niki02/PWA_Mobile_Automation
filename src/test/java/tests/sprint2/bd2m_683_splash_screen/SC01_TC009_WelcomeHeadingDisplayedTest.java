package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

/**
 * SC_01_TC_009 - Verify that the "Welcome to Bima Sugam" heading is displayed on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check heading
 * Expected:      "Welcome to Bima Sugam" heading should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 */
public class SC01_TC009_WelcomeHeadingDisplayedTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_009: Verify that the \"Welcome to Bima Sugam\" heading is displayed on the splash screen.")
    public void verifyWelcomeHeadingIsDisplayed() {
        SplashLandingScreen screen = openSplash();

        Locator heading = screen.welcomeHeading();
        Assert.assertTrue(screen.isOnScreen(heading), "\"" + SplashLandingScreen.WELCOME_HEADING
            + "\" heading is not displayed on the splash screen. Screen text: " + screen.semanticsText());
        saveEvidence("heading.png", ScreenPixels.captureRow(page, heading.boundingBox(), 12));
    }
}
