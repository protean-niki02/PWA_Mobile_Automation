package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SC_01_TC_014 - Verify that the quote card displays the "Instant issuance" tag when applicable.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * "When applicable" is taken from the app's own quote data, not from the screen - otherwise a tag
 * the app fails to draw would look like a quote that isn't eligible. While the quotes load, every
 * JSON answer from the API is read and instant-issuance fields noted (e.g. "instantIssuance": true).
 * Then the quote cards are scrolled for the "Instant issuance" tag (shown under the insurer name,
 * e.g. on PROTEAN GI Bima Vahan):
 * <ul>
 *   <li>eligible per the data and the tag is shown - PASS;</li>
 *   <li>eligible per the data but no tag on any card - FAIL;</li>
 *   <li>tag shown although the data marks nothing eligible - PASS, logged (the field may be named
 *       differently);</li>
 *   <li>neither - SKIPPED, not applicable.</li>
 * </ul>
 * Which quotes carry the tag changes from one quote fetch to the next on UAT (build 2.0.5+3: PROTEAN
 * in one run, RISKCOVERY in another, none in a third), so a run without the tag is not by itself a
 * defect. On build 2.0.5+3 the quote data carries no field with "instant" in its name or value, so
 * eligibility can't be read from it yet and only the screen decides.
 * The instant-issuance fields found are logged; the tag's row is saved as instant-issuance-tag.png.
 */
public class SC01_TC014_QuoteCardInstantIssuanceTagTest extends QuotesListingTestBase {

    /** A field about instant issuance and its value, e.g. "isInstantIssuance": true / "instant_issuance":"Y". */
    private static final Pattern INSTANT_FIELD = Pattern.compile(
        "\"([A-Za-z_]*instant[A-Za-z_]*)\"\\s*:\\s*(true|false|\"[^\"]{0,20}\"|[01])", Pattern.CASE_INSENSITIVE);

    /** A field whose value names instant issuance, e.g. "issuanceType": "INSTANT" - counted as eligible. */
    private static final Pattern INSTANT_VALUE = Pattern.compile(
        "\"([A-Za-z_]+)\"\\s*:\\s*\"([^\"]{0,30}instant[^\"]{0,30})\"", Pattern.CASE_INSENSITIVE);

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_014: Verify that the quote card displays the \"Instant issuance\" tag when applicable.")
    public void verifyInstantIssuanceTag() {
        List<String> instantFields = new CopyOnWriteArrayList<>();
        page.onResponse(response -> {
            String type = response.headers().getOrDefault("content-type", "");
            if (!response.url().contains("api") || !type.contains("json")) {
                return;
            }
            try {
                String body = response.text();
                Matcher m = INSTANT_FIELD.matcher(body);
                while (m.find()) {
                    instantFields.add(m.group(1) + "=" + m.group(2).replace("\"", ""));
                }
                Matcher v = INSTANT_VALUE.matcher(body);
                while (v.find()) {
                    instantFields.add(v.group(1) + "=" + v.group(2) + " (=true)");
                }
            } catch (RuntimeException unreadable) {
                // Redirects and aborted requests have no body
            }
        });

        QuotesListingPage quotes = openLoadedQuotes();
        List<String> eligible = new ArrayList<>();
        for (String field : instantFields) {
            if (field.matches("(?i).*=(true|1|y|yes)") || field.endsWith("(=true)")) {
                eligible.add(field);
            }
        }
        log("Instant-issuance fields in the quote data: " + (instantFields.isEmpty() ? "none" : instantFields)
            + " | eligible: " + eligible.size());

        Locator tag = quotes.text(QuotesListingPage.INSTANT_ISSUANCE);
        boolean shown = quotes.scrollUntilOnScreen(tag);
        if (shown) {
            quotes.bringIntoView(tag);
            saveEvidence("instant-issuance-tag.png", ScreenPixels.captureRow(page, tag.boundingBox(), 60));
            screenshot("instant-issuance-tag");
            String insurer = quotes.semanticsTexts().stream()
                .takeWhile(t -> !t.toLowerCase().contains(QuotesListingPage.INSTANT_ISSUANCE.toLowerCase()))
                .filter(t -> QuotesListingPage.INSURER.matcher(t).find())
                .reduce((first, second) -> second).orElse("(insurer not identified)");
            log("\"" + QuotesListingPage.INSTANT_ISSUANCE + "\" tag displayed on the quote card of " + insurer
                + (eligible.isEmpty() ? " (the quote data marks no quote eligible under a known field name)" : ""));
            return;
        }

        screenshot("no-instant-issuance-tag");
        Assert.assertTrue(eligible.isEmpty(), eligible.size() + " quote(s) are eligible for instant issuance in the app's quote data "
            + eligible + ", but no quote card displays the \"" + QuotesListingPage.INSTANT_ISSUANCE + "\" tag");
        throw new SkipException("Not applicable: no quote for " + selectedVehicle + " is eligible for instant issuance "
            + "(quote data: " + (instantFields.isEmpty() ? "no instant-issuance field" : instantFields) + ") and no tag is shown");
    }
}
