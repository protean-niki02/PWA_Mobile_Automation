package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_019 - Verify that tapping "View Breakup" on a quote card expands the premium breakdown.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Each quote card has "View Breakup" (with a down arrow) at its bottom right. On the first quote card,
 * tapping it must expand the premium breakdown in place:
 * <ul>
 *   <li>the app stays on the quotes listing (it expands, it does not navigate);</li>
 *   <li>new content appears below "View Breakup" with breakdown lines (Own Damage, Third Party,
 *       add-ons, GST, Net / Final Premium, ...) and rupee amounts.</li>
 * </ul>
 * The breakdown is logged, and screenshots of the card before and after are saved.
 */
public class SC01_TC019_QuoteCardViewBreakupTest extends QuotesListingTestBase {

    /** Lines a premium breakdown is made of. */
    private static final Pattern BREAKUP_LINE = Pattern.compile(
        "(?i)own\\s*damage|\\bOD\\b|third\\s*party|\\bTP\\b|GST|tax|net\\s*premium|base\\s*premium|final\\s*premium|"
            + "total|add-?ons?|owner\\s*driver|PA\\b|discount|NCB|premium");
    private static final Pattern RUPEES = Pattern.compile("₹\\s?\\d");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_019: Verify that tapping \"View Breakup\" on a quote card expands the premium breakdown.")
    public void verifyViewBreakupExpandsBreakdown() {
        QuotesListingPage quotes = openLoadedQuotes();
        Locator link = quotes.text(QuotesListingPage.VIEW_BREAKUP).first();
        Assert.assertTrue(link.count() > 0, "No \"" + QuotesListingPage.VIEW_BREAKUP + "\" on the quote cards. Screen text: "
            + quotes.semanticsText());
        quotes.bringIntoView(link);
        BoundingBox linkBox = link.boundingBox();
        String insurer = insurerAbove(quotes, linkBox.y);
        log("First quote card: " + insurer);
        saveCard("card-before-breakup", linkBox);

        String route = quotes.route();
        List<String> added = tapAndCollectNewTexts(quotes, link, QuotesListingPage.VIEW_BREAKUP);
        List<String> lines = added.stream().filter(t -> BREAKUP_LINE.matcher(t).find()).toList();
        List<String> amounts = added.stream().filter(t -> RUPEES.matcher(t).find()).toList();
        log("Breakdown lines: " + lines);
        log("Breakdown amounts: " + amounts);
        saveCard("card-after-breakup", linkBox);

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(quotes.route(), route, "Tapping \"" + QuotesListingPage.VIEW_BREAKUP
            + "\" left the quotes listing instead of expanding the breakdown on the card");
        softly.assertTrue(added.size() >= 2, "Tapping \"" + QuotesListingPage.VIEW_BREAKUP + "\" on " + insurer
            + "'s quote card did not expand anything (new texts: " + added + ")");
        softly.assertFalse(lines.isEmpty(), "The expanded breakup of " + insurer + " shows no premium breakdown lines "
            + "(Own Damage / Third Party / GST / Net Premium ...). New texts: " + added);
        softly.assertFalse(amounts.isEmpty(), "The expanded breakup of " + insurer + " shows no rupee amounts. New texts: " + added);
        softly.assertTrue(belowLink(quotes, added, linkBox.y), "The breakdown of " + insurer
            + " did not open on the card below \"" + QuotesListingPage.VIEW_BREAKUP + "\"");
        softly.assertAll();
    }

    /** Whether any of the new texts lies at or below the "View Breakup" link (the card's expanded area). */
    private static boolean belowLink(QuotesListingPage quotes, List<String> added, double linkY) {
        for (String text : added) {
            Locator shown = quotes.text(text);
            BoundingBox box = shown.count() > 0 ? shown.first().boundingBox() : null;
            if (box != null && box.y >= linkY - 5) {
                return true;
            }
        }
        return false;
    }

    /** Saves a picture of the card around "View Breakup" and the space under it. */
    private void saveCard(String name, BoundingBox linkBox) {
        double top = Math.max(0, linkBox.y - 400);
        double height = Math.min(page.viewportSize().height - top, 900);
        saveEvidence(name + ".png", ScreenPixels.capture(page, 0, top, page.viewportSize().width, height));
    }
}
