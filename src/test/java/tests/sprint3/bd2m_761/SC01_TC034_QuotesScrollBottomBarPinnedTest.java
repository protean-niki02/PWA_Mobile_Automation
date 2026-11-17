package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ImageCompare;
import support.ScreenPixels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SC_01_TC_034 - Verify that the list scrolls smoothly with the bottom action bar pinned at the bottom.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Once the quotes are loaded, the list is scrolled down to its end in small steps and back to the top.
 * Throughout:
 * <ul>
 *   <li>the list must move: the area between the header and the bar changes with each scroll;</li>
 *   <li>the bottom action bar (Add ons / Sort / Filters) must stay pinned: same position (within 2 px),
 *       still at the bottom of the screen, and its pixels unchanged (at most 3% differ);</li>
 *   <li>at the end of the list the last quote card must not be hidden behind the bar (its
 *       "View Breakup" is above the bar);</li>
 *   <li>smoothness: frames are recorded (requestAnimationFrame) during a separate scroll down and back
 *       with no screenshots taken, since a screenshot pauses drawing; the page must not
 *       freeze - no gap between frames of {@value #MAX_FRAME_GAP_MS} ms or more. The frame rate is
 *       logged; it is the emulator's, so it is not held to a device figure.</li>
 * </ul>
 * Pictures of the bar and the list before / after scrolling are saved.
 */
public class SC01_TC034_QuotesScrollBottomBarPinnedTest extends QuotesListingTestBase {

    private static final int STEP_PX = 150;
    /** Steps of the pinned pass: bigger, and enough for a long list (the number of quotes varies per fetch). */
    private static final int PINNED_STEP_PX = 300;
    private static final int MAX_STEPS = 60;
    private static final double MAX_BAR_DIFF = 0.03;
    private static final double MIN_LIST_DIFF = 0.02;
    private static final int MAX_FRAME_GAP_MS = 500;

    private static final String FRAME_RECORDER = """
        () => {
            window.__frames = [];
            window.__recording = true;
            const tick = t => { window.__frames.push(t); if (window.__recording) requestAnimationFrame(tick); };
            requestAnimationFrame(tick);
        }
        """;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_034: Verify that the list scrolls smoothly with the bottom action bar pinned at the bottom.")
    public void verifyScrollWithPinnedBottomBar() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        BoundingBox bar = barRow(quotes);
        Assert.assertNotNull(bar, "The bottom action bar (Add ons / Sort / Filters) is not shown. Screen text: " + quotes.semanticsText());
        double viewportHeight = page.viewportSize().height;
        double listTop = 130;
        double listHeight = bar.y - 20 - listTop;
        byte[] barStart = barPixels(bar);
        saveEvidence("bar-before-scroll.png", barStart);
        saveEvidence("list-top.png", ScreenPixels.capture(page, 0, listTop, page.viewportSize().width, listHeight));
        log(String.format("Bottom bar at y %.0f-%.0f of %.0f", bar.y, bar.y + bar.height, viewportHeight));

        // Smoothness pass: scroll down and back with nothing else going on - screenshots pause drawing
        // (on WebKit especially), so frames are only counted while no screenshot is taken
        page.evaluate(FRAME_RECORDER);
        for (int step = 0; step < 12; step++) {
            quotes.scrollDown(STEP_PX);
        }
        for (int step = 0; step < 12; step++) {
            quotes.scrollDown(-STEP_PX);
        }
        Map<String, Double> frames = frameStats();
        quotes.scrollToTop();

        // Pinned pass: scroll step by step, checking the bar and that the list moves
        SoftAssert softly = new SoftAssert();
        List<String> barMoves = new ArrayList<>();
        int moves = 0;
        int steps = 0;
        boolean reachedEnd = false;
        byte[] list = listPixels(listTop, listHeight);
        for (; steps < MAX_STEPS; steps++) {
            quotes.scrollDown(PINNED_STEP_PX);
            byte[] now = listPixels(listTop, listHeight);
            boolean moved = ImageCompare.compare(list, now, MIN_LIST_DIFF).diffRatio() >= MIN_LIST_DIFF;
            list = now;
            checkPinned(quotes, bar, barStart, "after scroll step " + (steps + 1), barMoves);
            if (!moved) {
                reachedEnd = true;
                break;
            }
            moves++;
        }
        saveEvidence("list-end.png", ScreenPixels.capture(page, 0, listTop, page.viewportSize().width, listHeight));
        screenshot("scrolled-to-end");

        Locator lastBreakup = page.getByText(QuotesListingPage.VIEW_BREAKUP).last();
        BoundingBox last = lastBreakup.count() > 0 ? lastBreakup.boundingBox() : null;
        // Only a "View Breakup" that is on screen can be hidden behind the bar
        boolean lastOnScreen = last != null && last.y < viewportHeight;
        boolean lastAboveBar = lastOnScreen && last.y + last.height <= bar.y;
        log(String.format("Scrolled %d step(s) of %d px, list moved on %d, end of list reached: %s; last \"View Breakup\" %s",
            steps, PINNED_STEP_PX, moves, reachedEnd,
            last == null ? "not found" : String.format("at y %.0f-%.0f (bar top %.0f)", last.y, last.y + last.height, bar.y)));

        quotes.scrollToTop();
        checkPinned(quotes, bar, barStart, "after scrolling back to the top", barMoves);
        screenshot("back-to-top");
        log(String.format("Frames while scrolling: %.0f, %.1f fps on average, longest gap %.0f ms, %.0f slow frame(s) (>50 ms)",
            frames.get("count"), frames.get("fps"), frames.get("maxGap"), frames.get("slow")));

        softly.assertTrue(moves >= 2, "The quotes list did not scroll (moved on " + moves + " of " + steps + " scroll steps)");
        softly.assertTrue(barMoves.isEmpty(), "The bottom action bar is not pinned while the list scrolls: " + barMoves);
        softly.assertTrue(reachedEnd, "The end of the quotes list was not reached after " + steps + " scroll steps of "
            + PINNED_STEP_PX + " px - the list kept moving");
        softly.assertTrue(!reachedEnd || !lastOnScreen || lastAboveBar, String.format(
            "At the end of the list the last quote card is hidden behind the bottom bar (its View Breakup ends at y %.0f, bar starts at %.0f)",
            last == null ? 0 : last.y + last.height, bar.y));
        softly.assertTrue(frames.get("maxGap") < MAX_FRAME_GAP_MS, String.format(
            "Scrolling is not smooth: the page froze for %.0f ms (allowed below %d ms)", frames.get("maxGap"), MAX_FRAME_GAP_MS));
        softly.assertAll();
    }

    /** The bar's row: from the top of its highest button to the bottom of its lowest (Add ons / Sort / Filters). */
    private static BoundingBox barRow(QuotesListingPage quotes) {
        BoundingBox filters = quotes.bottomBar();
        if (filters == null) {
            return null;
        }
        BoundingBox row = new BoundingBox();
        row.x = 0;
        row.y = filters.y - 12;
        row.width = 0;
        row.height = filters.height + 24;
        for (String item : List.of(QuotesListingPage.ADD_ONS, QuotesListingPage.SORT)) {
            Locator button = quotes.button(item);
            BoundingBox box = button.count() > 0 ? button.boundingBox() : null;
            if (box != null) {
                double bottom = Math.max(row.y + row.height, box.y + box.height + 12);
                row.y = Math.min(row.y, box.y - 12);
                row.height = bottom - row.y;
            }
        }
        return row;
    }

    /** Records why the bar is not pinned at this moment: moved, left the bottom, or its pixels changed. */
    private void checkPinned(QuotesListingPage quotes, BoundingBox start, byte[] startPixels, String when, List<String> problems) {
        BoundingBox now = barRow(quotes);
        if (now == null) {
            problems.add("bar gone " + when);
            return;
        }
        if (Math.abs(now.y - start.y) > 2) {
            problems.add(String.format("bar moved from y %.0f to %.0f %s", start.y, now.y, when));
        }
        if (now.y + now.height > page.viewportSize().height + 2 || now.y < page.viewportSize().height - 200) {
            problems.add(String.format("bar not at the bottom (y %.0f) %s", now.y, when));
        }
        double diff = ImageCompare.compare(startPixels, barPixels(start), MAX_BAR_DIFF).diffRatio();
        if (diff > MAX_BAR_DIFF) {
            problems.add(String.format("bar pixels changed %.1f%% %s", diff * 100, when));
            saveEvidence("bar-changed.png", barPixels(start));
        }
    }

    private byte[] barPixels(BoundingBox bar) {
        return ScreenPixels.capture(page, 0, bar.y, page.viewportSize().width, bar.height);
    }

    private byte[] listPixels(double top, double height) {
        return ScreenPixels.capture(page, 0, top, page.viewportSize().width, height);
    }

    /** Stops the frame recorder and returns count, fps, maxGap (ms) and slow (frames over 50 ms). */
    @SuppressWarnings("unchecked")
    private Map<String, Double> frameStats() {
        Map<String, Object> raw = (Map<String, Object>) page.evaluate("""
            () => {
                window.__recording = false;
                const f = window.__frames || [];
                let maxGap = 0, slow = 0;
                for (let i = 1; i < f.length; i++) {
                    const gap = f[i] - f[i - 1];
                    maxGap = Math.max(maxGap, gap);
                    if (gap > 50) slow++;
                }
                const span = f.length > 1 ? f[f.length - 1] - f[0] : 0;
                return { count: f.length, fps: span > 0 ? (f.length - 1) * 1000 / span : 0, maxGap: maxGap, slow: slow };
            }
            """);
        Map<String, Double> stats = new java.util.HashMap<>();
        raw.forEach((key, value) -> stats.put(key, value instanceof Number n ? n.doubleValue() : 0.0));
        return stats;
    }
}
