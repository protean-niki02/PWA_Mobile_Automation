package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import support.ImageCompare;
import support.ScreenPixels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SC_01_TC_033 - Verify that the screen matches the provided design across supported device sizes.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The loaded quotes listing is checked at the target device's size and at the smallest and largest
 * supported phone sizes ({@link #SIZES}). At each size the layout must hold together as designed:
 * <ul>
 *   <li>no sideways overflow - the page is no wider than the screen;</li>
 *   <li>header (back arrow, title, Close) present, title and screen title "Here are your quotes ..."
 *       inside the screen;</li>
 *   <li>the first quote card's insurer name, premium button, "Product Details" and "View Breakup"
 *       inside the screen once scrolled to (on a short screen they sit lower in the list);</li>
 *   <li>the bottom bar's Add ons / Sort / Filters fully on screen, at the bottom, not overlapping.</li>
 * </ul>
 * The quotes' data differ per fetch, so whole screens can't be compared with the Figma frames; a
 * screenshot per size is saved for that. If an approved header image is put in
 * src/test/resources/approved-design/quotes-listing/header_WxH.png it is compared with the header
 * (at most 3% of pixels may differ). Truncated insurer names ("...") are logged.
 * <p>
 * Playwright's iOS engine stops drawing after a resize, so on iOS the app is reloaded at each size
 * (the listing fetches its quotes again).
 */
public class SC01_TC033_QuotesDesignAcrossSizesTest extends QuotesListingTestBase {

    /** Extra sizes (CSS px, width x height) next to the target device: smallest and largest supported phones. */
    private static final Map<String, int[]> SIZES = new LinkedHashMap<>();
    static {
        SIZES.put("small phone 360 x 640", new int[] {360, 640});
        SIZES.put("large phone 430 x 932", new int[] {430, 932});
    }
    private static final Path APPROVED = Path.of("src", "test", "resources", "approved-design", "quotes-listing");
    private static final Pattern HERE_ARE = Pattern.compile("Here\\s*are\\s*your\\s*quotes", Pattern.CASE_INSENSITIVE);

    @Test(groups = {"sprint3", "SC_01"}, timeOut = 600_000,
        description = "SC_01_TC_033: Verify that the screen matches the provided design across supported device sizes.")
    public void verifyDesignAcrossDeviceSizes() {
        QuotesListingPage quotes = openLoadedQuotes();
        SoftAssert softly = new SoftAssert();
        var target = page.viewportSize();
        checkLayout(quotes, "target device " + target.width + " x " + target.height, softly);
        for (Map.Entry<String, int[]> size : SIZES.entrySet()) {
            resizeTo(quotes, size.getValue()[0], size.getValue()[1]);
            checkLayout(quotes, size.getKey(), softly);
        }
        softly.assertAll();
    }

    private void resizeTo(QuotesListingPage quotes, int width, int height) {
        page.setViewportSize(width, height);
        if (device.browserEngine().equalsIgnoreCase("webkit")) {
            page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            quotes.enableSemantics();
            page.waitForTimeout(2_000);
            quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        } else {
            page.waitForTimeout(1_500);
            if (quotes.premiums().isEmpty() && page.locator("flt-semantics-placeholder").count() > 0) {
                quotes.enableSemantics();
            }
        }
        quotes.dismissIndicativeToast();
        quotes.scrollToTop();
        quotes.waitForImagesToSettle();
    }

    /** Checks the layout at the current size; every problem found is a soft failure naming the size. */
    private void checkLayout(QuotesListingPage quotes, String size, SoftAssert softly) {
        double width = page.viewportSize().width;
        double height = page.viewportSize().height;
        String name = size.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
        screenshot("layout-" + name);
        List<String> problems = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Number> widths = (List<Number>) page.evaluate("() => [document.documentElement.scrollWidth, window.innerWidth]");
        if (widths.get(0).doubleValue() > widths.get(1).doubleValue() + 1) {
            problems.add(String.format("page is %.0f px wide on a %.0f px screen (sideways overflow)", widths.get(0).doubleValue(),
                widths.get(1).doubleValue()));
        }
        if (quotes.premiums().isEmpty()) {
            problems.add("no quotes are shown");
        }
        inside(quotes.headerTitle(), "header title", width, height, problems);
        // The rest scrolls with the list: on a short screen it may sit lower down, which is fine,
        // so each is scrolled into view and only has to fit the screen's width
        inList(quotes, quotes.text(HERE_ARE), "screen title", width, height, problems);
        List<String> insurers = quotes.insurerNames();
        if (!insurers.isEmpty()) {
            inList(quotes, quotes.text(insurers.get(0)), "insurer name \"" + insurers.get(0) + "\"", width, height, problems);
        }
        inList(quotes, quotes.premiumButton(), "premium button", width, height, problems);
        inList(quotes, quotes.text(QuotesListingPage.PRODUCT_DETAILS), QuotesListingPage.PRODUCT_DETAILS, width, height, problems);
        inList(quotes, quotes.text(QuotesListingPage.VIEW_BREAKUP), QuotesListingPage.VIEW_BREAKUP, width, height, problems);
        quotes.scrollToTop();

        List<BoundingBox> bar = new ArrayList<>();
        for (String item : List.of(QuotesListingPage.ADD_ONS, QuotesListingPage.SORT, QuotesListingPage.FILTERS)) {
            Locator button = quotes.button(item);
            if (inside(button, "\"" + item + "\"", width, height, problems)) {
                BoundingBox box = button.boundingBox();
                if (box.y < height - 160) {
                    problems.add(String.format("\"%s\" is not at the bottom (y %.0f of %.0f)", item, box.y, height));
                }
                bar.add(box);
            }
        }
        for (int i = 1; i < bar.size(); i++) {
            if (bar.get(i).x < bar.get(i - 1).x + bar.get(i - 1).width - 1) {
                problems.add("bottom bar buttons overlap");
            }
        }

        List<String> truncated = quotes.semanticsTexts().stream().filter(t -> t.endsWith("...") || t.endsWith("…")).toList();
        String header = compareHeader(quotes, (int) width, (int) height);
        log(String.format("[%s] %s; truncated texts: %s; approved header: %s", size,
            problems.isEmpty() ? "layout OK" : "problems " + problems, truncated.isEmpty() ? "none" : truncated, header));
        softly.assertTrue(problems.isEmpty(), "The quotes listing layout breaks at " + size + ": " + problems);
        softly.assertFalse(header.startsWith("differs"), "At " + size + " the header does not match the approved design: " + header);
    }

    /** A list item: scrolled into view, then it must be fully inside the screen. */
    private static void inList(QuotesListingPage quotes, Locator element, String what, double width, double height, List<String> problems) {
        if (element.count() > 0) {
            quotes.bringIntoView(element);
        }
        inside(element, what, width, height, problems);
    }

    /** True when the element is shown and fully inside the screen; otherwise records why not. */
    private static boolean inside(Locator element, String what, double width, double height, List<String> problems) {
        BoundingBox box = element.count() > 0 ? element.first().boundingBox() : null;
        if (box == null) {
            problems.add(what + " not shown");
            return false;
        }
        if (box.x < -1 || box.x + box.width > width + 1) {
            problems.add(String.format("%s cut off at the side (x %.0f-%.0f of %.0f)", what, box.x, box.x + box.width, width));
            return false;
        }
        if (box.y + box.height > height + 1) {
            problems.add(String.format("%s below the screen (y %.0f-%.0f of %.0f)", what, box.y, box.y + box.height, height));
            return false;
        }
        return true;
    }

    /** Compares the header with an approved image for this size, when one is provided. */
    private String compareHeader(QuotesListingPage quotes, int width, int height) {
        Path approved = APPROVED.resolve("header_" + width + "x" + height + ".png");
        BoundingBox title = quotes.headerTitle().count() > 0 ? quotes.headerTitle().boundingBox() : null;
        double bottom = title == null ? 110 : title.y + title.height + 30;
        byte[] actual = ScreenPixels.capture(page, 0, 0, width, bottom);
        saveEvidence("header_" + width + "x" + height + ".png", actual);
        if (!Files.exists(approved)) {
            return "none provided (" + approved + ")";
        }
        try {
            ImageCompare.Result result = ImageCompare.compare(Files.readAllBytes(approved), actual, 0.03);
            return (result.matches() ? "matches - " : "differs - ") + result.detail();
        } catch (IOException e) {
            return "could not read " + approved + ": " + e.getMessage();
        }
    }
}
