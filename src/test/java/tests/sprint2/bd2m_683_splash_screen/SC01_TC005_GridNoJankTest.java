package tests.sprint2.bd2m_683_splash_screen;

import config.DeviceProfile;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SC_01_TC_005 - Verify that the auto-scrolling image grid does not cause jank/frame drops while scrolling.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Expected:      Image grid should scroll without any jank/frame drops.
 * Status:        In Progress
 *
 * At the target device's screen size (Galaxy S24 Ultra 384 x 832, iPhone 17 Pro 402 x 874),
 * while the grid scrolls, records the time of every frame the browser draws for 5 seconds
 * (Flutter draws in these frames). A frame that takes over 50 ms (3 frames at 60 fps) is a
 * dropped frame. Allowed: at most 5% dropped, an average of 45 fps or more and no freeze over
 * 250 ms. The frame times are saved as frame-times.csv.
 *
 * Measured in an emulated phone on this PC while video is recorded, so a failure should be
 * confirmed on a real device; a pass is a good sign but not proof for slow phones.
 *
 * iOS: the emulated iPhone (WebKit on Windows) is slow at the WebGL drawing Flutter uses - an
 * empty full-screen WebGL animation runs at about 38 fps there against 60 in Chromium - so a
 * frame rate below the limits can't be blamed on the app. The test is then SKIPPED for a check
 * on a real iPhone; a frame rate within the limits still passes.
 */
public class SC01_TC005_GridNoJankTest extends SplashLandingTestBase {

    private static final int MEASURE_MS = 5_000;
    private static final double DROPPED_FRAME_MS = 50;
    private static final double MAX_DROPPED_SHARE = 0.05;
    private static final double MIN_AVERAGE_FPS = 45;
    private static final double MAX_FREEZE_MS = 250;

    /** Time between each pair of animation frames, over the given milliseconds. */
    private static final String FRAME_TIMES = """
        ms => new Promise(resolve => {
            const times = [];
            const end = performance.now() + ms;
            const tick = t => {
                times.push(t);
                if (t < end) requestAnimationFrame(tick);
                else resolve(times.slice(1).map((v, i) => v - times[i]));
            };
            requestAnimationFrame(tick);
        })
        """;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_005: Verify that the auto-scrolling image grid does not cause jank/frame drops while scrolling.")
    public void verifyImageGridScrollsWithoutJank() {
        SplashLandingScreen screen = openSplash();
        resizeToTargetDevice(screen);
        assertGridScrolling(screen, "before measuring");

        List<Double> frames = ((List<?>) page.evaluate(FRAME_TIMES, MEASURE_MS)).stream()
            .map(v -> ((Number) v).doubleValue()).toList();
        saveEvidence("frame-times.csv", ("frame,ms\n" + String.join("\n",
            java.util.stream.IntStream.range(0, frames.size())
                .mapToObj(i -> (i + 1) + "," + String.format("%.1f", frames.get(i))).toList()))
            .getBytes(StandardCharsets.UTF_8));

        double total = frames.stream().mapToDouble(Double::doubleValue).sum();
        double fps = frames.size() * 1000 / total;
        long dropped = frames.stream().filter(ms -> ms > DROPPED_FRAME_MS).count();
        double longest = frames.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        log(String.format("%d frames in %.1f s: %.1f fps average, %d dropped (> %.0f ms, %.1f%%), longest %.0f ms",
            frames.size(), total / 1000, fps, dropped, DROPPED_FRAME_MS, 100.0 * dropped / frames.size(), longest));

        boolean withinLimits = dropped <= MAX_DROPPED_SHARE * frames.size() && fps >= MIN_AVERAGE_FPS
            && longest <= MAX_FREEZE_MS;
        if (!withinLimits && device == DeviceProfile.IOS) {
            throw new SkipException(String.format("Not measurable on the emulated iPhone: %.1f fps here, but this PC's "
                + "WebKit is too slow at the WebGL drawing Flutter uses to judge the app - verify on a real %s",
                fps, targetDeviceName()));
        }

        Assert.assertTrue(dropped <= MAX_DROPPED_SHARE * frames.size(), String.format(
            "Jank: %d of %d frames dropped (%.1f%%, allowed %.0f%%)", dropped, frames.size(),
            100.0 * dropped / frames.size(), 100 * MAX_DROPPED_SHARE));
        Assert.assertTrue(fps >= MIN_AVERAGE_FPS, String.format(
            "Frame drops: grid animation ran at %.1f fps on average (minimum %.0f)", fps, MIN_AVERAGE_FPS));
        Assert.assertTrue(longest <= MAX_FREEZE_MS, String.format(
            "Jank: the screen froze for %.0f ms while the grid scrolled (allowed %.0f ms)", longest, MAX_FREEZE_MS));
    }
}
