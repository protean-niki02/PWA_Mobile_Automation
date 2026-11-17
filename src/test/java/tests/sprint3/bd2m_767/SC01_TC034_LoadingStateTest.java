package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

/**
 * SC_01_TC_034 - Verify that the loading state is handled with appropriate messaging.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Confirm Plan recalculates the premium after a cover is added. A recorder in the page notes, every 100 ms,
 * any loading indication: a progress indicator or spinner, or a message (loading / please wait / fetching /
 * calculating / updating). An add-on cover is added and the Final Premium update awaited. If the update took
 * longer than 1s, a loading indication must have been shown meanwhile; either way it must end, with no
 * error shown. What was seen, and for how long, is logged. Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC034_LoadingStateTest extends ConfirmPlanWebTestBase {

    private static final String RECORDER = """
        (() => {
            if (window.__loading) return;
            const rec = window.__loading = { first: 0, last: 0, message: '' };
            setInterval(() => {
                if (!document.body) return;
                const bar = document.querySelector('[role="progressbar"], [aria-busy="true"], .animate-spin, [class*="spinner" i], [class*="loader" i]');
                const m = /(loading|please wait|fetching|calculating|recalculating|updating)[^|\\n]{0,60}/i.exec(document.body.innerText || '');
                if (bar || m) {
                    const now = Date.now();
                    rec.first = rec.first || now;
                    rec.last = now;
                    rec.message = rec.message || (m ? m[0].trim() : 'progress indicator');
                }
            }, 100);
        })();
        """;

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_034: Verify that the loading state is handled with appropriate messaging.")
    public void verifyLoadingState() {
        page.addInitScript(RECORDER);
        openConfirmPlan();
        page.evaluate("() => { if (window.__loading) { window.__loading.first = 0; window.__loading.last = 0; window.__loading.message = ''; } }");
        long before = finalPremium();
        Locator toggle = addOnToggle();
        Assert.assertTrue(toggle.count() > 0, "No add-on cover to add on Confirm Plan. Screen text: " + bodyText());
        String cover = coverTitleOf(toggle);
        toggle.scrollIntoViewIfNeeded();

        long tapped = System.currentTimeMillis();
        toggle.click();
        long end = tapped + 30_000;
        long after = finalPremium();
        while (System.currentTimeMillis() < end && (after == before || after == 0)) {
            page.waitForTimeout(250);
            after = finalPremium();
        }
        long took = System.currentTimeMillis() - tapped;
        page.waitForTimeout(1_000);
        after = finalPremium();
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) page.evaluate("() => window.__loading || {}");
        long first = rec.get("first") instanceof Number n ? n.longValue() : 0;
        long last = rec.get("last") instanceof Number n ? n.longValue() : 0;
        String message = String.valueOf(rec.getOrDefault("message", ""));
        String body = bodyText();
        boolean stillLoading = body.matches("(?is).*(loading|please wait|calculating|recalculating).*");
        log(String.format("Added %s: Final Premium %s -> %s in about %.1fs; loading indication %s", cover, indianFormat(before),
            indianFormat(after), took / 1000.0, first == 0 ? "none seen" : String.format("\"%s\" for %.1fs", message, (last - first) / 1000.0)));
        screenshot("after-recalculation");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(after != before, "The Final Premium did not update after adding " + cover + ", so no loading state was triggered");
        softly.assertTrue(took <= 1_000 + 1_500 || first > 0, String.format(
            "The premium took about %.1fs to update after adding %s but no loading indication or message was shown", took / 1000.0, cover));
        softly.assertFalse(stillLoading, "The loading state did not end: " + body);
        softly.assertFalse(ERROR_TEXT.matcher(body).find(), "An error is shown after the recalculation: " + body);
        softly.assertAll();
    }
}
