package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.AssetRecorder;
import support.AssetRecorder.Asset;
import support.ScreenPixels;

import java.util.List;

/**
 * SC_01_TC_013 - Verify that the quote card displays the insurer logo and name.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Sanity
 *
 * Each quote card starts with the insurer's logo and name (e.g. the "digit" logo and "DIGIT GI Bima
 * Vahan", the Bima Vahan logo and "RISKCOVERY GI Bima Vahan").
 * <ul>
 *   <li>Name: an insurer name on the listing (Bima Vahan, Digit, ICICI Lombard, HDFC ERGO, Tata AIG ...).</li>
 *   <li>Logo: Flutter draws logos without labels, so for every insurer shown, the spot left of its
 *       name must hold a drawn, multi-coloured picture - at least three colours, not blank. Logos are
 *       line art on the white card (the Bima Vahan logo leaves ~92% of its spot white), while an empty
 *       spot is all white with at most one colour. (Images loaded on the
 *       listing are only logged: they include the Sort / Filter icons and the car, not just logos.)</li>
 * </ul>
 * Each card's name row and logo spot are saved (insurer-row_*.png, logo_*.png).
 */
public class SC01_TC013_QuoteCardInsurerLogoNameTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "sanity"},
        description = "SC_01_TC_013: Verify that the quote card displays the insurer logo and name.")
    public void verifyQuoteCardShowsInsurerLogoAndName() {
        AssetRecorder assets = new AssetRecorder(page);
        QuotesListingPage quotes = openLoadedQuotes();

        List<String> insurers = quotes.insurerNames();
        Assert.assertFalse(insurers.isEmpty(), "No insurer name is displayed on the quote cards. Screen text: " + quotes.semanticsText());
        List<String> images = assets.where(a -> a.ok() && a.route().contains("/quotes/")).stream()
            .map(Asset::fileName).distinct().toList();
        log("Insurer names: " + insurers + " | images loaded on the listing: " + images);

        // The logo is drawn left of each insurer's name; check that spot on every card shown
        SoftAssert softly = new SoftAssert();
        for (String insurer : insurers.stream().distinct().toList()) {
            Locator name = quotes.text(insurer);
            if (!quotes.bringIntoView(name)) {
                softly.fail("The insurer name \"" + insurer + "\" could not be brought on screen");
                continue;
            }
            BoundingBox box = name.boundingBox();
            String file = insurer.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
            saveEvidence("insurer-row_" + file + ".png", ScreenPixels.captureRow(page, box, 30));
            byte[] logoSpot = ScreenPixels.capture(page, 16, box.y - 12, Math.max(20, box.x - 28), box.height + 50);
            saveEvidence("logo_" + file + ".png", logoSpot);
            double white = ScreenPixels.whiteShare(logoSpot);
            int colours = ScreenPixels.countColours(logoSpot, 0.03);
            log(String.format("%s: logo spot %.0f%% white, %d colours", insurer, white * 100, colours));
            softly.assertTrue(white < 0.98 && colours >= 3, "No insurer logo is displayed beside \"" + insurer
                + "\" on its quote card (the logo spot is " + Math.round(white * 100) + "% blank, " + colours + " colour(s))");
        }
        softly.assertAll();
    }
}
