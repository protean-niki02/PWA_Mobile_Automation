package tests.sprint2.bd2m_683_splash_screen;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SplashLandingScreen;
import support.GridMotion.Frame;
import support.GridMotion.Shift;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SC_01_TC_002 - Verify that the auto-scrolling image grid loops seamlessly without any visible jump or reset.
 *
 * Sprint:        Sprint 2 - BD2M-683
 * Module:        Customer Creation & other functionality
 * Requirement:   US-UCM-01 - Splash Screen
 * Scenario:      SC_01 - Validate the "Splash Screen" functionality.
 * Pre-requisite: Splash screen is displayed
 * Test data:     None
 * Step 1:        Launch the app & land on the splash screen & observe the image grid over one complete loop
 * Expected:      Image grid should loop seamlessly without any visible jump or reset.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Watches the grid frame by frame until each row of photos shows its first frame's picture
 * again (one complete loop), for up to 150 seconds - the rows move at different speeds, so
 * they complete their loops at different times. A row only counts as back at its start after
 * travelling more than twice the distance one comparison covers. Every step between frames must be the row's
 * usual steady sideways movement; a jump or reset shows as a step that stops, reverses, is
 * much bigger than the others, or no longer lines up with the frame before.
 */
public class SC01_TC002_GridLoopsSeamlesslyTest extends SplashLandingTestBase {

    private static final long MAX_OBSERVE_MS = 150_000;

    /** A step whose speed is off the row's usual speed by more than this share is a jump. */
    private static final double MAX_STEP_SPEED_DEVIATION = 0.5;

    @Test(groups = {"sprint2", "BD2M-683", "SC_01", "positive"},
        description = "SC_01_TC_002: Verify that the auto-scrolling image grid loops seamlessly without any visible jump or reset.")
    public void verifyImageGridLoopsSeamlessly() {
        SplashLandingScreen screen = openSplash();
        Frame first = captureGrid(screen);
        int rows = rowCount();
        List<Frame> evidence = new ArrayList<>(List.of(first));
        List<GridStep> steps = new ArrayList<>();
        List<Long> stepTimes = new ArrayList<>();
        // A row only counts as back at its start once it has travelled well beyond the
        // distance a single comparison can line up, or plain movement would look like a loop
        double loopAfterPx = 2 * maxMeasurableShift();
        double[] travelled = new double[rows];
        Long[] loopAt = new Long[rows];
        Frame previous = first;
        while (previous.atMs() - first.atMs() < MAX_OBSERVE_MS && Arrays.stream(loopAt).anyMatch(t -> t == null)) {
            page.waitForTimeout(250);
            Frame frame = captureGrid(screen);
            long elapsed = frame.atMs() - first.atMs();
            GridStep step = step(previous, frame);
            steps.add(step);
            stepTimes.add(elapsed);
            for (int row = 0; row < rows; row++) {
                travelled[row] += Math.abs(step.rows().get(row).pixels());
            }
            previous = frame;
            if (steps.size() % 20 == 0) {
                evidence.add(frame);
            }

            for (int row = 0; row < rows; row++) {
                if (loopAt[row] != null || travelled[row] < loopAfterPx) {
                    continue;
                }
                int r = row;
                double usualError = median(steps.stream().map(s -> s.rows().get(r).error()).toList());
                Shift back = rowShift(first, frame, row);
                if (back.error() <= Math.max(2, 1.5 * usualError)) {
                    loopAt[row] = elapsed;
                    evidence.add(frame);
                    saveEvidence(ROW_NAMES.get(row).replace(' ', '-') + "_loop-complete.png", frame.png());
                    log(String.format("%s back to its first picture after %.1f s, having travelled %.0f px (%s)",
                        ROW_NAMES.get(row), elapsed / 1000.0, travelled[row], back));
                }
            }
        }
        saveEvidence("loop-start.png", first.png());
        saveGridFrames("image-grid", evidence);
        log(steps.size() + " steps observed; grid speed " + describeSpeeds(rowSpeeds(steps)));

        for (int row = 0; row < rows; row++) {
            Assert.assertNotNull(loopAt[row], "The image grid's " + ROW_NAMES.get(row) + " did not come back to its first "
                + "picture within " + MAX_OBSERVE_MS / 1000 + "s, so no complete loop was seen (it may loop more slowly, or never repeat)");
        }

        double[] usualSpeed = rowSpeeds(steps);
        List<String> jumps = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            int r = row;
            double usualError = median(steps.stream().map(s -> s.rows().get(r).error()).toList());
            for (int i = 0; i < steps.size(); i++) {
                Shift step = steps.get(i).rows().get(row);
                // A capture so slow that the row went further than one comparison can follow
                if (Math.abs(usualSpeed[row]) * step.elapsedMs() / 1000.0 > 0.8 * maxMeasurableShift()) {
                    log(String.format("%s at %.1f s: step of %d ms too long to measure - left out",
                        ROW_NAMES.get(row), stepTimes.get(i) / 1000.0, step.elapsedMs()));
                    continue;
                }
                String problem = !step.moved() ? "stopped"
                    : Math.signum(step.speed()) != Math.signum(usualSpeed[row]) ? "moved backwards"
                    : Math.abs(step.speed() - usualSpeed[row]) > MAX_STEP_SPEED_DEVIATION * Math.abs(usualSpeed[row]) ? "jumped"
                    : step.error() > Math.max(6, 3 * usualError) ? "picture changed abruptly (reset)"
                    : null;
                if (problem != null) {
                    jumps.add(String.format("%s at %.1f s: %s - %s", ROW_NAMES.get(row), stepTimes.get(i) / 1000.0, problem, step));
                }
            }
        }
        jumps.forEach(jump -> log("Jump/reset: " + jump));
        Assert.assertTrue(jumps.isEmpty(), "The image grid did not loop seamlessly - " + jumps.size()
            + " visible jump(s)/reset(s): " + jumps);
    }
}
