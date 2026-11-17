package tests.sprint2.bd2m_683_splash_screen;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;
import support.ScreenPixels;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SC_01_TC_028 - Verify that the splash screen renders correctly on the Mobile application.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Expected:      Splash screen should render correctly on the Mobile application.
 * Status:        In Progress
 *
 * The overall check, at the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17
 * Pro 402 x 874): the app starts without errors and the splash screen is drawn complete - not
 * blank, heading and action button shown, every grid photo loaded, nothing wider than the
 * screen. The details of each part are covered by SC_01_TC_001 to TC_027; how many action
 * buttons there should be is SC_01_TC_013.
 */
public class SC01_TC028_SplashRendersCorrectlyTest extends SplashLandingTestBase {

    /** A blank or half-drawn screen is nearly flat; a drawn one varies far more than this. */
    private static final double MIN_SCREEN_SPREAD = 20;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_028: Verify that the splash screen renders correctly on the Mobile application.")
    @SuppressWarnings("unchecked")
    public void verifySplashScreenRendersCorrectly() {
        AssetRecorder assets = new AssetRecorder(page);
        List<String> pageErrors = new CopyOnWriteArrayList<>();
        page.onPageError(pageErrors::add);
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        screenshot("target-device-size");

        double spread = ScreenPixels.brightnessSpread(page.screenshot());
        int buttons = screen.actionButtons().size();
        List<Asset> tiles = assets.where(SplashLandingTestBase::isGridPhoto);
        List<String> failedTiles = tiles.stream().filter(t -> !t.ok()).map(t -> t.fileName() + " HTTP " + t.status()).toList();
        List<Number> widths = (List<Number>) page.evaluate("() => [document.documentElement.scrollWidth, innerWidth]");
        log(String.format("Route #%s; screen brightness spread %.1f; %d action buttons; %d grid images (%d failed); "
                + "content width %s of %s px; page errors %s",
            screen.route(), spread, buttons, tiles.size(), failedTiles.size(), widths.get(0), widths.get(1), pageErrors));

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(pageErrors.isEmpty(), "The app threw errors while showing the splash screen: " + pageErrors);
        softly.assertTrue(spread >= MIN_SCREEN_SPREAD, "The splash screen is blank or not fully drawn");
        softly.assertTrue(screen.isOnScreen(screen.welcomeHeading()), "\"" + SplashLandingScreen.WELCOME_HEADING
            + "\" heading is not displayed");
        softly.assertTrue(buttons >= 1, "No action button is displayed on the splash screen");
        softly.assertFalse(tiles.isEmpty(), "No image grid images were loaded");
        softly.assertTrue(failedTiles.isEmpty(), "Image grid images failed to load: " + failedTiles);
        softly.assertTrue(widths.get(0).doubleValue() <= widths.get(1).doubleValue() + 1,
            "The splash screen is wider than the phone screen (" + widths.get(0) + " of " + widths.get(1) + " px)");
        softly.assertAll();
    }
}
