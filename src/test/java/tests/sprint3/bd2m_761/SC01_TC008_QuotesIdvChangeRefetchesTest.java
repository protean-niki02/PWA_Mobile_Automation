package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_008 - Verify that changing the IDV re-fetches/updates the quotes.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Sanity
 * Status:        In Progress
 *
 * On the quotes listing, opens the IDV editor ("Select your vehicle IDV"), types a new IDV inside
 * the allowed range - three quarters of the way up, rounded to ten thousand (e.g. 4,00,000 for
 * 1,00,000 - 5,00,000), or a quarter of the way if that is the current IDV - and taps Update.
 * Then, within 2 minutes:
 * <ul>
 *   <li>the quotes must be fetched again - the "We are fetching the premium ..." loader comes back,
 *       or the premiums change;</li>
 *   <li>the IDV control on the listing must show the new IDV.</li>
 * </ul>
 * Premiums before and after are logged; screenshots of the editor and the updated quotes are saved.
 */
public class SC01_TC008_QuotesIdvChangeRefetchesTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "sanity"},
        description = "SC_01_TC_008: Verify that changing the IDV re-fetches/updates the quotes.")
    public void verifyIdvChangeRefetchesQuotes() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        List<String> premiumsBefore = quotes.premiums();
        List<String> editor = tapAndCollectNewTexts(quotes, quotes.idvControl(), "IDV");

        List<Long> range = amounts(editor);
        Assert.assertTrue(quotes.text(IDV_FIELD_LABEL).count() > 0 && range.size() >= 2,
            "Tapping the IDV control did not open the IDV editor. Editor texts: " + editor);
        long min = range.stream().mapToLong(Long::longValue).min().getAsLong();
        long max = range.stream().mapToLong(Long::longValue).max().getAsLong();
        long before = shownIdv(quotes);
        long target = Math.round((min + 0.75 * (max - min)) / 10_000) * 10_000;
        if (target == before) {
            target = Math.round((min + 0.25 * (max - min)) / 10_000) * 10_000;
        }
        log(String.format("IDV editor: shows %,d, allowed %,d to %,d - changing it to %,d", before, min, max, target));

        typeIntoIdvField(quotes, target);
        long typed = shownIdv(quotes);
        screenshot("idv-changed");
        Assert.assertEquals(typed, target, String.format("The IDV field did not take %,d; the editor shows %,d", target, typed));

        Locator update = quotes.button("Update");
        Assert.assertTrue(update.count() > 0, "The IDV editor has no Update button. Editor texts: " + editor);
        quotes.pressOn(update);
        log("Tapped Update");
        // The editor closes first; give the loader a moment to come up before judging the quotes
        long closeBy = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < closeBy && quotes.text(IDV_FIELD_LABEL).count() > 0) {
            page.waitForTimeout(250);
        }
        long loaderBy = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < loaderBy && !quotes.isFetching()) {
            page.waitForTimeout(250);
        }
        log("After Update: editor closed, loader " + (quotes.isFetching() ? "shown" : "not seen within 5s"));

        boolean refetched = quotes.waitForRefetch(premiumsBefore, QUOTES_TIMEOUT_MS);
        quotes.scrollToTop();
        screenshot("quotes-after-idv-change");
        String control = quotes.idvControl().count() > 0 ? quotes.idvControl().textContent().trim() : "(not found)";
        log("Premiums before: " + premiumsBefore + " | after: " + quotes.premiums() + " | IDV control now: " + control);

        Assert.assertTrue(refetched, String.format("Changing the IDV to %,d did not re-fetch or update the quotes "
            + "(premiums still %s)", target, quotes.premiums()));
        Assert.assertTrue(digits(control) == target || quotes.shows("₹" + indianFormat(target)),
            String.format("The IDV control does not show the new IDV %s; it shows '%s'", indianFormat(target), control));
    }
}
