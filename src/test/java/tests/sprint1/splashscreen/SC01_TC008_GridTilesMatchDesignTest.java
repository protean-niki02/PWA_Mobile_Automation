package tests.sprint1.splashscreen;

import com.microsoft.playwright.APIResponse;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.WelcomePage;
import support.ApprovedDesign;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SC_01_TC_008 - Verify that the updated grid tile images match the approved design.
 *
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Launch Icon & Splash Screen
 * Scenario:      SC_01 - Validate the "Launch Icon & Splash Screen" functionality.
 * Pre-requisite: User is on a screen displaying grid tile images
 * Test data:     Approved grid tile image design
 * Step 1:        Launch the app & check grid tile images on the onboarding/splash/login screens & compare with the approved design
 * Expected:      Updated grid tile images should match the approved design.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Test data: put the approved tile images in src/test/resources/approved-design/grid-tiles/,
 * named like the app's files (person_one.jpeg ... person_twentynine.jpeg). Skipped until
 * they are there. Only some tiles are on screen at a time, so each approved tile is
 * fetched from the folder the app loads its tiles from.
 */
public class SC01_TC008_GridTilesMatchDesignTest extends SplashScreenTestBase {

    @Test(groups = {"sprint1", "SC_01", "positive"},
        description = "SC_01_TC_008: Verify that the updated grid tile images match the approved design.")
    public void verifyGridTilesMatchApprovedDesign() {
        Map<String, byte[]> approved = ApprovedDesign.load("grid-tiles");

        AssetRecorder assets = new AssetRecorder(page);
        new WelcomePage(page).open();
        screenshot("onboarding-screen");
        new LoginPage(page).open();
        screenshot("login-screen");

        List<Asset> tiles = assets.matching(GRID_TILE_PATH);
        Assert.assertFalse(tiles.isEmpty(), "The app did not load any grid tile images");
        String tileFolder = tiles.get(0).url().substring(0, tiles.get(0).url().lastIndexOf('/') + 1);
        log("Grid tiles are served from " + tileFolder);

        Map<String, byte[]> appTiles = new LinkedHashMap<>();
        for (String name : approved.keySet()) {
            APIResponse response = context.request().get(tileFolder + name);
            if (response.ok()) {
                appTiles.put(name, response.body());
            } else {
                log(name + ": HTTP " + response.status() + " from " + tileFolder + name);
            }
        }

        assertMatchesApproved("SC_01_TC_008 grid tiles", approved, appTiles);
    }
}
