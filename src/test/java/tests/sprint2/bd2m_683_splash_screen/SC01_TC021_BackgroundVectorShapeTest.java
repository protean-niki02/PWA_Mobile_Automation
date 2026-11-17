package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_021 - Verify that the background decorative vector shape is displayed correctly on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check background vector shape
 * Expected:      Background decorative vector shape should be displayed correctly on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * In the app the decorative shape (an outline motif) sits at the bottom, below the action
 * buttons. The screen is scrolled to its end and the area below the last button checked for a
 * drawn shape: at least 1% of it not white, with visible lines. The area is saved as
 * background-shape-area.png; whether it matches the design is SC_01_TC_020.
 */
public class SC01_TC021_BackgroundVectorShapeTest extends SplashLandingTestBase {

    private static final double MIN_DRAWN_SHARE = 0.01;
    private static final double MIN_SPREAD = 3;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_021: Verify that the background decorative vector shape is displayed correctly on the splash screen.")
    public void verifyBackgroundVectorShapeIsDisplayed() {
        SplashLandingScreen screen = openSplash();
        for (int i = 0; i < 5; i++) {
            screen.scrollDown(600);
        }
        screenshot("bottom-of-screen");

        double width = page.viewportSize().width;
        double height = page.viewportSize().height;
        List<Locator> buttons = screen.actionButtons();
        double top = buttons.stream().mapToDouble(b -> b.boundingBox().y + b.boundingBox().height + 8)
            .max().orElse(height * 0.75);
        if (height - top < 30) {
            top = height * 0.75;
        }
        byte[] area = ScreenPixels.capture(page, 0, top, width, height - top);
        saveEvidence("background-shape-area.png", area);
        double drawn = 1 - ScreenPixels.whiteShare(area);
        double spread = ScreenPixels.brightnessSpread(area);
        log(String.format("Area below the buttons (%.0f px tall): %.1f%% drawn, brightness spread %.1f",
            height - top, drawn * 100, spread));

        Assert.assertTrue(drawn >= MIN_DRAWN_SHARE && spread >= MIN_SPREAD,
            "No background decorative vector shape is displayed below the action buttons (area is blank)");
    }
}
