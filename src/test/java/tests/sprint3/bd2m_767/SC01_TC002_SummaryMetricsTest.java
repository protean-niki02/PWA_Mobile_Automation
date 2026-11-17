package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_002 - Verify that the plan summary card displays the correct metrics (Cashless Garages, Claim Settlement, Policies Issued).
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The plan summary card's metrics box (between the insurer name and IDV) must show the three metrics
 * named in the TC - Cashless Garages, Claim Settlement, Policies Issued - each with a value. The metrics
 * actually shown are logged, and a picture of the card saved.
 */
public class SC01_TC002_SummaryMetricsTest extends ConfirmPlanTestBase {

    private static final List<String> METRICS = List.of("Cashless Garages", "Claim Settlement", "Policies Issued");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_002: Verify that the plan summary card displays the correct metrics (Cashless Garages, Claim Settlement, Policies Issued).")
    public void verifySummaryMetrics() {
        QuotesListingPage screen = openConfirmPlan();
        screen.scrollToTop();
        // The metrics box is drawn between the insurer row and the IDV row; the accessibility tree does
        // not list its texts in that order, so they are taken by position
        Locator insurer = screen.text(chosenInsurer);
        Locator idv = screen.text(Pattern.compile("^\\s*IDV\\s*$"));
        Locator finalPremium = screen.text(Pattern.compile("^\\s*" + FINAL_PREMIUM + "\\s*$"));
        // On iOS the accessibility tree can lag a moment behind the drawn screen
        waitFor(() -> insurer.count() > 0 && idv.count() > 0, 10_000);
        Assert.assertTrue(insurer.count() > 0 && idv.count() > 0, "The plan summary card's insurer name or IDV is not shown. Screen text: "
            + screen.semanticsText());
        BoundingBox top = insurer.boundingBox();
        BoundingBox bottom = idv.boundingBox();
        BoundingBox premiumLabel = finalPremium.count() > 0 ? finalPremium.boundingBox() : null;
        if (premiumLabel != null && premiumLabel.y < bottom.y) {
            bottom = premiumLabel;
        }
        @SuppressWarnings("unchecked")
        List<String> box = (List<String>) page.evaluate("""
            ([top, bottom]) => [...document.querySelectorAll('flt-semantics')].flatMap(e => {
                const own = [...e.childNodes].filter(n => n.nodeType === 3 || (n.nodeType === 1 && n.tagName === 'SPAN'))
                    .map(n => n.textContent.trim()).join(' ').trim();
                const text = own || e.getAttribute('aria-label') || '';
                const r = e.getBoundingClientRect();
                return text && r.top > top && r.top < bottom ? [text] : [];
            })
            """, List.of(top.y + top.height - 2, bottom.y - 2));
        log("Metrics box on the plan summary card: " + box);
        screenshot("summary-metrics");

        SoftAssert softly = new SoftAssert();
        for (String metric : METRICS) {
            softly.assertTrue(box.stream().anyMatch(t -> t.equalsIgnoreCase(metric)), "The plan summary card does not show \""
                + metric + "\" - its metrics box shows " + box);
        }
        softly.assertAll();
    }
}
