package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import support.AssetRecorder;
import support.AssetRecorder.Asset;

import java.util.List;

/**
 * SC_01_TC_022 - Verify that images in the image grid load correctly from the configured source.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     splash.gridImageSource in config.properties (the CMS: https://cms-bucket-uat.bimasugam.co.in/s3fs-public/)
 * Step 1:        Launch the app & land on the splash screen & check images in the image grid
 * Expected:      Images in the image grid should load correctly from the configured source.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * The grid photos (person_*.jpeg) come from the CMS; the app also bundles its own copies under
 * /assets/assets/images/onboarding/grid_image/ as a fallback, which don't count here. Grid photos
 * must be loaded from the configured source, each arriving without error as a real, displayable
 * image. All of them are saved and shown on one sheet.
 */
public class SC01_TC022_GridImagesLoadFromSourceTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_022: Verify that images in the image grid load correctly from the configured source.")
    public void verifyGridImagesLoadFromConfiguredSource() {
        String source = gridImageSource();
        AssetRecorder assets = new AssetRecorder(page);
        openSplash();

        List<Asset> photos = assets.where(SplashLandingTestBase::isGridPhoto);
        List<Asset> fromSource = photos.stream().filter(a -> a.url().startsWith(source)).toList();
        List<Asset> bundled = photos.stream().filter(a -> a.url().contains(GRID_TILE_PATH)).toList();
        log(String.format("Grid photos: %d from the configured source %s, %d bundled copies (fallback), %d elsewhere",
            fromSource.stream().map(Asset::url).distinct().count(), source, bundled.stream().map(Asset::url).distinct().count(),
            photos.stream().filter(a -> !fromSource.contains(a) && !bundled.contains(a)).map(Asset::url).distinct().count()));

        Assert.assertFalse(fromSource.isEmpty(), "No image grid photos were loaded from the configured source " + source
            + "; grid photos came from: " + photos.stream().map(Asset::url).distinct().toList());
        SoftAssert softly = new SoftAssert();
        assertImagesLoaded(softly, fromSource, "grid-images");
        softly.assertAll();
    }
}
