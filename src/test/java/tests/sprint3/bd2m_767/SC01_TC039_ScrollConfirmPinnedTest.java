package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_039 - Verify that long content scrolls with "Confirm Plan" pinned at the bottom.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The scrolling area of Confirm Plan is scrolled down in steps to its end and back up. Throughout, the
 * content must move while the "Confirm Plan" button stays pinned: same position on the screen (within
 * 2 px), still fully inside the screen. At the end the last content, "Go Back to Quotes", must be reachable
 * and not hidden behind the button. Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC039_ScrollConfirmPinnedTest extends ConfirmPlanWebTestBase {

    private static final int STEP_PX = 300;
    private static final int MAX_STEPS = 60;

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_039: Verify that long content scrolls with \"Confirm Plan\" pinned at the bottom.")
    public void verifyScrollWithConfirmPinned() {
        openConfirmPlan();
        Locator area = scrollArea();
        area.evaluate("e => { e.scrollTop = 0; }");
        page.waitForTimeout(500);
        Locator button = confirmPlanButton();
        Assert.assertTrue(button.count() > 0, "No \"Confirm Plan\" button on the screen. Screen text: " + bodyText());
        BoundingBox start = button.boundingBox();
        double height = page.viewportSize().height;
        log(String.format("Confirm Plan button at the top of the content: y %.0f-%.0f of a %.0f px screen; scroll area %s", start.y,
            start.y + start.height, height, area.evaluate("e => e.scrollHeight + ' px of content in ' + e.clientHeight + ' px'")));
        screenshot("before-scroll");

        List<String> problems = new ArrayList<>();
        double lastTop = 0;
        int steps = 0, moves = 0, still = 0;
        boolean reachedEnd = false;
        for (; steps < MAX_STEPS; steps++) {
            double top = ((Number) area.evaluate("(e, d) => { e.scrollTop += d; return e.scrollTop; }", STEP_PX)).doubleValue();
            page.waitForTimeout(150);
            boolean moved = top > lastTop + 1;
            lastTop = top;
            checkPinned(start, "after scroll step " + (steps + 1), problems);
            still = moved ? 0 : still + 1;
            moves += moved ? 1 : 0;
            if (still >= 2) {
                reachedEnd = true;
                break;
            }
        }
        Locator last = page.getByText(GO_BACK_TO_QUOTES, new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).first();
        BoundingBox lastBox = last.count() > 0 ? last.boundingBox() : null;
        BoundingBox buttonNow = button.boundingBox();
        boolean lastVisible = lastBox != null && lastBox.y >= 0 && lastBox.y + lastBox.height <= height;
        boolean covered = lastBox != null && Boolean.TRUE.equals(last.evaluate(
            "e => { const r = e.getBoundingClientRect(); const t = document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2);"
                + " return !(t && (e === t || e.contains(t))); }"));
        screenshot("scrolled-to-end");
        log(String.format("Scrolled %d step(s) of %d px, content moved on %d, end reached %s; \"%s\" %s, hidden behind something %s; button now at y %s",
            steps, STEP_PX, moves, reachedEnd, GO_BACK_TO_QUOTES, lastBox == null ? "not found" : String.format("at y %.0f-%.0f", lastBox.y,
                lastBox.y + lastBox.height), covered, buttonNow == null ? "-" : String.format("%.0f", buttonNow.y)));
        area.evaluate("e => { e.scrollTop = 0; }");
        page.waitForTimeout(500);
        checkPinned(start, "back at the top", problems);
        screenshot("back-to-top");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(moves >= 2, "Confirm Plan's content did not scroll (moved on " + moves + " of " + steps + " steps)");
        softly.assertTrue(problems.isEmpty(), "The Confirm Plan button is not pinned while scrolling: " + problems);
        softly.assertTrue(reachedEnd, "The end of the content was not reached after " + steps + " steps");
        softly.assertTrue(!reachedEnd || (lastVisible && !covered), "At the end of the content \"" + GO_BACK_TO_QUOTES
            + "\" is hidden behind another element or not shown");
        softly.assertAll();
    }

    private void checkPinned(BoundingBox start, String when, List<String> problems) {
        BoundingBox now = confirmPlanButton().boundingBox();
        if (now == null) {
            problems.add("button gone " + when);
            return;
        }
        if (Math.abs(now.y - start.y) > 2) {
            problems.add(String.format("button moved from y %.0f to %.0f %s", start.y, now.y, when));
        }
    }
}
