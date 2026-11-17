package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_020 - Verify that tapping the premium button on a quote card proceeds to the next step.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Each quote card has a dark premium button at its right ("₹53,313 →"). On the first quote card,
 * tapping it must take the user on to the next step of the journey:
 * <ul>
 *   <li>within 30s the app leaves the quotes listing for another screen (new route), or shows the
 *       next step's content in place of the listing;</li>
 *   <li>the next step is not an error.</li>
 * </ul>
 * The next step's route and texts are logged and screenshots before and after are saved.
 */
public class SC01_TC020_QuoteCardPremiumProceedsTest extends QuotesListingTestBase {

    private static final int NEXT_STEP_TIMEOUT_MS = 30_000;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_020: Verify that tapping the premium button on a quote card proceeds to the next step.")
    public void verifyPremiumButtonProceeds() {
        QuotesListingPage quotes = openLoadedQuotes();
        Locator premium = quotes.premiumButton();
        Assert.assertTrue(premium.count() > 0, "No premium button on the quote cards. Screen text: " + quotes.semanticsText());
        quotes.bringIntoView(premium);
        String insurer = insurerAbove(quotes, premium.boundingBox().y);
        String label = premium.getAttribute("aria-label");
        log("First quote card: " + insurer + ", premium button \"" + (label == null || label.isBlank() ? premium.innerText() : label) + "\"");
        screenshot("before-tapping-premium");

        String listingRoute = quotes.route();
        List<String> before = quotes.semanticsTexts();
        quotes.pressOn(premium);
        boolean moved = waitForNextStep(quotes, listingRoute, before);
        quotes.waitForImagesToSettle();
        List<String> added = new ArrayList<>(quotes.semanticsTexts());
        added.removeAll(before);
        String route = quotes.route();
        log(String.format("Tapped the premium button: route %s -> %s, %d new text(s) %s", listingRoute, route, added.size(),
            added.size() > 12 ? added.subList(0, 12) + "..." : added));
        screenshot("next-step-after-premium");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(moved, "Tapping the premium button on " + insurer + "'s quote card did not proceed: still on the quotes "
            + "listing (#" + route + ") after " + NEXT_STEP_TIMEOUT_MS / 1000 + "s");
        softly.assertFalse(quotes.showsError(), "The next step after tapping the premium button shows an error. Screen text: "
            + quotes.semanticsText());
        softly.assertAll();
    }

    /**
     * Waits for the next step: the route changes from the listing, or the listing's own texts
     * (insurer names, "View Breakup") are replaced by new content. True when that happened in time.
     */
    private boolean waitForNextStep(QuotesListingPage quotes, String listingRoute, List<String> before) {
        long end = System.currentTimeMillis() + NEXT_STEP_TIMEOUT_MS;
        while (System.currentTimeMillis() < end) {
            page.waitForTimeout(1_000);
            if (!quotes.route().equals(listingRoute)) {
                return true;
            }
            List<String> now = quotes.semanticsTexts();
            List<String> added = new ArrayList<>(now);
            added.removeAll(before);
            boolean listingGone = quotes.text(QuotesListingPage.VIEW_BREAKUP).count() == 0;
            if (listingGone && added.size() >= 3) {
                return true;
            }
        }
        return false;
    }
}
