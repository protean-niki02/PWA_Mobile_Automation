package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_008 - Verify that the Bima Sugam brand logo is displayed on the splash screen.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check brand logo
 * Expected:      Bima Sugam brand logo should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The logo is a picture Flutter draws without a label, so two things are checked: a Bima Sugam
 * logo file was downloaded, and something is actually drawn in the logo's place, between the
 * image grid and the "Welcome to Bima Sugam" heading. That area is saved as logo-area.png.
 */
public class SC01_TC008_BrandLogoDisplayedTest extends SplashLandingTestBase {

    /** An empty logo area is all white; the logo leaves well under this share white. */
    private static final double MAX_WHITE_SHARE = 0.97;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_008: Verify that the Bima Sugam brand logo is displayed on the splash screen.")
    public void verifyBrandLogoIsDisplayed() {
        AssetRecorder assets = new AssetRecorder(page);
        SplashLandingScreen screen = openSplash();

        BoundingBox heading = box(screen.welcomeHeading());
        Assert.assertNotNull(heading, "\"" + SplashLandingScreen.WELCOME_HEADING
            + "\" heading not found, so the logo area above it can't be located");
        double top = screen.gridBottom();
        byte[] logoArea = ScreenPixels.capture(page, 0, top, page.viewportSize().width, heading.y - top);
        saveEvidence("logo-area.png", logoArea);
        double white = ScreenPixels.whiteShare(logoArea);

        List<Asset> logos = brandLogos(assets);
        log("Logo files loaded: " + logos.stream().map(Asset::fileName).distinct().toList()
            + "; logo area " + Math.round(white * 100) + "% white");

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(logos.isEmpty(), "No Bima Sugam logo image (" + BRAND_LOGO_FILES + ") was loaded. Images loaded: "
            + assets.all().stream().map(Asset::fileName).distinct().toList());
        softly.assertTrue(white < MAX_WHITE_SHARE,
            "Nothing is drawn between the image grid and the heading - the brand logo is not displayed");
        softly.assertAll();
    }
}
