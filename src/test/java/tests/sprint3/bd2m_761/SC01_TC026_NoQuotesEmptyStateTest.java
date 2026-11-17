package tests.sprint3.bd2m_761;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Route;
import config.TestConfig;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SC_01_TC_026 - Verify that an appropriate empty state message is displayed when no quotes are available.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * Needs a car no insurer quotes for. When quotes.noQuotesRegistrationNumber is set in
 * config.properties that car is used as it is. Otherwise "no quotes available" is simulated for the
 * test car: the real get_quotes answer (POST .../quote/motor/journey/get_quotes) is let through with
 * its quote lists emptied, so the app is told there are no quotes. Then:
 * <ul>
 *   <li>the loading state must end (no endless loader);</li>
 *   <li>no quote cards may be shown;</li>
 *   <li>an empty state message must be shown that tells the user no quotes / plans are available
 *       (the app says "No eligible products are available");</li>
 *   <li>it must be user-friendly: no technical error text (exception, null, undefined, status codes).</li>
 * </ul>
 * The empty state's texts, the actions it offers and a screenshot are recorded.
 */
public class SC01_TC026_NoQuotesEmptyStateTest extends QuotesListingTestBase {

    private static final String GET_QUOTES = "**/quote/motor/journey/get_quotes";
    /** An object in a quote list: it names an insurer, product or premium. */
    private static final Pattern QUOTE_KEY = Pattern.compile("(?i)premium|insurer|product|idv|quote");
    /** What an empty state says. */
    private static final Pattern EMPTY_MESSAGE = Pattern.compile(
        "(?i)no\\s+(\\w+\\s+){0,2}(quotes?|plans?|insurers?|products?|results?|offers?)|not\\s+available|unavailable|"
            + "(could|can)\\s*(not|n't)\\s+(find|fetch|get)|unable\\s+to\\s+(find|fetch|get)|sorry|nothing\\s+(found|to\\s+show)");
    /** Technical text a user should not see. */
    private static final Pattern TECHNICAL = Pattern.compile(
        "(?i)exception|\\bnull\\b|undefined|NaN|stack\\s*trace|status\\s*code|\\b[45]\\d\\d\\b|\\{\\s*\"");
    private static final Pattern ACTION = Pattern.compile(
        "(?i)try\\s+again|retry|edit|change|modify|back|go\\s+home|contact|call|help|reset|clear");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_026: Verify that an appropriate empty state message is displayed when no quotes are available.")
    public void verifyNoQuotesEmptyState() {
        String noQuotesCar = TestConfig.optional("quotes.noQuotesRegistrationNumber");
        List<String> emptied = new ArrayList<>();
        if (noQuotesCar.isEmpty()) {
            log("quotes.noQuotesRegistrationNumber is not set - simulating \"no quotes available\" for "
                + registrationNumber() + " by emptying the quote lists in the get_quotes answer");
            page.route(GET_QUOTES, route -> answerWithoutQuotes(route, emptied));
        }
        QuotesListingPage quotes = noQuotesCar.isEmpty() ? openQuotesListing() : openQuotesListing(noQuotesCar);
        List<String> before = quotes.semanticsTexts();
        boolean finished = quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        quotes.waitForImagesToSettle();
        screenshot("empty-state");
        List<String> texts = quotes.semanticsTexts();
        log("Emptied in get_quotes: " + (emptied.isEmpty() ? "nothing" : emptied));
        log("Screen after loading: " + texts);
        if (noQuotesCar.isEmpty() && emptied.isEmpty() && !quotes.premiums().isEmpty()) {
            throw new SkipException("Could not simulate \"no quotes\": get_quotes had no quote list to empty and quotes "
                + quotes.premiums() + " are shown - set quotes.noQuotesRegistrationNumber to a car with no quotes");
        }

        List<String> messages = texts.stream().filter(t -> EMPTY_MESSAGE.matcher(t).find()).toList();
        List<String> technical = texts.stream().filter(t -> TECHNICAL.matcher(t).find() && !QuotesListingPage.PREMIUM.matcher(t).find()).toList();
        List<String> actions = texts.stream().filter(t -> ACTION.matcher(t).find()).toList();
        log("Empty state message: " + messages + "; actions offered: " + actions + "; loading texts before: " + before.size());

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(finished && !quotes.isFetching(), "The loader did not end: still \"" + QuotesListingPage.FETCHING_MESSAGE
            + "\" after " + Math.round(QUOTES_TIMEOUT_MS / 1000) + "s with no quotes available");
        softly.assertTrue(quotes.premiums().isEmpty(), "Quote cards are shown although no quotes are available: " + quotes.premiums());
        softly.assertFalse(messages.isEmpty(), "No empty state message (e.g. \"No quotes available\") is shown when no quotes are "
            + "available. Screen text: " + texts);
        softly.assertTrue(technical.isEmpty(), "The empty state shows technical text instead of a user-friendly message: " + technical);
        softly.assertAll();
    }

    /** Lets the real get_quotes answer through with every quote list in it emptied (noted in {@code emptied}). */
    private void answerWithoutQuotes(Route route, List<String> emptied) {
        APIResponse real = route.fetch();
        String body = real.text();
        try {
            JsonElement json = JsonParser.parseString(body);
            JsonElement changed = emptyQuoteLists(json, "$", emptied);
            route.fulfill(new Route.FulfillOptions().setResponse(real).setBody(changed.toString()));
        } catch (RuntimeException notJson) {
            log("get_quotes answer is not JSON (" + notJson.getMessage() + "); passed on unchanged");
            route.fulfill(new Route.FulfillOptions().setResponse(real));
        }
    }

    /** Replaces each array of quote objects (objects with insurer / premium / product keys) with an empty one. */
    private static JsonElement emptyQuoteLists(JsonElement element, String path, List<String> emptied) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            boolean quoteList = !array.isEmpty() && array.get(0).isJsonObject()
                && array.get(0).getAsJsonObject().keySet().stream().anyMatch(k -> QUOTE_KEY.matcher(k).find());
            if (quoteList) {
                emptied.add(path + " (" + array.size() + " item(s))");
                return new JsonArray();
            }
            JsonArray copy = new JsonArray();
            for (int i = 0; i < array.size(); i++) {
                copy.add(emptyQuoteLists(array.get(i), path + "[" + i + "]", emptied));
            }
            return copy;
        }
        if (element.isJsonObject()) {
            JsonObject copy = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                copy.add(entry.getKey(), emptyQuoteLists(entry.getValue(), path + "." + entry.getKey(), emptied));
            }
            return copy;
        }
        return element;
    }
}
