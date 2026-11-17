package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_015 - Verify that the quote card displays the three metrics (Cashless Garages, Paid-in-Claims, Policies Sold).
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Each quote card shows a box of metrics between the insurer's name and its IDV / premium. For every
 * quote card on the listing, that box must show the three metrics Cashless Garages, Paid-in-Claims
 * and Policies Sold (letter case and spacing may differ, e.g. "Paid in Claims"). Every card is
 * checked, and the metrics each card actually shows are logged - build 2.0.5+3 was seen showing
 * "Cars Insured / Claims Settled / EV-Ready Garages" on one card and only "Priority Support" on
 * another. A screenshot of each card's metrics is saved (metrics_&lt;insurer&gt;.png).
 */
public class SC01_TC015_QuoteCardMetricsTest extends QuotesListingTestBase {

    private static final List<String> EXPECTED = QuotesListingPage.METRICS;

    /** A metric's number above its label, e.g. "1000", "98%", "8,500+". */
    private static final Pattern METRIC_VALUE = Pattern.compile("^[\\d,.]+\\s*[%+KkLl]*$");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_015: Verify that the quote card displays the three metrics (Cashless Garages, Paid-in-Claims, Policies Sold).")
    public void verifyQuoteCardMetrics() {
        QuotesListingPage quotes = openLoadedQuotes();
        List<String> insurers = quotes.insurerNames().stream().distinct().toList();

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(insurers.isEmpty(), "No quote card (insurer name) found on the listing");
        for (String insurer : insurers) {
            Locator name = quotes.text(insurer);
            quotes.bringIntoView(name);
            List<String> card = cardTexts(quotes.semanticsTexts(), insurer);
            List<String> shown = card.stream().filter(t -> !METRIC_VALUE.matcher(t).matches()).toList();
            List<String> missing = EXPECTED.stream().filter(m -> card.stream().noneMatch(t -> sameMetric(t, m))).toList();

            String file = insurer.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
            com.microsoft.playwright.options.BoundingBox box = name.boundingBox();
            saveEvidence("metrics_" + file + ".png", ScreenPixels.capture(page, 0, box.y - 20, page.viewportSize().width, 260));
            log(insurer + ": metrics shown " + shown + " | expected missing " + (missing.isEmpty() ? "none" : missing));
            softly.assertTrue(missing.isEmpty(), "The quote card of " + insurer + " does not display the metric(s) " + missing
                + "; it shows " + (shown.isEmpty() ? "no metrics" : shown));
        }
        screenshot("quote-card-metrics");
        softly.assertAll();
    }

    /**
     * The texts of one quote card's metrics box: after the insurer's name and its policy line, up to the
     * card's IDV (or the next insurer).
     */
    private static List<String> cardTexts(List<String> texts, String insurer) {
        List<String> card = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).contains(insurer)) {
                start = i;
                break;
            }
        }
        for (int i = start + 1; start >= 0 && i < texts.size(); i++) {
            String text = texts.get(i);
            if (text.matches("(?s).*\\bIDV\\b.*") || QuotesListingPage.INSURER.matcher(text).find()
                || QuotesListingPage.PREMIUM.matcher(text).find()) {
                break;
            }
            if (!text.matches("(?i).*(policy|instant issuance).*")) {
                card.add(text);
            }
        }
        return card;
    }

    /** Same metric, ignoring letter case, spaces and hyphens ("Paid-in-Claims" = "Paid in Claims"). */
    private static boolean sameMetric(String text, String metric) {
        String normal = metric.toLowerCase().replaceAll("[^a-z]", "");
        return text.toLowerCase().replaceAll("[^a-z]", "").contains(normal);
    }
}
