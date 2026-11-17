package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;

import java.util.List;

/**
 * SC_01_TC_015 - Verify that tapping the primary action button navigates the user to the correct destination.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     splash.primaryButtonRoute in config.properties (the destination's route, e.g. /login)
 * Step 1:        Launch the app & land on the splash screen & tap the primary action button
 * Expected:      User should be navigated to the correct destination on tapping the primary action button.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Taps the primary (top-most) action button with a touch; the app must open the expected
 * route. Skipped while the expected destination is not set.
 */
public class SC01_TC015_PrimaryButtonNavigationTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_015: Verify that tapping the primary action button navigates the user to the correct destination.")
    public void verifyPrimaryButtonNavigation() {
        String expectedRoute = required("splash.primaryButtonRoute", "Expected destination of the primary button");
        SplashLandingScreen screen = openSplash();

        List<Locator> buttons = screen.actionButtons();
        Assert.assertFalse(buttons.isEmpty(), "No action button is displayed on the splash screen");
        String name = SplashLandingScreen.nameOf(buttons.get(0));
        String route = tapAndWaitForNewRoute(screen, buttons.get(0));
        screenshot("after-tapping-primary-button");
        log("Tapped \"" + name + "\": now on #" + route + " (expected #" + expectedRoute + ")");

        Assert.assertEquals(route, expectedRoute, "Tapping the primary button \"" + name + "\" opened the wrong screen");
    }
}
