package tests.sprint1.splashscreen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.WelcomePage;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.List;

/**
 * SC_01_TC_005 - Verify that the updated grid tile images are displayed on the onboarding screens.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on an onboarding screen
 * Test data:     None
 * Step 1:        Launch the app & navigate through the onboarding screens & check grid tile images
 * Expected:      Updated grid tile images should be displayed on the onboarding screens.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Goes through every onboarding slide (screenshot of each), then checks the grid tile
 * photos the screens use all downloaded and are real images.
 */
public class SC01_TC005_GridTilesOnOnboardingTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_005: Verify that the updated grid tile images are displayed on the onboarding screens.")
    public void verifyGridTilesOnOnboardingScreens() {
        AssetRecorder assets = new AssetRecorder(page);
        WelcomePage onboarding = new WelcomePage(page).open();

        String slide = onboarding.semanticsText();
        screenshot("onboarding-slide-1");
        for (int i = 2; i <= WelcomePage.SLIDE_COUNT; i++) {
            slide = onboarding.nextSlide(slide);
            screenshot("onboarding-slide-" + i);
        }

        List<Asset> tiles = assets.matching(GRID_TILE_PATH);
        Assert.assertFalse(tiles.isEmpty(), "No grid tile images were loaded on the onboarding screens");

        SoftAssert softly = new SoftAssert();
        assertImagesLoaded(softly, tiles, "grid-tiles");
        softly.assertAll();
    }
}
