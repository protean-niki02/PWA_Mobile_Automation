package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;

import java.util.List;

/**
 * SC_01_TC_017 - Verify that tapping the secondary action button navigates the user to the correct destination.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     splash.secondaryButtonRoute in config.properties (the destination's route)
 * Step 1:        Launch the app & land on the splash screen & tap the secondary action button
 * Expected:      User should be navigated to the correct destination on tapping the secondary action button.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Taps the secondary (second from the top) action button with a touch; the app must open the
 * expected route. Skipped while the expected destination is not set.
 */
public class SC01_TC017_SecondaryButtonNavigationTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_017: Verify that tapping the secondary action button navigates the user to the correct destination.")
    public void verifySecondaryButtonNavigation() {
        String expectedRoute = required("splash.secondaryButtonRoute", "Expected destination of the secondary button");
        SplashLandingScreen screen = openSplash();

        List<Locator> buttons = screen.actionButtons();
        Assert.assertTrue(buttons.size() >= 2, "No secondary action button is displayed on the splash screen; buttons: "
            + buttons.stream().map(SplashLandingScreen::nameOf).toList());
        Locator secondary = buttons.get(1);
        String name = SplashLandingScreen.nameOf(secondary);
        screen.bringIntoView(secondary);
        String route = tapAndWaitForNewRoute(screen, secondary);
        screenshot("after-tapping-secondary-button");
        log("Tapped \"" + name + "\": now on #" + route + " (expected #" + expectedRoute + ")");

        Assert.assertEquals(route, expectedRoute, "Tapping the secondary button \"" + name + "\" opened the wrong screen");
    }
}
