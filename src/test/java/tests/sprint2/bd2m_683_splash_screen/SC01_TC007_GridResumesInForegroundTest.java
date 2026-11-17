package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;

import java.util.List;

/**
 * SC_01_TC_007 - Verify that the auto-scroll of the image grid resumes correctly when the app is brought back to foreground.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Expected:      Auto-scroll of the image grid should resume correctly when the app is brought back to foreground.
 * Status:        In Progress
 *
 * At the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro 402 x 874),
 * measures each row's speed, backgrounds the app for 3 seconds (told it is hidden the way a
 * phone's browser does - see sendToBackground), brings it back and measures again: every row
 * must be scrolling in the same direction at about the same speed (within 50%).
 */
public class SC01_TC007_GridResumesInForegroundTest extends SplashLandingTestBase {

    private static final long BACKGROUND_MS = 3_000;
    private static final double MAX_SPEED_CHANGE = 0.5;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_007: Verify that the auto-scroll of the image grid resumes correctly when the app is brought back to foreground.")
    public void verifyImageGridResumesInForeground() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        double[] before = assertGridScrolling(screen, "before backgrounding");

        log("Background: " + sendToBackground());
        page.waitForTimeout(BACKGROUND_MS);
        bringToForeground();
        page.waitForTimeout(1_000);
        screenshot("back-in-foreground");

        List<Frame> frames = sampleGrid(screen, 6, 400);
        saveGridFrames("image-grid-after-resume", frames);
        List<GridStep> steps = steps(frames);
        Assert.assertTrue(steps.stream().allMatch(GridStep::moved),
            "The image grid did not resume scrolling after the app came back to the foreground");

        double[] after = rowSpeeds(steps);
        log("Speed before background: " + describeSpeeds(before) + "; after: " + describeSpeeds(after));
        SoftAssert softly = new SoftAssert();
        for (int row = 0; row < rowCount(); row++) {
            String name = ROW_NAMES.get(row);
            softly.assertEquals(Math.signum(after[row]), Math.signum(before[row]),
                "The image grid's " + name + " resumed in the opposite direction");
            softly.assertTrue(Math.abs(after[row] - before[row]) <= MAX_SPEED_CHANGE * Math.abs(before[row]), String.format(
                "The image grid's %s resumed at a different speed: %.1f px/s before, %.1f px/s after", name, before[row], after[row]));
        }
        softly.assertAll();
    }
}
