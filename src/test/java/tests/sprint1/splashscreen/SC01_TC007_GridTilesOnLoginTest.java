package tests.sprint1.splashscreen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.LoginPage;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * SC_01_TC_007 - Verify that the updated grid tile images are displayed on the login screen.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on the login screen
 * Test data:     None
 * Step 1:        Launch the app & navigate to the login screen & check grid tile images
 * Expected:      Updated grid tile images should be displayed on the login screen.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Flutter downloads each image once per session, so tiles already shown on an earlier
 * screen are not downloaded again on login; the check covers the whole session up to
 * the login screen, and the screenshot shows what login displays.
 */
public class SC01_TC007_GridTilesOnLoginTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_007: Verify that the updated grid tile images are displayed on the login screen.")
    public void verifyGridTilesOnLoginScreen() {
        AssetRecorder assets = new AssetRecorder(page);
        LoginPage login = new LoginPage(page).open();
        assertThat(login.mobileNumberField()).isVisible();
        screenshot("login-screen");

        List<Asset> tiles = assets.matching(GRID_TILE_PATH);
        Assert.assertFalse(tiles.isEmpty(), "No grid tile images were loaded up to the login screen");

        SoftAssert softly = new SoftAssert();
        assertImagesLoaded(softly, tiles, "grid-tiles-login");
        softly.assertAll();
    }
}
