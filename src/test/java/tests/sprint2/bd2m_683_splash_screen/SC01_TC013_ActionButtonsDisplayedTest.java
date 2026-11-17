package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_013 - Verify that both the primary and secondary action buttons are displayed on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check action buttons
 * Expected:      Both the primary and secondary action buttons should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * At least two action buttons below the image grid, each fully on screen (scrolled to if the
 * screen is taller than the phone). A picture of each button row is saved.
 */
public class SC01_TC013_ActionButtonsDisplayedTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_013: Verify that both the primary and secondary action buttons are displayed on the splash screen.")
    public void verifyPrimaryAndSecondaryButtonsAreDisplayed() {
        SplashLandingScreen screen = openSplash();
        List<Locator> buttons = assertActionButtons(screen);

        SoftAssert softly = new SoftAssert();
        String[] roles = {"Primary", "Secondary"};
        for (int i = 0; i < 2; i++) {
            Locator button = buttons.get(i);
            String name = SplashLandingScreen.nameOf(button);
            boolean visible = screen.bringIntoView(button);
            if (visible) {
                saveEvidence(roles[i].toLowerCase() + "-button.png", ScreenPixels.captureRow(page, button.boundingBox(), 12));
            }
            log(roles[i] + " button: \"" + name + "\"" + (visible ? "" : " - not fully on screen"));
            softly.assertTrue(visible, roles[i] + " action button \"" + name + "\" is not fully displayed on screen");
        }
        screen.scrollToTop();
        screenshot("action-buttons");
        softly.assertAll();
    }
}
