package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_014 - Verify that the primary action button is displayed with the correct label.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     splash.primaryButtonLabel in config.properties (the label from the FSD / Figma)
 * Step 1:        Launch the app & land on the splash screen & check primary action button label
 * Expected:      Primary action button should be displayed with the correct label.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The primary button is the top-most action button; its label must be exactly the expected one.
 * Skipped while the expected label is not set.
 */
public class SC01_TC014_PrimaryButtonLabelTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_014: Verify that the primary action button is displayed with the correct label.")
    public void verifyPrimaryButtonLabel() {
        String expected = required("splash.primaryButtonLabel", "Expected primary button label");
        SplashLandingScreen screen = openSplash();

        List<Locator> buttons = screen.actionButtons();
        Assert.assertFalse(buttons.isEmpty(), "No action button is displayed on the splash screen");
        Locator primary = buttons.get(0);
        String shown = SplashLandingScreen.nameOf(primary);
        if (screen.bringIntoView(primary)) {
            saveEvidence("primary-button.png", ScreenPixels.captureRow(page, primary.boundingBox(), 12));
        }
        log("Primary button label: \"" + shown + "\" (expected \"" + expected + "\")");

        Assert.assertEquals(shown, expected, "Primary action button has the wrong label");
    }
}
