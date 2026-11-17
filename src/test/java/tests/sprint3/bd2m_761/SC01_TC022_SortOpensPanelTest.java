package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_022 - Verify that tapping "Sort" in the bottom action bar opens the Sort panel.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The quotes listing has a bottom action bar: "Add ons", "Sort", "Filters". Tapping "Sort" must open
 * the Sort panel over the listing:
 * <ul>
 *   <li>the app stays on the quotes listing (a panel opens; it does not navigate away);</li>
 *   <li>new content appears (2+ new texts);</li>
 *   <li>the panel offers sort options (Premium Low to High / High to Low, IDV, Recommended, ...)
 *       or has option selectors (radio buttons) or panel controls (Apply / Done / Reset / Close ...).</li>
 * </ul>
 * The panel's texts are logged and a screenshot of it is saved.
 */
public class SC01_TC022_SortOpensPanelTest extends QuotesListingTestBase {

    /** What a sort option reads like. */
    private static final Pattern SORT_OPTION = Pattern.compile(
        "(?i)low\\s*to\\s*high|high\\s*to\\s*low|ascending|descending|premium|\\bIDV\\b|recommended|relevance|popular|"
            + "claim|price|lowest|highest|sort\\s*by");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_022: Verify that tapping \"Sort\" in the bottom action bar opens the Sort panel.")
    public void verifySortOpensPanel() {
        QuotesListingPage quotes = openLoadedQuotes();
        String route = quotes.route();
        int selectorsBefore = selectorCount();

        List<String> panel = tapAndCollectNewTexts(quotes, quotes.button(QuotesListingPage.SORT), QuotesListingPage.SORT);
        List<String> options = panel.stream().filter(t -> SORT_OPTION.matcher(t).find()).toList();
        int selectorsAdded = selectorCount() - selectorsBefore;
        boolean controls = panel.stream().anyMatch(t -> t.matches("(?is).*\\b(apply|done|update|save|confirm|reset|clear|close|cancel)\\b.*"));
        log("Sort panel: " + panel.size() + " new text(s); sort options " + options + "; " + selectorsAdded
            + " new radio button(s)/selector(s); panel controls: " + controls);
        screenshot("sort-panel");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(quotes.route(), route, "Tapping \"" + QuotesListingPage.SORT
            + "\" left the quotes listing instead of opening the Sort panel");
        softly.assertTrue(panel.size() >= 2, "Tapping \"" + QuotesListingPage.SORT
            + "\" in the bottom action bar did not open the Sort panel. New texts: " + panel);
        softly.assertTrue(!options.isEmpty() || selectorsAdded > 0 || controls, "The panel opened by \"" + QuotesListingPage.SORT
            + "\" offers no sort options and has no option selectors or controls. New texts: " + panel);
        softly.assertAll();
    }

    /** Radio buttons, checkboxes and selected buttons on screen (how a sort order is picked). */
    private int selectorCount() {
        return page.getByRole(AriaRole.RADIO).count() + page.getByRole(AriaRole.CHECKBOX).count()
            + page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(true)).count();
    }
}
