package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;

import java.util.List;

/**
 * SC_01_TC_003 - Verify that the image grid scrolls at a smooth and consistent speed.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & observe the image grid scroll speed
 * Expected:      Image grid should scroll at a smooth and consistent speed.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Measures each row's speed over eleven intervals of about a second (the rows have speeds of
 * their own, by design). Each row's speeds must stay close together: their spread (standard
 * deviation) within 25% of the row's average, and no single interval more than 50% off it.
 * Interval timing on this PC adds a few % of noise.
 */
public class SC01_TC003_GridScrollSpeedConsistentTest extends SplashLandingTestBase {

    private static final double MAX_SPEED_SPREAD = 0.25;
    private static final double MAX_SINGLE_DEVIATION = 0.5;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_003: Verify that the image grid scrolls at a smooth and consistent speed.")
    public void verifyImageGridScrollSpeedIsConsistent() {
        SplashLandingScreen screen = openSplash();

        List<Frame> frames = sampleGrid(screen, 12, 800);
        saveGridFrames("image-grid", frames);
        List<GridStep> steps = steps(frames);
        Assert.assertTrue(steps.stream().allMatch(GridStep::moved),
            "The image grid stopped during the measurement, so its speed is not consistent");

        SoftAssert softly = new SoftAssert();
        for (int row = 0; row < rowCount(); row++) {
            int r = row;
            String name = ROW_NAMES.get(row);
            List<Double> speeds = steps.stream().map(s -> Math.abs(s.rows().get(r).speed())).toList();
            double mean = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double spread = Math.sqrt(speeds.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0));
            double worst = speeds.stream().mapToDouble(v -> Math.abs(v - mean)).max().orElse(0);
            log(String.format("%s speeds (px/s): %s - average %.1f, spread %.1f (%.0f%%), largest deviation %.1f (%.0f%%)",
                name, speeds.stream().map(v -> String.format("%.1f", v)).toList(), mean, spread, 100 * spread / mean,
                worst, 100 * worst / mean));

            softly.assertTrue(spread <= MAX_SPEED_SPREAD * mean, String.format(
                "Grid %s speed is not consistent: varies by %.0f%% (allowed %.0f%%)", name, 100 * spread / mean, 100 * MAX_SPEED_SPREAD));
            softly.assertTrue(worst <= MAX_SINGLE_DEVIATION * mean, String.format(
                "Grid %s speed is not smooth: one interval was %.0f%% off its average speed (allowed %.0f%%)",
                name, 100 * worst / mean, 100 * MAX_SINGLE_DEVIATION));
        }
        softly.assertAll();
    }
}
