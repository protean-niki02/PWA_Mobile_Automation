package pages.fourwheeler;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import pages.FlutterPage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 4 Wheeler quotes listing ("Insurer Listing" in build 2.0.4+8, "Product Listing" in the test
 * cases; #/journey/en/motor/fourWheeler/quotes/&lt;leadId&gt;), reached from the review screen
 * through "View Prices" and the Personal Accident Cover sheet.
 *
 * While the insurers are asked for prices it shows "We are fetching the premium from the insurer
 * based on your inputs"; Add ons, Sort and Filters sit in a bottom bar throughout. Above the
 * quotes are a vehicle summary card (Make &amp; Model, Fuel type, Variant, Ownership, Edit pencil),
 * the IDV and coverage-tenure controls and add-on chips. Each quote card shows the insurer logo
 * and name, an optional "Instant issuance" tag, three metrics, the IDV, a premium button,
 * "View Breakup" and a chevron / "Product Details". A quote is recognised by its premium amount.
 *
 * The finished listing was not inspected before these checks were written; texts come from the
 * test cases, so a check that can't find its element fails with the screen's text in the message.
 */
public class QuotesListingPage extends FlutterPage {

    public static final String TITLE = "Insurer Listing";
    public static final String HEADER_TITLE = "Product Listing";
    public static final String FETCHING_MESSAGE = "We are fetching the premium from the insurer based on your inputs";
    public static final String SCREEN_TITLE = "Here are your quotes sorted by price";
    public static final String TITLE_HIGHLIGHT = "sorted by price";
    public static final String ADD_ONS = "Add ons";
    public static final String SORT = "Sort";
    public static final String FILTERS = "Filters";
    public static final String DONT_WISH_TO_PROCEED = "I don't wish to proceed further";
    public static final String VIEW_BREAKUP = "View Breakup";
    public static final String PRODUCT_DETAILS = "Product Details";
    public static final String INSTANT_ISSUANCE = "Instant issuance";
    public static final List<String> METRICS = List.of("Cashless Garages", "Paid-in-Claims", "Policies Sold");

    private static final Pattern FETCHING = Pattern.compile("fetching the premium", Pattern.CASE_INSENSITIVE);
    public static final Pattern PREMIUM = Pattern.compile("(₹|Rs\\.?|INR)\\s?[0-9][0-9,]*(\\.[0-9]+)?");
    private static final Pattern NO_QUOTES = Pattern.compile(
        "no (\\w+ )?(quotes|plans|insurers?|products)( (are|were))? (found|available)|unable to fetch|could not fetch|try again later",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern ERROR = Pattern.compile(
        "something went wrong|unable to (fetch|load|get)|could not (fetch|load|get)|failed to (fetch|load)|try again|error",
        Pattern.CASE_INSENSITIVE);
    public static final Pattern INSURER = Pattern.compile(
        "Lombard|General Insurance|ERGO|Tata AIG|Allianz|Chola|Digit|Acko|Royal Sundaram|SBI General|Reliance|IFFCO|Kotak"
            + "|Liberty|Magma|Navi|Shriram|Universal Sompo|Zuno|Future Generali|National Insurance|Oriental|New India"
            + "|United India|Raheja|Bima Vahan", Pattern.CASE_INSENSITIVE);
    public static final Pattern ADD_ON = Pattern.compile(
        "Zero Dep|Zero depreciation|PA Owner|Owner Driver|PA cover|Personal Accident|Roadside|RSA|Engine Protect|Consumable|Key (Replacement|Protect)"
            + "|Return to Invoice|NCB Protect|Tyre|Passenger", Pattern.CASE_INSENSITIVE);
    public static final Pattern TENURE = Pattern.compile("tenure|policy term|\\b\\d+\\s*(year|yr)s?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADER = Pattern.compile("^(Insurer|Product) Listing$");
    private static final Pattern INDICATIVE_TOAST = Pattern.compile("premium is indicative", Pattern.CASE_INSENSITIVE);

    private final Locator backButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back"));
    private final Locator closeButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close"));

    public QuotesListingPage(Page page) {
        super(page);
    }

    /** True when the app shows the quotes listing (its route or its title). */
    public boolean isShown() {
        return route().contains("/quotes/") || textOrLabel(TITLE).count() > 0 || textOrLabel(HEADER_TITLE).count() > 0;
    }

    /** True while the "We are fetching the premium ..." loader is on screen. */
    public boolean isFetching() {
        return FETCHING.matcher(semanticsText()).find();
    }

    /** True when the listing says there are no quotes (or that fetching them failed). */
    public boolean showsNoQuotes() {
        return NO_QUOTES.matcher(semanticsText()).find();
    }

    /** True when the listing shows an error message. */
    public boolean showsError() {
        return ERROR.matcher(semanticsText()).find();
    }

    /**
     * Waits up to {@code timeoutMs} for fetching to finish: quotes shown, or a "no quotes"
     * message. False when it is still fetching at the end.
     */
    public boolean waitForQuotes(double timeoutMs) {
        try {
            page.waitForCondition(() -> !isFetching() && (!premiums().isEmpty() || showsNoQuotes() || showsError()),
                new Page.WaitForConditionOptions().setTimeout(timeoutMs));
            waitForImagesToSettle();
            return true;
        } catch (TimeoutError stillFetching) {
            return false;
        }
    }

    /**
     * Waits up to {@code timeoutMs} for the quotes to be fetched again after a change: either the
     * loader comes back and finishes, or the premiums change. False when neither happens.
     */
    public boolean waitForRefetch(List<String> premiumsBefore, double timeoutMs) {
        long end = System.currentTimeMillis() + (long) timeoutMs;
        boolean loaderSeen = false;
        List<String> last = null;
        int steadyPolls = 0;
        while (System.currentTimeMillis() < end) {
            boolean fetching = isFetching();
            loaderSeen |= fetching;
            List<String> now = premiums();
            if (fetching || now.isEmpty()) {
                last = null;
                steadyPolls = 0;
            } else {
                // The premiums must hold for a moment: right after a change the old quotes can still
                // be drawn for an instant before the loader appears
                steadyPolls = now.equals(last) ? steadyPolls + 1 : 0;
                last = now;
                if (steadyPolls >= 3 && (loaderSeen || !now.equals(premiumsBefore))) {
                    waitForImagesToSettle();
                    return true;
                }
            }
            page.waitForTimeout(500);
        }
        return false;
    }

    /**
     * The quotes' premiums: the names of the premium buttons on the quote cards (e.g. "₹53,313").
     * IDV amounts (the IDV control, the IDV on each card) are also in rupees and don't count.
     * Before any premium button is drawn, falls back to rupee texts that don't mention IDV.
     */
    public List<String> premiums() {
        List<String> premiums = new ArrayList<>();
        Locator buttons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(PREMIUM));
        for (int i = 0; i < buttons.count(); i++) {
            String name = buttons.nth(i).getAttribute("aria-label");
            name = name == null || name.isBlank() ? buttons.nth(i).innerText() : name;
            if (name != null && !name.contains("IDV")) {
                premiums.add(name.replaceAll("\\s+", " ").trim());
            }
        }
        if (premiums.isEmpty()) {
            textsMatching(PREMIUM).stream().filter(t -> !t.contains("IDV")).forEach(premiums::add);
        }
        return premiums;
    }

    /**
     * Closes the green "This premium is indicative ..." toast at the top of the listing, which
     * covers the header (and its back / close icons) until it goes. True when it was there.
     */
    public boolean dismissIndicativeToast() {
        Locator toast = text(INDICATIVE_TOAST);
        if (toast.count() == 0 || !toast.isVisible()) {
            return false;
        }
        BoundingBox box = toast.boundingBox();
        tapAt(page.viewportSize().width - 40, box.y + box.height / 2);
        try {
            page.waitForCondition(() -> toast.count() == 0 || !toast.isVisible(),
                new Page.WaitForConditionOptions().setTimeout(8_000));
        } catch (TimeoutError stillThere) {
            // Leave it; checks that need the header will report what they find
        }
        return true;
    }

    /** Insurer names shown (texts naming a known insurer). */
    public List<String> insurerNames() {
        return textsMatching(INSURER);
    }

    /** Each piece of text on the screen matching the pattern, shortened to 80 characters. */
    public List<String> textsMatching(Pattern pattern) {
        List<String> found = new ArrayList<>();
        for (String text : semanticsTexts()) {
            if (pattern.matcher(text).find()) {
                found.add(text.length() > 80 ? text.substring(0, 77) + "..." : text);
            }
        }
        return found;
    }

    /** True when the screen's text contains {@code text}, in any letter case. */
    public boolean shows(String text) {
        return semanticsText().toLowerCase().contains(text.toLowerCase());
    }

    /**
     * Text (or a label containing it) on the screen, any letter case. Spaces match any whitespace or
     * none: the app draws the lines of a two-line option as separate pieces, with a line break or
     * nothing between them.
     */
    public Locator text(String text) {
        return text(Pattern.compile(regexQuote(text.trim()).replaceAll("\\s+", "\\\\s*"), Pattern.CASE_INSENSITIVE));
    }

    public Locator text(Pattern pattern) {
        return page.getByText(pattern).or(page.getByLabel(pattern)).first();
    }

    /**
     * A button by its name: one named exactly that (icons aside) first, so "Sort" is not taken for
     * the heading "... sorted by price"; else a text reading exactly that; else a button whose name
     * contains it, else such a text.
     */
    public Locator button(String name) {
        Pattern exact = Pattern.compile("^\\W*" + regexQuote(name.trim()) + "\\W*$", Pattern.CASE_INSENSITIVE);
        Locator named = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(exact));
        if (named.count() > 0) {
            return named.first();
        }
        Locator labelled = text(exact);
        if (labelled.count() > 0) {
            return labelled;
        }
        Pattern pattern = Pattern.compile(regexQuote(name), Pattern.CASE_INSENSITIVE);
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(pattern)).or(text(pattern)).first();
    }

    /** The premium button of the first quote card (a button named with an amount, not the IDV control). */
    public Locator premiumButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName(Pattern.compile("^(?!.*IDV).*(₹|Rs\\.?|INR)\\s?[0-9]"))).first();
    }

    /** The IDV control above the quotes (the first "IDV" on the screen). */
    public Locator idvControl() {
        return text(Pattern.compile("\\bIDV\\b"));
    }

    /** The coverage-tenure control above the quotes. */
    public Locator tenureControl() {
        return text(TENURE);
    }

    /** The first add-on chip (a known add-on name). */
    public Locator addOnChip() {
        return text(ADD_ON);
    }

    /**
     * IDV values on the screen: texts that mention IDV with an amount, or an IDV label followed
     * by an amount.
     */
    public List<String> idvValues() {
        List<String> texts = semanticsTexts();
        List<String> values = new ArrayList<>();
        Pattern amount = Pattern.compile("[0-9][0-9,]{3,}");
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (!text.matches("(?s).*\\bIDV\\b.*")) {
                continue;
            }
            if (amount.matcher(text).find()) {
                values.add(text);
            } else if (i + 1 < texts.size() && amount.matcher(texts.get(i + 1)).find()) {
                values.add(text + " " + texts.get(i + 1));
            }
        }
        return values;
    }

    /** The bottom action bar (where Filters sits); null when it is not on screen. */
    public BoundingBox bottomBar() {
        Locator filters = text(FILTERS);
        return filters.count() > 0 && filters.isVisible() ? filters.boundingBox() : null;
    }

    public Locator headerTitle() {
        return page.getByText(HEADER).first();
    }

    /** Vertical centre of the header bar, for its unlabelled back and close icons. */
    public double headerCentreY() {
        Locator title = headerTitle();
        BoundingBox box = title.count() > 0 ? title.boundingBox() : null;
        return box != null ? box.y + box.height / 2 : 71;
    }

    public Locator backButton() {
        return backButton;
    }

    public Locator closeButton() {
        return closeButton;
    }

    /** Taps the header back arrow ("Back" button, else the top-left icon spot). */
    public void tapBack() {
        if (backButton.count() > 0) {
            pressOn(backButton);
        } else {
            tapAt(34, headerCentreY());
        }
    }

    /**
     * Taps the header Close (X) ("Close" button, else the top-right icon spot). If the app asks to
     * confirm leaving, confirms with "Leave". Returns true when that dialog appeared.
     */
    public boolean tapCloseAndConfirm() {
        if (closeButton.count() > 0) {
            pressOn(closeButton);
        } else {
            tapAt(page.viewportSize().width - 34, headerCentreY());
        }
        Locator leave = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Leave"));
        try {
            leave.first().waitFor(new Locator.WaitForOptions().setTimeout(3_000));
            leave.first().click();
            return true;
        } catch (TimeoutError noDialog) {
            return false;
        }
    }
}
