package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * SC_01_TC_027 - Verify that an appropriate error message is displayed when quotes fail to load.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * A quotes failure can't be caused on UAT on demand, so it is simulated on get_quotes
 * (POST .../quote/motor/journey/get_quotes), two ways:
 * <ol>
 *   <li>server error - every call answers HTTP 500 with a body without any message, so whatever the
 *       user sees is the app's own wording;</li>
 *   <li>network failure - the listing is reloaded and every call has its connection reset.</li>
 * </ol>
 * For each:
 * <ul>
 *   <li>the loader must end (no endless "fetching the premium");</li>
 *   <li>no quote cards may be shown;</li>
 *   <li>an error message must be shown that tells the user the quotes could not be loaded (something
 *       went wrong / unable to fetch / try again ...) - the plain empty state "No eligible products
 *       are available" alone does not count, as it tells the user there are no products rather than
 *       that loading failed;</li>
 *   <li>it must be user-friendly: no technical text (exception, null, undefined, status codes, JSON).</li>
 * </ul>
 * The message, the actions offered (Try again / Retry / Back ...) and a screenshot are recorded.
 */
public class SC01_TC027_QuotesLoadErrorTest extends QuotesListingTestBase {

    private static final String GET_QUOTES = "**/quote/motor/journey/get_quotes";
    /** What an error message says. */
    private static final Pattern ERROR_MESSAGE = Pattern.compile(
        "(?i)something\\s+went\\s+wrong|(unable|failed)\\s+to\\s+(load|fetch|get)|(could|can)\\s*(not|n't)\\s+(load|fetch|get)|"
            + "try\\s+again|error|oops|technical\\s+(issue|difficult)|went\\s+wrong|not\\s+able\\s+to|issue|problem");
    /** The plain empty state (no products) - not an error message. */
    private static final Pattern EMPTY_STATE = Pattern.compile("(?i)no\\s+(\\w+\\s+){0,2}(quotes?|plans?|products?)\\s+(are\\s+)?(found|available)");
    /** Technical text a user should not see. */
    private static final Pattern TECHNICAL = Pattern.compile(
        "(?i)exception|\\bnull\\b|undefined|NaN|stack\\s*trace|status\\s*code|\\b(500|502|503|504|404|400)\\b|\\{\\s*\"|internal\\s+server");
    private static final Pattern ACTION = Pattern.compile("(?i)try\\s+again|retry|reload|refresh|edit|back|go\\s+home|contact|help");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_027: Verify that an appropriate error message is displayed when quotes fail to load.")
    public void verifyQuotesLoadErrorMessage() {
        AtomicInteger calls = new AtomicInteger();
        boolean[] dropConnection = {false};
        log("Failure 1 - server error: every get_quotes call answers HTTP 500");
        page.route(GET_QUOTES, route -> {
            calls.incrementAndGet();
            if (dropConnection[0]) {
                route.abort("connectionreset");
            } else {
                route.fulfill(new Route.FulfillOptions().setStatus(500).setContentType("application/json").setBody("{\"success\":false}"));
            }
        });
        QuotesListingPage quotes = openQuotesListing();
        SoftAssert softly = new SoftAssert();
        checkFailureScreen(quotes, "server error (HTTP 500)", "quotes-load-error-server", calls, softly);

        // Failure 2: the connection drops - reload the listing, which asks get_quotes again
        log("Failure 2 - network failure: every get_quotes call has its connection reset");
        dropConnection[0] = true;
        calls.set(0);
        page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        quotes.enableSemantics();
        page.waitForTimeout(2_000);
        checkFailureScreen(quotes, "network failure", "quotes-load-error-network", calls, softly);
        softly.assertAll();
    }

    /** Waits for the listing to settle after the failure and checks it shows an appropriate error message. */
    private void checkFailureScreen(QuotesListingPage quotes, String failure, String shot, AtomicInteger calls, SoftAssert softly) {
        boolean finished = quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        quotes.waitForImagesToSettle();
        screenshot(shot);
        List<String> texts = quotes.semanticsTexts();
        List<String> errors = texts.stream().filter(t -> ERROR_MESSAGE.matcher(t).find()).toList();
        List<String> empty = texts.stream().filter(t -> EMPTY_STATE.matcher(t).find()).toList();
        List<String> technical = texts.stream().filter(t -> TECHNICAL.matcher(t).find() && !QuotesListingPage.PREMIUM.matcher(t).find()).toList();
        List<String> actions = texts.stream().filter(t -> ACTION.matcher(t).find()).toList();
        log("[" + failure + "] get_quotes calls failed: " + calls.get() + "; route #" + quotes.route());
        log("[" + failure + "] Screen: " + texts);
        log("[" + failure + "] Error message: " + errors + "; empty state text: " + empty + "; actions offered: " + actions);

        String when = "When the quotes fail to load (" + failure + ")";
        softly.assertTrue(calls.get() > 0, when + ": the app did not call get_quotes, so the failure could not be simulated");
        softly.assertTrue(finished && !quotes.isFetching(), when + ": the loader did not end - still \""
            + QuotesListingPage.FETCHING_MESSAGE + "\" after " + Math.round(QUOTES_TIMEOUT_MS / 1000) + "s");
        softly.assertTrue(quotes.premiums().isEmpty(), when + ": quote cards are shown: " + quotes.premiums());
        softly.assertFalse(errors.isEmpty(), empty.isEmpty()
            ? when + ": no error message is shown. Screen text: " + texts
            : when + ": only the empty state " + empty + " is shown - it tells the user there are no products instead of "
                + "that the quotes could not be loaded, and offers no retry");
        softly.assertTrue(technical.isEmpty(), when + ": technical text is shown instead of a user-friendly message: " + technical);
    }
}
