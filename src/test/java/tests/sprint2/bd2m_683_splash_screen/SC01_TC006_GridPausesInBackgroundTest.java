package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;
import support.GridMotion.Shift;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_006 - Verify that the auto-scroll of the image grid pauses/stops when the screen is backgrounded or not visible.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Expected:      Auto-scroll of the image grid should pause/stop when the screen is backgrounded or not visible.
 * Status:        In Progress
 *
 * At the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro 402 x 874),
 * measures each row's speed, puts the app in the background for 5 seconds and brings it back.
 * The app is told it is hidden the way a phone's browser tells it when the user switches apps
 * (see sendToBackground). If the auto-scroll paused, every row is where it was left (give or
 * take the second it takes to capture); if it kept running, the rows have moved on by about
 * 5 seconds' worth of scrolling. A real device switch is the final check.
 */
public class SC01_TC006_GridPausesInBackgroundTest extends SplashLandingTestBase {

    private static final long BACKGROUND_MS = 5_000;

    /** Movement allowed between leaving and returning: this many seconds of scrolling. */
    private static final double ALLOWED_SECONDS_OF_MOVEMENT = 1.5;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_006: Verify that the auto-scroll of the image grid pauses/stops when the screen is backgrounded or not visible.")
    public void verifyImageGridPausesInBackground() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        double[] speeds = assertGridScrolling(screen, "before backgrounding");

        Frame left = captureGrid(screen);
        log("Background: " + sendToBackground());
        page.waitForTimeout(BACKGROUND_MS);
        bringToForeground();
        Frame back = captureGrid(screen);
        screenshot("back-in-foreground");
        saveGridFrames("before-and-after-background", List.of(left, back));
        List<GridStep> after = steps(sampleGrid(screen, 3, 300));

        List<String> keptScrolling = new ArrayList<>();
        for (int row = 0; row < rowCount(); row++) {
            int r = row;
            Shift moved = rowShift(left, back, row);
            double usualError = after.stream().mapToDouble(s -> s.rows().get(r).error()).average().orElse(0);
            double allowed = Math.abs(speeds[row]) * ALLOWED_SECONDS_OF_MOVEMENT + 3;
            double ifRunning = Math.abs(speeds[row]) * BACKGROUND_MS / 1000.0;
            log(String.format("%s while in background for %.1f s: %s; allowed %.0f px if paused, about %.0f px if it kept running",
                ROW_NAMES.get(row), BACKGROUND_MS / 1000.0, moved, allowed, ifRunning));
            if (Math.abs(moved.pixels()) > allowed || moved.error() > Math.max(4, 2 * usualError)) {
                keptScrolling.add(String.format("%s (expected at most %.0f px of movement; about %.0f px if it never paused)",
                    ROW_NAMES.get(row), allowed, ifRunning));
            }
        }
        Assert.assertTrue(keptScrolling.isEmpty(), "The image grid kept scrolling while the app was in the background - "
            + "it did not resume from where it was left: " + keptScrolling);
    }
}
