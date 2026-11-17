package tests.sprint1.splashscreen;

import org.testng.annotations.Test;
import pages.SplashScreen;
import support.ApprovedDesign;
import support.AssetRecorder;

import java.util.Map;

/**
 * SC_01_TC_004 - Verify that the updated splash screen images match the approved design.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: Mobile application is installed on the device
 * Test data:     Approved splash screen design
 * Step 1:        Launch the app & check splash screen images & compare with the approved design
 * Expected:      Updated splash screen images should match the approved design.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Test data: put the approved splash images in src/test/resources/approved-design/splash-screen/,
 * named like the app's files (e.g. bima_sugam_logo.png). Each must be an image the app
 * loads during the splash. Skipped until they are there.
 */
public class SC01_TC004_SplashScreenMatchesDesignTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_004: Verify that the updated splash screen images match the approved design.")
    public void verifySplashScreenImagesMatchApprovedDesign() {
        Map<String, byte[]> approved = ApprovedDesign.load("splash-screen");

        AssetRecorder assets = new AssetRecorder(page);
        SplashScreen splash = new SplashScreen(page);
        splash.launch();
        splash.waitForHtmlSplashLogoRendered();
        screenshot("html-splash");
        if (SplashScreen.FLUTTER_SPLASH_ROUTE.equals(splash.waitForFirstAppRoute())) {
            screenshot("flutter-splash");
        }
        splash.waitUntilSplashFinished();

        Map<String, byte[]> splashImages = byFileName(assets.where(SplashScreenTestBase::isSplashImage));
        assertMatchesApproved("SC_01_TC_004 splash screen", approved, splashImages);
    }
}
