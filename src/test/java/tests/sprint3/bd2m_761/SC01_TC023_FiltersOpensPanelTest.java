package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_023 - Verify that tapping "Filters" in the bottom action bar opens the Filters panel.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The quotes listing has a bottom action bar: "Add ons", "Sort", "Filters". Tapping "Filters" must
 * open the Filters panel over the listing:
 * <ul>
 *   <li>the app stays on the quotes listing (a panel opens; it does not navigate away);</li>
 *   <li>new content appears (2+ new texts);</li>
 *   <li>the panel offers filters (insurer, plan / policy type, IDV, premium range, instant issuance,
 *       claim settlement, garages, ...) or has filter selectors (checkboxes / radio buttons / switches /
 *       sliders) or panel controls (Apply / Reset / Clear / Close ...).</li>
 * </ul>
 * The panel's texts are logged and a screenshot of it is saved.
 */
public class SC01_TC023_FiltersOpensPanelTest extends QuotesListingTestBase {

    /** What a filter reads like. */
    private static final Pattern FILTER_OPTION = Pattern.compile(
        "(?i)insurer|plan\\s*type|policy\\s*type|comprehensive|third\\s*party|own\\s*damage|\\bIDV\\b|premium|range|"
            + "instant|claim|garage|cashless|rating|filter\\s*by|bima\\s*vahan");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_023: Verify that tapping \"Filters\" in the bottom action bar opens the Filters panel.")
    public void verifyFiltersOpensPanel() {
        QuotesListingPage quotes = openLoadedQuotes();
        String route = quotes.route();
        int selectorsBefore = selectorCount();

        List<String> panel = tapAndCollectNewTexts(quotes, quotes.button(QuotesListingPage.FILTERS), QuotesListingPage.FILTERS);
        List<String> filters = panel.stream().filter(t -> FILTER_OPTION.matcher(t).find()).toList();
        int selectorsAdded = selectorCount() - selectorsBefore;
        boolean controls = panel.stream().anyMatch(t -> t.matches("(?is).*\\b(apply|done|update|save|confirm|reset|clear|close|cancel)\\b.*"));
        log("Filters panel: " + panel.size() + " new text(s); filters " + filters + "; " + selectorsAdded
            + " new selector(s); panel controls: " + controls);
        screenshot("filters-panel");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(quotes.route(), route, "Tapping \"" + QuotesListingPage.FILTERS
            + "\" left the quotes listing instead of opening the Filters panel");
        softly.assertTrue(panel.size() >= 2, "Tapping \"" + QuotesListingPage.FILTERS
            + "\" in the bottom action bar did not open the Filters panel. New texts: " + panel);
        softly.assertTrue(!filters.isEmpty() || selectorsAdded > 0 || controls, "The panel opened by \"" + QuotesListingPage.FILTERS
            + "\" offers no filters and has no filter selectors or controls. New texts: " + panel);
        softly.assertAll();
    }

    /** Checkboxes, radio buttons, switches, sliders and selected buttons on screen (how filters are set). */
    private int selectorCount() {
        int count = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(true)).count();
        for (AriaRole role : List.of(AriaRole.CHECKBOX, AriaRole.RADIO, AriaRole.SWITCH, AriaRole.SLIDER)) {
            count += page.getByRole(role).count();
        }
        return count;
    }
}
