package tests.sprint2.bd2m_683_splash_screen;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ReducedMotion;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_027 - Verify that the auto-scroll of the image grid respects the device's reduced-motion setting.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Test data:     Device with reduced-motion accessibility setting enabled
 * Expected:      Auto-scroll of the image grid should respect the device's reduced-motion setting by slowing or stopping the animation.
 * Status:        In Progress
 *
 * At the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro 402 x 874),
 * measures each row's normal speed, then switches on reduced motion (the browser passes the
 * phone's setting to the app as "prefers-reduced-motion: reduce") and relaunches the app.
 * Every row must then stop, or scroll at half its normal speed or less.
 */
public class SC01_TC027_GridRespectsReducedMotionTest extends SplashLandingTestBase {

    private static final double MAX_REDUCED_SPEED_SHARE = 0.5;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_027: Verify that the auto-scroll of the image grid respects the device's reduced-motion setting.")
    public void verifyImageGridRespectsReducedMotion() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        double[] normal = assertGridScrolling(screen, "with normal motion");

        page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.REDUCE));
        page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        screen.enableSemantics();
        screen.waitForLanding();
        log("Reduced motion on: " + page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
        screenshot("reduced-motion-on");

        List<Frame> frames = sampleGrid(screen, 6, 500);
        saveGridFrames("image-grid-reduced-motion", frames);
        List<GridStep> steps = steps(frames);
        double[] reduced = new double[normal.length];
        List<String> ignored = new ArrayList<>();
        for (int row = 0; row < normal.length; row++) {
            int r = row;
            boolean stopped = steps.stream().noneMatch(s -> s.rows().get(r).moved());
            reduced[row] = stopped ? 0 : rowSpeeds(steps)[row];
            if (Math.abs(reduced[row]) > MAX_REDUCED_SPEED_SHARE * Math.abs(normal[row])) {
                ignored.add(String.format("%s still scrolls at %.1f px/s (normally %.1f px/s)",
                    ROW_NAMES.get(row), Math.abs(reduced[row]), Math.abs(normal[row])));
            }
        }
        log("Grid speed normally: " + describeSpeeds(normal) + "; with reduced motion: " + describeSpeeds(reduced));

        Assert.assertTrue(ignored.isEmpty(), "The image grid ignores the reduced-motion setting: " + ignored);
    }
}
