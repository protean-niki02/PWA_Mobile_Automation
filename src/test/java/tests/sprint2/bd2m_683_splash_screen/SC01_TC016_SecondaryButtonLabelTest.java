package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_016 - Verify that the secondary action button is displayed with the correct label.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     splash.secondaryButtonLabel in config.properties (the label from the FSD / Figma)
 * Step 1:        Launch the app & land on the splash screen & check secondary action button label
 * Expected:      Secondary action button should be displayed with the correct label.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The secondary button is the second action button from the top; its label must be exactly
 * the expected one. Skipped while the expected label is not set.
 */
public class SC01_TC016_SecondaryButtonLabelTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_016: Verify that the secondary action button is displayed with the correct label.")
    public void verifySecondaryButtonLabel() {
        String expected = required("splash.secondaryButtonLabel", "Expected secondary button label");
        SplashLandingScreen screen = openSplash();

        List<Locator> buttons = screen.actionButtons();
        Assert.assertTrue(buttons.size() >= 2, "No secondary action button is displayed on the splash screen; buttons: "
            + buttons.stream().map(SplashLandingScreen::nameOf).toList());
        Locator secondary = buttons.get(1);
        String shown = SplashLandingScreen.nameOf(secondary);
        if (screen.bringIntoView(secondary)) {
            saveEvidence("secondary-button.png", ScreenPixels.captureRow(page, secondary.boundingBox(), 12));
        }
        log("Secondary button label: \"" + shown + "\" (expected \"" + expected + "\")");

        Assert.assertEquals(shown, expected, "Secondary action button has the wrong label");
    }
}
