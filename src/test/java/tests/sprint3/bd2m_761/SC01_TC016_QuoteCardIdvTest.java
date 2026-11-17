package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SC_01_TC_016 - Verify that the quote card displays the IDV.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Each quote card shows its IDV left of the premium button ("IDV ₹3,00,000"). For every quote card
 * on the listing, the card must show an "IDV" label with a rupee amount (in the same text or the
 * next). The card's IDV is compared with the IDV selected above the quotes (the IDV control) and
 * whether they match is logged. A picture of each card is saved (card_&lt;insurer&gt;.png).
 */
public class SC01_TC016_QuoteCardIdvTest extends QuotesListingTestBase {

    private static final Pattern RUPEES = Pattern.compile("₹\\s?\\d{1,3}(,\\d{2})*,\\d{3}");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_016: Verify that the quote card displays the IDV.")
    public void verifyQuoteCardShowsIdv() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        String selected = quotes.idvControl().count() > 0 ? amountIn(nameOf(quotes.idvControl())) : null;
        log("IDV selected above the quotes: " + (selected == null ? "(not found)" : selected));
        List<String> insurers = quotes.insurerNames().stream().distinct().toList();

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(insurers.isEmpty(), "No quote card (insurer name) found on the listing");
        for (String insurer : insurers) {
            Locator name = quotes.text(insurer);
            quotes.bringIntoView(name);
            String idv = cardIdv(quotes.semanticsTexts(), insurer);
            BoundingBox box = name.boundingBox();
            saveEvidence("card_" + insurer.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase() + ".png",
                ScreenPixels.capture(page, 0, box.y - 20, page.viewportSize().width, 360));
            log(insurer + ": IDV " + (idv == null ? "NOT shown" : idv)
                + (idv != null && selected != null ? (idv.equals(selected) ? " (matches the selected IDV)" : " (differs from the selected IDV " + selected + ")") : ""));
            softly.assertNotNull(idv, "The quote card of " + insurer + " does not display the IDV");
        }
        screenshot("quote-card-idv");
        softly.assertAll();
    }

    /** The IDV shown on one quote card: the amount with its "IDV" label, after the insurer's name. */
    private static String cardIdv(List<String> texts, String insurer) {
        int start = -1;
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).contains(insurer)) {
                start = i;
                break;
            }
        }
        for (int i = start + 1; start >= 0 && i < texts.size(); i++) {
            String text = texts.get(i);
            if (QuotesListingPage.INSURER.matcher(text).find()) {
                break;
            }
            if (text.matches("(?s).*\\bIDV\\b.*")) {
                String amount = amountIn(text);
                if (amount == null && i + 1 < texts.size()) {
                    amount = amountIn(texts.get(i + 1));
                }
                if (amount != null) {
                    return amount;
                }
            }
        }
        return null;
    }

    private static String amountIn(String text) {
        Matcher m = RUPEES.matcher(text == null ? "" : text);
        return m.find() ? m.group().replace(" ", "") : null;
    }

    private static String nameOf(Locator element) {
        String label = element.getAttribute("aria-label");
        String text = label != null && !label.isBlank() ? label : element.textContent();
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }
}
