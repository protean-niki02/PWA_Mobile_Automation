package support;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import org.testng.Reporter;
import pages.BasePage;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Works around the UAT server leaving some requests unanswered (seen with fonts, the logo and
 * main.dart.js; reproducible with plain HTTP clients, so it is a server-side problem). A browser
 * waits for such a request forever and the Flutter app never starts.
 *
 * The app's static files (same origin, GET: scripts, wasm, fonts, images, json) are fetched by
 * Playwright instead; a request with no response within {@link #ATTEMPT_TIMEOUT_MS} is sent
 * again, up to {@link #ATTEMPTS} times. API calls are never repeated. Every retry is logged with
 * {@link BasePage#STALL_WARNING}, so the run is flagged "(stall)" in the timing report.
 */
public final class StalledRequestRetry {

    private static final Pattern STATIC_FILE =
        Pattern.compile("(?i)\\.(js|mjs|wasm|ttf|otf|woff2?|png|jpe?g|gif|svg|webp|ico|json)$");
    private static final double ATTEMPT_TIMEOUT_MS = 15_000;
    private static final int ATTEMPTS = 4;

    private StalledRequestRetry() {
    }

    public static void install(BrowserContext context, String baseUrl) {
        String origin = origin(baseUrl);
        context.route(url -> url.startsWith(origin) && STATIC_FILE.matcher(pathOf(url)).find(),
            StalledRequestRetry::fetchWithRetry);
    }

    private static void fetchWithRetry(Route route) {
        if (!"GET".equals(route.request().method())) {
            route.resume();
            return;
        }
        String file = pathOf(route.request().url());
        for (int attempt = 1; ; attempt++) {
            try {
                APIResponse response = route.fetch(new Route.FetchOptions().setTimeout(ATTEMPT_TIMEOUT_MS));
                route.fulfill(new Route.FulfillOptions().setResponse(response));
                return;
            } catch (PlaywrightException noResponse) {
                if (attempt >= ATTEMPTS) {
                    Reporter.log(BasePage.STALL_WARNING + " - " + file + " not sent after " + ATTEMPTS + " attempts", true);
                    route.abort();
                    return;
                }
                Reporter.log(BasePage.STALL_WARNING + " - " + file + " not sent within "
                    + Math.round(ATTEMPT_TIMEOUT_MS / 1000) + "s, requesting it again (" + (attempt + 1) + "/" + ATTEMPTS + ")", true);
            }
        }
    }

    private static String origin(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    private static String pathOf(String url) {
        String path = URI.create(url).getPath();
        return path == null ? "" : path;
    }
}
