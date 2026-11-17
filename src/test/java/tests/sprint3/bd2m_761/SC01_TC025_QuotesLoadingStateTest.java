package tests.sprint3.bd2m_761;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * SC_01_TC_025 - Verify that the loading state is handled with appropriate messaging while quotes are being fetched.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * After "View Prices" the listing fetches quotes from the insurers, which takes a few seconds. The
 * fetch can end quickly, so a recorder is put into the page before the journey starts: every 100 ms
 * it notes whether the loading message is on screen, and when the quotes appear. Then:
 * <ul>
 *   <li>a loading message must be shown while the quotes are fetched;</li>
 *   <li>it must be the appropriate message: "We are fetching the premium from the insurer based on
 *       your inputs";</li>
 *   <li>no error may be shown with it;</li>
 *   <li>the loading state must end: the message goes and the quotes (or a no-quotes message) take
 *       its place within the quotes timeout.</li>
 * </ul>
 * The message, how long it was shown and a screenshot of the loading state are recorded.
 */
public class SC01_TC025_QuotesLoadingStateTest extends QuotesListingTestBase {

    /** The expected message, with any spacing between its words (Flutter may break it over lines). */
    private static final Pattern EXPECTED_MESSAGE = Pattern.compile(
        QuotesListingPage.FETCHING_MESSAGE.replace(" ", "\\s*"), Pattern.CASE_INSENSITIVE);

    /** Runs in the page from its first load: records the loading message and when the quotes appear. */
    private static final String LOADING_RECORDER = """
        (() => {
            if (window.__quotesLoading) return;
            const rec = window.__quotesLoading = { seen: 0, first: 0, last: 0, message: '', quotesAt: 0, errorWhileLoading: '' };
            setInterval(() => {
                const host = document.querySelector('flt-semantics-host');
                if (!host) return;
                const parts = [...host.querySelectorAll('[aria-label]')].map(e => e.getAttribute('aria-label') || '');
                parts.push(...host.innerText.split('\\n'));
                const text = parts.join(' | ').replace(/\\s+/g, ' ');
                const now = Date.now();
                if (/fetching the premium/i.test(text)) {
                    rec.seen++;
                    rec.first = rec.first || now;
                    rec.last = now;
                    rec.message = rec.message || (parts.find(p => /fetching the premium/i.test(p)) || '').replace(/\\s+/g, ' ').trim();
                    const error = /something went wrong|failed to (fetch|load)|unable to (fetch|load)/i.exec(text);
                    if (error) rec.errorWhileLoading = error[0];
                } else if (rec.first && !rec.quotesAt && /View Breakup|\\u20B9\\s?\\d/.test(text)) {
                    rec.quotesAt = now;
                }
            }, 100);
        })();
        """;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_025: Verify that the loading state is handled with appropriate messaging while quotes are being fetched.")
    public void verifyQuotesLoadingState() {
        page.addInitScript(LOADING_RECORDER);
        QuotesListingPage quotes = openQuotesListing();
        if (quotes.isFetching()) {
            log("Loading state: " + quotes.semanticsTexts());
            screenshot("loading-state");
        }
        boolean finished = quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        screenshot("after-loading");

        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) page.evaluate("() => window.__quotesLoading || {}");
        long seen = number(rec.get("seen"));
        long shownMs = number(rec.get("last")) - number(rec.get("first"));
        String message = String.valueOf(rec.getOrDefault("message", ""));
        String error = String.valueOf(rec.getOrDefault("errorWhileLoading", ""));
        long quotesAt = number(rec.get("quotesAt"));
        log(String.format("Loading message seen %d time(s) over %.1fs: \"%s\"; quotes appeared %s; error with it: %s",
            seen, shownMs / 1000.0, message,
            quotesAt > 0 ? String.format("%.1fs after loading began", (quotesAt - number(rec.get("first"))) / 1000.0) : "not after it",
            error.isEmpty() ? "none" : error));
        log("After loading: " + (quotes.premiums().isEmpty() ? "no premiums" : quotes.premiums().size() + " quote(s) " + quotes.premiums())
            + (quotes.showsNoQuotes() ? ", no-quotes message shown" : ""));

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(seen > 0, "No loading message was shown while the quotes were being fetched");
        softly.assertTrue(seen == 0 || EXPECTED_MESSAGE.matcher(message).find(), "The loading message is \"" + message
            + "\", expected \"" + QuotesListingPage.FETCHING_MESSAGE + "\"");
        softly.assertTrue(error.isEmpty(), "An error (\"" + error + "\") was shown during the loading state");
        softly.assertTrue(finished && !quotes.isFetching(), "The loading state did not end: still \"" + QuotesListingPage.FETCHING_MESSAGE
            + "\" after " + Math.round(QUOTES_TIMEOUT_MS / 1000) + "s");
        softly.assertTrue(!quotes.premiums().isEmpty() || quotes.showsNoQuotes(),
            "After the loading state neither quotes nor a no-quotes message are shown. Screen text: " + quotes.semanticsText());
        softly.assertAll();
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0;
    }
}
