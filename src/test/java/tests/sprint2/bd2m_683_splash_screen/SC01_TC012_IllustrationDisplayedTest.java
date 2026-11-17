package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_012 - Verify that the illustration/graphic block is displayed correctly on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check illustration
 * Expected:      Illustration/graphic block should be displayed correctly on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The illustration sits between the heading and the action buttons and has no label, so the
 * check is on that area's pixels: a multi-coloured picture (at least 4 distinct colours, each
 * covering 3% or more of what is drawn) taking up a real part of it, where plain text gives one
 * or two colours. The slides change pictures, and one large shape can dominate a picture. The area is saved as
 * illustration-area.png. Whether it matches the design is SC_01_TC_020.
 */
public class SC01_TC012_IllustrationDisplayedTest extends SplashLandingTestBase {

    /** Colours that each cover at least 3% of the drawn pixels: a picture has several, text one or two. */
    private static final int MIN_COLOURS = 4;
    private static final double MIN_COLOUR_SHARE = 0.03;
    private static final double MAX_WHITE_SHARE = 0.9;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_012: Verify that the illustration/graphic block is displayed correctly on the splash screen.")
    public void verifyIllustrationIsDisplayed() {
        SplashLandingScreen screen = openSplash();

        BoundingBox heading = box(screen.welcomeHeading());
        List<Locator> buttons = screen.actionButtons();
        Assert.assertNotNull(heading, "\"" + SplashLandingScreen.WELCOME_HEADING + "\" heading not found");
        Assert.assertFalse(buttons.isEmpty(), "No action button found below the illustration");
        double top = heading.y + heading.height + 8;
        double bottom = buttons.get(0).boundingBox().y - 8;
        Assert.assertTrue(bottom - top > 40, String.format(
            "There is no room for an illustration between the heading and the buttons (%.0f px)", bottom - top));

        byte[] area = ScreenPixels.capture(page, 0, top, page.viewportSize().width, bottom - top);
        saveEvidence("illustration-area.png", area);
        int colours = ScreenPixels.countColours(area, MIN_COLOUR_SHARE);
        double white = ScreenPixels.whiteShare(area);
        log(String.format("Illustration area %.0f px tall: %d colours, %.0f%% white", bottom - top, colours, white * 100));

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(colours >= MIN_COLOURS, "No illustration/graphic is displayed between the heading and the "
            + "buttons: the area shows only " + colours + " colour(s), as plain text would");
        softly.assertTrue(white <= MAX_WHITE_SHARE, String.format(
            "The illustration area is %.0f%% empty (white) - the graphic is missing or too small", white * 100));
        softly.assertAll();
    }
}
