package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_012 - Verify that removing an add-on chip updates the quotes/premiums.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 * Status:        In Progress
 *
 * On the quotes listing, notes the premiums, then removes the first add-on chip under the IDV and
 * tenure controls (e.g. "PA Owner Driver ✕") by tapping its "✕". PA Owner Driver cover is mandatory
 * by law, so the app asks "Are you sure? ... please select the reason" ("I already have PA cover" /
 * "I don't have driving license", Proceed disabled until one is picked); the first reason is picked,
 * Proceed tapped, and the confirmation must close. Within 2 minutes:
 * <ul>
 *   <li>the chip must be gone, and</li>
 *   <li>the quotes must be fetched again - the "We are fetching the premium ..." loader comes back,
 *       or the premiums change.</li>
 * </ul>
 * Premiums before and after are logged; screenshots of the chip, any confirmation and the updated
 * quotes are saved. Removing the chip may be remembered for the car, so a later run may find no chip.
 */
public class SC01_TC012_RemoveAddOnChipUpdatesPremiumsTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_012: Verify that removing an add-on chip updates the quotes/premiums.")
    public void verifyRemovingAddOnChipUpdatesPremiums() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        List<String> premiumsBefore = quotes.premiums();
        String chip = removeFirstAddOnChip(quotes);
        Assert.assertEquals(page.getByText(REMOVE_CONFIRMATION_TITLE).count(), 0, "The \"" + REMOVE_CONFIRMATION_TITLE
            + "\" confirmation did not close after picking a reason and confirming, so the add-on was not removed");

        long loaderBy = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < loaderBy && !quotes.isFetching()) {
            page.waitForTimeout(250);
        }
        log("After removing \"" + chip + "\": loader " + (quotes.isFetching() ? "shown" : "not seen within 5s"));
        boolean updated = quotes.waitForRefetch(premiumsBefore, QUOTES_TIMEOUT_MS);
        quotes.scrollToTop();
        screenshot("quotes-after-removing-chip");
        boolean chipGone = quotes.text(chip).count() == 0;
        log("Premiums before: " + premiumsBefore + " | after: " + quotes.premiums() + " | chip \"" + chip + "\" "
            + (chipGone ? "removed" : "still shown"));

        Assert.assertTrue(chipGone, "The add-on chip \"" + chip + "\" is still shown after tapping its ✕");
        Assert.assertTrue(updated, "Removing the add-on chip \"" + chip + "\" did not update the quotes/premiums (still "
            + quotes.premiums() + ")");
    }
}
