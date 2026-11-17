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
 * SC_01_TC_017 - Verify that the quote card displays the premium.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Each quote card shows its premium on the dark button at its right ("₹53,313 →"), apart from the
 * IDV on its left ("IDV ₹3,00,000"). For every quote card on the listing, the card must show a
 * premium: a premium button inside the card (between its insurer name and the next card's) with a
 * rupee amount. The listing must also have one premium button per quote card. Each card's premium is logged and a picture of each card saved (card_&lt;insurer&gt;.png).
 */
public class SC01_TC017_QuoteCardPremiumTest extends QuotesListingTestBase {

    /** A rupee amount, whole: "₹20,473", "₹53,313", "₹1,05,000" (digits and commas after the ₹). */
    private static final Pattern RUPEES = Pattern.compile("₹\\s?\\d[\\d,]*\\d|₹\\s?\\d");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_017: Verify that the quote card displays the premium.")
    public void verifyQuoteCardShowsPremium() {
        QuotesListingPage quotes = openLoadedQuotes();
        List<String> insurers = quotes.insurerNames().stream().distinct().toList();
        List<String> premiumButtons = quotes.premiums();
        log(insurers.size() + " quote card(s), " + premiumButtons.size() + " premium button(s): " + premiumButtons);

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(insurers.isEmpty(), "No quote card (insurer name) found on the listing");
        for (String insurer : insurers) {
            Locator name = quotes.text(insurer);
            quotes.bringIntoView(name);
            String premium = cardPremium(quotes, insurer, insurers);
            BoundingBox box = name.boundingBox();
            saveEvidence("card_" + insurer.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase() + ".png",
                ScreenPixels.capture(page, 0, box.y - 20, page.viewportSize().width, 360));
            log(insurer + ": premium " + (premium == null ? "NOT shown" : premium));
            softly.assertNotNull(premium, "The quote card of " + insurer + " does not display a premium");
        }
        softly.assertTrue(premiumButtons.size() >= insurers.size(), insurers.size() + " quote card(s) but only "
            + premiumButtons.size() + " premium button(s): " + premiumButtons);
        screenshot("quote-card-premium");
        softly.assertAll();
    }

    /**
     * The premium on one quote card: the amount on the premium button that lies inside the card -
     * below the insurer's name and above the next card's insurer name. Taken from the button, not the
     * card's texts, so the card's IDV ("IDV ₹3,00,000") can't be mistaken for it whatever order the
     * browser lists the texts in.
     */
    private String cardPremium(QuotesListingPage quotes, String insurer, List<String> insurers) {
        double top = quotes.text(insurer).boundingBox().y;
        double bottom = top + 450;
        for (String other : insurers) {
            Locator next = quotes.text(other);
            BoundingBox box = other.equals(insurer) || next.count() == 0 ? null : next.boundingBox();
            if (box != null && box.y > top && box.y < bottom) {
                bottom = box.y;
            }
        }
        Locator buttons = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new com.microsoft.playwright.Page.GetByRoleOptions().setName(Pattern.compile("^(?!.*IDV).*₹\\s?\\d")));
        for (int i = 0; i < buttons.count(); i++) {
            BoundingBox box = buttons.nth(i).boundingBox();
            if (box != null && box.y > top && box.y < bottom) {
                String label = buttons.nth(i).getAttribute("aria-label");
                Matcher m = RUPEES.matcher(label != null && !label.isBlank() ? label : buttons.nth(i).innerText());
                if (m.find()) {
                    return m.group().replace(" ", "");
                }
            }
        }
        return null;
    }
}
