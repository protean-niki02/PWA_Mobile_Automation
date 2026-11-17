package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;

import java.util.List;

/**
 * SC_01_TC_001 - Verify that the image grid at the top of the splash screen auto-scrolls horizontally and continuously.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: User has launched the Mobile application
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & observe the image grid
 * Expected:      Image grid should auto-scroll horizontally and continuously.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Ten frames of the grid about half a second apart: in every interval each row of photos must
 * have shifted sideways (horizontally), each row always in the same direction, without the
 * user touching anything.
 */
public class SC01_TC001_GridAutoScrollsTest extends SplashLandingTestBase {

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_001: Verify that the image grid at the top of the splash screen auto-scrolls horizontally and continuously.")
    public void verifyImageGridAutoScrollsHorizontally() {
        SplashLandingScreen screen = openSplash();

        List<Frame> frames = sampleGrid(screen, 10, 500);
        saveGridFrames("image-grid", frames);
        List<GridStep> steps = steps(frames);
        log("Grid speed: " + describeSpeeds(rowSpeeds(steps)));

        long stopped = steps.stream().filter(s -> !s.moved()).count();
        Assert.assertEquals(stopped, 0L, "The image grid did not move horizontally in " + stopped + " of "
            + steps.size() + " intervals - it is not auto-scrolling continuously");
        for (int row = 0; row < rowCount(); row++) {
            int r = row;
            long rightward = steps.stream().filter(s -> s.rows().get(r).pixels() > 0).count();
            Assert.assertTrue(rightward == 0 || rightward == steps.size(),
                "The image grid's " + ROW_NAMES.get(row) + " changed direction while scrolling");
        }
    }
}
