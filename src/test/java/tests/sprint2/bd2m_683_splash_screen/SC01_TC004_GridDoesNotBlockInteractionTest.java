package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;

/**
 * SC_01_TC_004 - Verify that the auto-scrolling image grid does not block user interaction with the screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & tap an action button while the image grid is scrolling
 * Expected:      User should be able to interact with the screen without any blocking caused by the auto-scrolling image grid.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Confirms the grid is scrolling, then taps the primary action button with a touch: the app
 * must respond by leaving the splash screen.
 */
public class SC01_TC004_GridDoesNotBlockInteractionTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_004: Verify that the auto-scrolling image grid does not block user interaction with the screen.")
    public void verifyImageGridDoesNotBlockInteraction() {
        SplashLandingScreen screen = openSplash();
        assertGridScrolling(screen, "before the tap");

        Locator button = configuredOrNthButton(screen, "splash.primaryButtonLabel", 0);
        String name = SplashLandingScreen.nameOf(button);
        String before = screen.route();
        log("Tapping \"" + name + "\" while the grid scrolls");
        String after = tapAndWaitForNewRoute(screen, button);
        screenshot("after-tapping-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        log("Route before: #" + before + ", after: #" + after);

        Assert.assertNotEquals(after, before, "Tapping \"" + name
            + "\" while the image grid was scrolling did nothing - the app stayed on #" + before);
    }
}
