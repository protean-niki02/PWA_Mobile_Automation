package tests.sprint1.splashscreen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.List;

/**
 * SC_01_TC_006 - Verify that the updated grid tile images are displayed on the splash screen.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on the splash screen
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & check grid tile images
 * Expected:      Updated grid tile images should be displayed on the splash screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The splash is everything shown before the app's first screen: the HTML splash and, if the
 * app uses it, the #/splash route. Grid tile photos must be loaded while it is shown and be
 * real images. Screenshots of the splash and of the first screen after it are evidence.
 */
public class SC01_TC006_GridTilesOnSplashTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_006: Verify that the updated grid tile images are displayed on the splash screen.")
    public void verifyGridTilesOnSplashScreen() {
        AssetRecorder assets = new AssetRecorder(page);
        SplashScreen splash = new SplashScreen(page);

        splash.launch();
        splash.waitForHtmlSplashLogoRendered();
        screenshot("splash-screen");
        if (SplashScreen.FLUTTER_SPLASH_ROUTE.equals(splash.waitForFirstAppRoute())) {
            screenshot("flutter-splash");
        }
        String firstScreen = splash.waitUntilSplashFinished();
        screenshot("first-screen-after-splash");
        log("Launch went through routes " + splash.routesSeen() + ", first screen " + firstScreen);

        List<Asset> splashImages = assets.where(SplashScreenTestBase::isSplashImage);
        List<Asset> tiles = splashImages.stream().filter(asset -> asset.url().contains(GRID_TILE_PATH)).toList();
        log("Images on the splash: " + splashImages.stream().map(Asset::fileName).distinct().toList());

        Assert.assertFalse(tiles.isEmpty(), "No grid tile images are displayed on the splash screen. The splash shows "
            + splashImages.stream().map(Asset::fileName).distinct().toList()
            + (splash.flutterSplashWasShown() ? "" : " only (the app does not use its #/splash screen)")
            + "; grid tiles first appear on " + firstScreen);

        SoftAssert softly = new SoftAssert();
        assertImagesLoaded(softly, tiles, "grid-tiles-splash");
        softly.assertAll();
    }
}
