package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_011 - Verify that an add-on chip is removable.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 * Status:        In Progress
 *
 * On the quotes listing, finds the first add-on chip under the IDV and tenure controls (e.g.
 * "PA Owner Driver ✕") and taps its "✕". PA Owner Driver cover is mandatory by law, so the app
 * asks "Are you sure? ... please select the reason" ("I already have PA cover" / "I don't have
 * driving license", Proceed disabled until one is picked); the first reason is picked and Proceed
 * tapped. The confirmation must close - while it is open the chip is only hidden behind it - and
 * the chip must then be gone from the listing. Screenshots of the chip, the confirmation and the
 * listing after are saved. (The removal is not remembered for the car: the next run has the chip again.)
 */
public class SC01_TC011_QuotesAddOnChipRemovableTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_011: Verify that an add-on chip is removable.")
    public void verifyAddOnChipIsRemovable() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        String chip = removeFirstAddOnChip(quotes);
        Assert.assertEquals(page.getByText(REMOVE_CONFIRMATION_TITLE).count(), 0, "The \"" + REMOVE_CONFIRMATION_TITLE
            + "\" confirmation did not close after picking a reason and confirming, so the add-on chip was not removed");

        quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        quotes.scrollToTop();
        screenshot("after-removing-chip");
        boolean gone = quotes.text(chip).count() == 0;
        log("Add-on chip \"" + chip + "\" " + (gone ? "removed" : "still shown"));
        Assert.assertTrue(gone, "The add-on chip \"" + chip + "\" is still shown after tapping its ✕");
    }
}
