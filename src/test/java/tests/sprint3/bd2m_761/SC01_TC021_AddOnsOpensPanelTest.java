package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_021 - Verify that tapping "Add ons" in the bottom action bar opens the Add ons panel.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The quotes listing has a bottom action bar: "Add ons", "Sort", "Filters". Tapping "Add ons" must
 * open the Add ons panel over the listing:
 * <ul>
 *   <li>the app stays on the quotes listing (a panel opens; it does not navigate away);</li>
 *   <li>new content appears (3+ new texts);</li>
 *   <li>the panel lists add-on covers (Zero Depreciation, Roadside Assistance, PA Owner Driver,
 *       Engine Protect, ...) or has add-on selectors (checkboxes / switches) or panel controls
 *       (Apply / Done / Update / Close ...).</li>
 * </ul>
 * The panel's texts are logged and a screenshot of it is saved.
 */
public class SC01_TC021_AddOnsOpensPanelTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_021: Verify that tapping \"Add ons\" in the bottom action bar opens the Add ons panel.")
    public void verifyAddOnsOpensPanel() {
        QuotesListingPage quotes = openLoadedQuotes();
        String route = quotes.route();
        int selectorsBefore = selectorCount();

        List<String> panel = tapAndCollectNewTexts(quotes, quotes.button(QuotesListingPage.ADD_ONS), QuotesListingPage.ADD_ONS);
        List<String> addOns = panel.stream().filter(t -> QuotesListingPage.ADD_ON.matcher(t).find()).toList();
        int selectorsAdded = selectorCount() - selectorsBefore;
        boolean controls = panel.stream().anyMatch(t -> t.matches("(?is).*\\b(apply|done|update|save|confirm|reset|clear|close|cancel)\\b.*"));
        log("Add ons panel: " + panel.size() + " new text(s); add-ons listed " + addOns + "; " + selectorsAdded
            + " new checkbox(es)/switch(es); panel controls: " + controls);
        screenshot("add-ons-panel");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(quotes.route(), route, "Tapping \"" + QuotesListingPage.ADD_ONS
            + "\" left the quotes listing instead of opening the Add ons panel");
        softly.assertTrue(panel.size() >= 3, "Tapping \"" + QuotesListingPage.ADD_ONS
            + "\" in the bottom action bar did not open the Add ons panel. New texts: " + panel);
        softly.assertTrue(!addOns.isEmpty() || selectorsAdded > 0 || controls, "The panel opened by \"" + QuotesListingPage.ADD_ONS
            + "\" lists no add-on covers and has no add-on selectors or controls. New texts: " + panel);
        softly.assertAll();
    }

    /** Checkboxes and switches on screen (how add-ons are picked). */
    private int selectorCount() {
        return page.getByRole(AriaRole.CHECKBOX).count() + page.getByRole(AriaRole.SWITCH).count()
            + page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(true)).count();
    }
}
