package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Route;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.AssetRecorder;
import support.AssetRecorder.Asset;
import support.ScreenPixels;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SC_01_TC_023 - Verify that a graceful fallback is displayed if an image in the image grid fails to load.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: An image in the image grid fails to load
 * Test data:     Image grid with a failed image source (splash.gridImageSource, the CMS)
 * Step 1:        Launch the app & land on the splash screen when an image fails to load
 * Expected:      A graceful fallback/placeholder should be displayed for the image that fails to load without any broken tile.
 * Type:          Negative
 * Designed by:   Sumit
 *
 * Every grid photo request to the configured source (the CMS) is answered with "404 Not Found",
 * so every photo the grid normally shows has failed. Checked:
 * <ul>
 *   <li>the app does not crash: no uncaught errors, and the splash screen still comes up;</li>
 *   <li>the grid area is not left blank: a fallback (the app's bundled copies, or placeholders)
 *       draws pictures or tile shapes, so its brightness varies (spread of 8 or more), where
 *       empty tiles leave a flat background.</li>
 * </ul>
 * Whether the app's bundled copies were used as the fallback is logged. The grid area is saved
 * as image-grid-with-failed-images.png; look at it to judge whether the fallback looks
 * graceful, as a picture check cannot judge style.
 */
public class SC01_TC023_GridImageFailureFallbackTest extends SplashLandingTestBase {

    private static final double MIN_PLACEHOLDER_SPREAD = 8;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "negative"},
        description = "SC_01_TC_023: Verify that a graceful fallback is displayed if an image in the image grid fails to load.")
    public void verifyFallbackWhenGridImageFails() {
        String source = gridImageSource();
        List<String> failedImages = new CopyOnWriteArrayList<>();
        List<String> pageErrors = new CopyOnWriteArrayList<>();
        page.onPageError(pageErrors::add);
        page.route(url -> url.startsWith(source) && url.substring(url.lastIndexOf('/') + 1).toLowerCase().startsWith("person_"),
            route -> {
                failedImages.add(route.request().url());
                route.fulfill(new Route.FulfillOptions().setStatus(404).setBody(""));
            });
        AssetRecorder assets = new AssetRecorder(page);

        SplashLandingScreen screen = openSplash();
        page.waitForTimeout(2_000);
        Assert.assertFalse(failedImages.isEmpty(), "The splash screen requested no grid photos from " + source
            + ", so an image failure could not be simulated");
        log(failedImages.size() + " grid photo request(s) to the configured source failed on purpose (404), e.g. "
            + failedImages.get(0));
        List<String> fallbacks = assets.where(a -> isGridPhoto(a) && a.url().contains(GRID_TILE_PATH) && a.ok())
            .stream().map(Asset::fileName).distinct().toList();
        log("Bundled fallback copies loaded: " + (fallbacks.isEmpty() ? "none" : fallbacks.size() + " " + fallbacks));

        byte[] grid = captureUnlessBlank(0, 0, page.viewportSize().width, screen.gridBottom()).png();
        saveEvidence("image-grid-with-failed-images.png", grid);
        screenshot("splash-with-failed-images");
        double spread = ScreenPixels.brightnessSpread(grid);
        log(String.format("Grid area brightness spread %.1f (fallback drawn: %.0f or more); page errors: %s",
            spread, MIN_PLACEHOLDER_SPREAD, pageErrors));

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(pageErrors.isEmpty(), "The app threw errors when grid photos failed to load: " + pageErrors);
        softly.assertTrue(screen.isOnScreen(screen.welcomeHeading()),
            "The splash screen did not display properly when grid photos failed to load");
        softly.assertTrue(spread >= MIN_PLACEHOLDER_SPREAD, String.format("The image grid is blank when its photos fail "
            + "to load (brightness spread %.1f) - failed tiles are left empty instead of showing a fallback/placeholder", spread));
        softly.assertAll();
    }
}
