package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import config.TestConfig;
import org.testng.Reporter;

import java.util.List;
import java.util.function.Predicate;

/**
 * Base for screens of the Flutter web PWA served to mobile browsers.
 *
 * Flutter paints the UI on a canvas, so there is nothing for locators to find until
 * its semantics (accessibility) tree is switched on. Once it is, Flutter mirrors every
 * widget as an flt-semantics element with a role and label, and getByRole/getByText work.
 *
 * The app routes on the URL hash: #/splash, #/onboarding, #/login, ...
 */
public abstract class FlutterPage extends BasePage {

    // Booting downloads main.dart.js (~6 MB) and CanvasKit, so it is part of page load
    // and gets the navigation timeout rather than the shorter action timeout
    private static final Locator.WaitForOptions BOOTED =
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED)
            .setTimeout(TestConfig.navigationTimeoutMs());

    /** All text Flutter exposes through semantics: labels plus visible text, whitespace collapsed. */
    private static final String SEMANTICS_TEXT_JS = """
        () => {
            const host = document.querySelector('flt-semantics-host');
            if (!host) return '';
            const labels = [...host.querySelectorAll('[aria-label]')].map(e => e.getAttribute('aria-label'));
            return (labels.join(' | ') + ' | ' + host.innerText).replace(/\\s+/g, ' ').trim();
        }
        """;

    protected FlutterPage(Page page) {
        super(page);
    }

    /** Opens a hash route of the app (e.g. "" for the start screen, "/login") with semantics on. */
    protected void openApp(String route) {
        navigate(route.isEmpty() ? "/" : route);
        enableSemantics();
    }

    /**
     * Waits for Flutter to boot, then presses its hidden "Enable accessibility" control.
     * Does nothing when semantics are already on (Flutter then removes the placeholder).
     */
    public void enableSemantics() {
        waitForAppStart(this::switchOnSemantics);
    }

    /**
     * The semantics tree only appears once the app has drawn its first screen, which needs
     * every startup file (main.dart.js, CanvasKit, fonts), so both waits get the navigation timeout.
     */
    private void switchOnSemantics() {
        page.locator("flt-semantics-placeholder, flt-semantics").first().waitFor(BOOTED);
        if (page.locator("flt-semantics").count() > 0) {
            return;
        }
        // The placeholder is off-screen, so a real click can't reach it. On mobile, Flutter
        // also ignores the click unless it lands on the placeholder's centre.
        page.evaluate("""
            () => {
                const placeholder = document.querySelector('flt-semantics-placeholder');
                if (!placeholder) return;
                const rect = placeholder.getBoundingClientRect();
                placeholder.dispatchEvent(new MouseEvent('click', {
                    bubbles: true,
                    clientX: rect.left + rect.width / 2,
                    clientY: rect.top + rect.height / 2
                }));
            }
            """);
        page.locator("flt-semantics").first().waitFor(BOOTED);
    }

    /**
     * Runs a wait for the app to start. The UAT server sometimes stops sending the app
     * files (main.dart.js, CanvasKit) altogether, leaving the HTML splash up for good; a
     * user would refresh, so if the wait times out the page is reloaded and the wait
     * repeated, up to {@link #START_RELOADS} times (a single reload sometimes stalls too).
     * Each reload is logged with {@link #STALL_WARNING} so reports can flag the run.
     */
    protected void waitForAppStart(Runnable waitForStart) {
        for (int reload = 1; ; reload++) {
            try {
                waitForStart.run();
                return;
            } catch (TimeoutError stalled) {
                if (reload > START_RELOADS) {
                    throw stalled;
                }
                Reporter.log(STALL_WARNING + " - the app did not start within " + Math.round(TestConfig.navigationTimeoutMs() / 1000)
                    + "s (startup files main.dart.js / CanvasKit / fonts not sent). Reloading (" + reload + "/" + START_RELOADS + ").", true);
                page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            }
        }
    }

    /** How many times a stalled app start is reloaded before the test fails. */
    private static final int START_RELOADS = 2;

    /**
     * Waits until the screen's images have downloaded and been drawn, so screenshots show
     * the finished screen rather than empty image frames. Never fails the test: a screen
     * that keeps the network busy is captured as it is after the navigation timeout.
     */
    public void waitForImagesToSettle() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions().setTimeout(TestConfig.navigationTimeoutMs()));
        } catch (TimeoutError stillBusy) {
            Reporter.log("Network still busy after " + Math.round(TestConfig.navigationTimeoutMs() / 1000)
                + "s; continuing", true);
        }
        // Flutter decodes and draws images a few frames after they arrive
        page.waitForTimeout(1_000);
    }

    /** Current hash route, e.g. "/onboarding"; "" before the app has routed. */
    public String route() {
        return routeOf(page.url());
    }

    public void waitForRoute(String route) {
        waitForRouteMatching(r -> r.equals(route));
    }

    protected void waitForRouteMatching(Predicate<String> condition) {
        page.waitForURL(url -> condition.test(routeOf(url)), new Page.WaitForURLOptions()
            .setWaitUntil(WaitUntilState.COMMIT)
            .setTimeout(TestConfig.navigationTimeoutMs()));
    }

    /** Text of the current screen as exposed through semantics (needs {@link #enableSemantics()}). */
    public String semanticsText() {
        return (String) page.evaluate(SEMANTICS_TEXT_JS);
    }

    /** Waits until the semantics text differs from {@code previous}, e.g. after a carousel moves. */
    protected void waitForSemanticsTextChange(String previous, double timeoutMs) {
        page.waitForFunction("previous => (" + SEMANTICS_TEXT_JS + ")() !== previous", previous,
            new Page.WaitForFunctionOptions().setTimeout(timeoutMs));
    }

    /**
     * Each piece of text on the screen, in screen order, as exposed through semantics:
     * a node's label, or its own text (not its children's).
     */
    @SuppressWarnings("unchecked")
    public List<String> semanticsTexts() {
        return (List<String>) page.evaluate("""
            () => {
                const host = document.querySelector('flt-semantics-host');
                if (!host) return [];
                const out = [];
                for (const el of host.querySelectorAll('flt-semantics')) {
                    let own = '';
                    for (const child of el.childNodes) {
                        const tag = child.nodeType === 1 ? child.tagName : '';
                        if (child.nodeType === 3) own += child.textContent;
                        else if (tag !== 'FLT-SEMANTICS' && tag !== 'FLT-SEMANTICS-CONTAINER') own += ' ' + child.textContent;
                    }
                    const text = (el.getAttribute('aria-label') || own).replace(/\\s+/g, ' ').trim();
                    if (text) out.push(text);
                }
                return out;
            }
            """);
    }

    /**
     * The value shown for a label, e.g. "Make & Model" -> "Maruti Swift". Handles both a
     * label and value in separate nodes and in one node ("Make & Model Maruti Swift").
     * Searches after {@code section} when given, so labels repeated elsewhere are skipped.
     * Returns null when the label is not on screen.
     */
    public String valueOf(String label, String section) {
        List<String> texts = semanticsTexts();
        int from = section == null ? 0 : Math.max(0, indexOfText(texts, section));
        for (int i = from; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text.equals(label)) {
                return i + 1 < texts.size() ? texts.get(i + 1) : "";
            }
            if (text.startsWith(label + " ")) {
                return text.substring(label.length()).trim();
            }
        }
        return null;
    }

    private static int indexOfText(List<String> texts, String text) {
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).equals(text) || texts.get(i).startsWith(text + " ")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Text on the screen, however Flutter exposes it: as text (Chromium) or as the label of
     * a node that groups it with other text, e.g. an image labelled "Make &amp; Model\nAUDI A4" (WebKit).
     */
    public Locator textOrLabel(String text) {
        // The label must be the text, or the text followed by its value ("Make & Model\nAUDI A4");
        // not a container whose label merely starts with it ("Your Car DetailsSelect 'Yes'...")
        return page.getByText(text, new Page.GetByTextOptions().setExact(true))
            .or(page.getByLabel(java.util.regex.Pattern.compile("^" + regexQuote(text) + "(\\s|$)")))
            .first();
    }

    /**
     * Escapes text for a regular expression that the browser evaluates. Java's Pattern.quote
     * wraps text in \Q...\E, which JavaScript doesn't understand, so the pattern would never match.
     */
    public static String regexQuote(String text) {
        return text.replaceAll("[\\\\^$.|?*+()\\[\\]{}]", "\\\\$0");
    }

    /** Scrolls back to the top of the screen in steps (one huge scroll is ignored on iOS). */
    public void scrollToTop() {
        for (int step = 0; step < 15; step++) {
            scrollDown(-600);
        }
    }

    /** True when the text is on screen and inside the visible viewport. */
    public boolean isOnScreen(Locator locator) {
        if (locator.count() == 0 || !locator.first().isVisible()) {
            return false;
        }
        BoundingBox box = locator.first().boundingBox();
        return box != null && box.y >= 0 && box.y + box.height <= page.viewportSize().height;
    }

    /** Scrolls the screen down step by step until the locator is on screen; false if it never appears. */
    public boolean scrollUntilOnScreen(Locator locator) {
        for (int step = 0; step <= 12 && !isOnScreen(locator); step++) {
            scrollDown(400);
        }
        return isOnScreen(locator);
    }

    /**
     * Scrolls until the locator is on screen, then moves it to about a third of the way down,
     * so it is fully visible rather than cut off at an edge. False if it never appears.
     */
    public boolean bringIntoView(Locator locator) {
        if (!scrollUntilOnScreen(locator)) {
            return false;
        }
        BoundingBox box = locator.first().boundingBox();
        double offset = box == null ? 0 : box.y - page.viewportSize().height / 3.0;
        if (Math.abs(offset) > 40) {
            scrollDown((int) offset);
        }
        return isOnScreen(locator);
    }

    /**
     * Scrolls the Flutter view by the given pixels (positive = down). Uses the mouse wheel;
     * Playwright's mobile WebKit (iPhone) has no mouse wheel, so there the same wheel event
     * is sent to the Flutter view's root element (flutter-view / flt-glass-pane), where Flutter
     * listens for it. Sending it to whatever element is at the point instead could hit an
     * accessibility node that swallows it, and the page would stop scrolling part way.
     */
    public void scrollDown(int pixels) {
        var viewport = page.viewportSize();
        double x = viewport.width / 2.0;
        double y = viewport.height / 2.0;
        try {
            page.mouse().move(x, y);
            page.mouse().wheel(0, pixels);
        } catch (PlaywrightException noMouseWheel) {
            page.evaluate("""
                ([x, y, dy]) => {
                    const target = document.querySelector('flutter-view, flt-glass-pane')
                        || document.elementFromPoint(x, y) || document.body;
                    target.dispatchEvent(new WheelEvent('wheel', {
                        deltaY: dy, deltaMode: 0, clientX: x, clientY: y,
                        bubbles: true, cancelable: true, composed: true
                    }));
                }
                """, List.of(x, y, pixels));
        }
        page.waitForTimeout(400);
    }

    /**
     * Taps the middle of an element like a finger. On iOS (WebKit) neither clicking a Flutter
     * semantics node nor a mouse press reliably reaches the app (e.g. the header back arrow);
     * a touch on its position does.
     */
    public void pressOn(Locator locator) {
        BoundingBox box = locator.boundingBox();
        tapAt(box.x + box.width / 2, box.y + box.height / 2);
    }

    /** Taps a point on the screen (touch on phones), for icons Flutter does not label. */
    public void tapAt(double x, double y) {
        if (Boolean.TRUE.equals(page.evaluate("() => navigator.maxTouchPoints > 0"))) {
            page.touchscreen().tap(x, y);
        } else {
            page.mouse().click(x, y);
        }
    }

    /**
     * The app's route in a URL. Builds up to 2.0.4 route on the hash ({@code /#/onboarding});
     * UAT 2.0.5+7 uses plain paths ({@code /onboarding}). Both give "/onboarding"; the bare
     * start page ({@code /} or {@code /#/}) gives "".
     */
    public static String routeOf(String url) {
        if (!url.matches("(?i)^https?://.*")) {
            return "";   // about:blank before the first navigation
        }
        int hash = url.indexOf('#');
        String route;
        if (hash >= 0 && url.length() > hash + 1 && url.charAt(hash + 1) == '/') {
            route = url.substring(hash + 1);
        } else {
            String rest = url.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/?#]*", "");
            route = hash >= 0 ? rest.substring(0, Math.max(0, rest.indexOf('#'))) : rest;
        }
        route = route.replaceAll("[?#].*$", "");
        return route.equals("/") ? "" : route;
    }
}
