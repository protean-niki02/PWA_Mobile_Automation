package tests.sprint1.splashscreen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * SC_01_TC_003 - Verify that the updated splash screen images are displayed when the app is launched.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: Mobile application is installed on the device
 * Test data:     None
 * Step 1:        Launch the app & check splash screen images
 * Expected:      Updated splash screen images should be displayed when the app is launched.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Checks the HTML splash logo is shown and actually rendered, then that every image the
 * app loads during the splash (HTML splash and #/splash) downloads and is a real image.
 */
public class SC01_TC003_SplashScreenImagesDisplayedTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_003: Verify that the updated splash screen images are displayed when the app is launched.")
    public void verifySplashScreenImagesAreDisplayed() {
        AssetRecorder assets = new AssetRecorder(page);
        SplashScreen splash = new SplashScreen(page);

        splash.launch();
        splash.waitForHtmlSplashLogoRendered();
        assertThat(splash.htmlSplashLogo()).isVisible();
        screenshot("html-splash");

        if (SplashScreen.FLUTTER_SPLASH_ROUTE.equals(splash.waitForFirstAppRoute())) {
            screenshot("flutter-splash");
        }
        String firstScreen = splash.waitUntilSplashFinished();
        screenshot("after-splash");
        log("Launch went through routes " + splash.routesSeen() + ", first screen " + firstScreen);

        List<Asset> splashImages = assets.where(SplashScreenTestBase::isSplashImage);
        Assert.assertFalse(splashImages.isEmpty(), "No images were loaded while the splash screen was shown");

        SoftAssert softly = new SoftAssert();
        assertImagesLoaded(softly, splashImages, "splash-images");
        softly.assertAll();
    }
}
