package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import tests.sprint3.bd2m_761.FourWheelerReviewTestBase;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base for BD2M-767 tests on the Confirm Plan screen of the web journey (UAT 2.0.5+7 and later).
 * <p>
 * From UAT 2.0.5+7 the 4 Wheeler journey ({@code /journey/...}) is an ordinary HTML (Next.js) page,
 * not the Flutter canvas, so it is driven through the DOM. Same steps as a user: the car number page
 * (registration number from {@code fourWheeler.registrationNumber}, e.g. KA01MK1244), "Fetch Car Details",
 * the personal-use tick, "View Prices", the Personal Accident Cover sheet's "Confirm", then "Select" on the
 * first quote, which opens {@code /journey/en/motor/fourWheeler/QuoteAddonPage/<leadId>}.
 */
public abstract class ConfirmPlanWebTestBase extends FourWheelerReviewTestBase {

    protected static final String JOURNEY_START = "/journey/motor/fourWheeler";
    protected static final String ROUTE_PART = "/QuoteAddonPage/";
    protected static final String HEADING = "Confirm your Bima Vahan Plan";
    protected static final String OPTIONAL_COVERS = "Optional Covers";
    protected static final String INDICATIVE = "Premiums given are indicative premium";
    protected static final String CONFIRM_PLAN = "Confirm Plan";

    private static final int STEP_TIMEOUT_MS = 90_000;
    private static final int QUOTES_TIMEOUT_MS = 120_000;

    /** The insurer / premium of the quote whose Select button opened Confirm Plan. */
    protected String chosenQuote;

    /**
     * Opens the quotes listing the way a user does (car number, fetch, personal use, View Prices, Personal
     * Accident Cover "Confirm") and waits for the quotes; fails if none come. The listing holds the
     * "Add Extra Protection" add-ons panel.
     */
    protected void openQuotes() {
        String registration = registrationNumber();
        Locator field = page.locator("#vehicleRegistrationNumber");
        // UAT sometimes stalls a script of the page, leaving it drawn but not usable (WebKit more than Chromium):
        // as a user would, reload and try again, up to 3 times
        for (int attempt = 1; ; attempt++) {
            try {
                page.navigate(JOURNEY_START);
                field.waitFor(new Locator.WaitForOptions().setTimeout(STEP_TIMEOUT_MS));
                screenshot("car-number-screen");
                field.click(new Locator.ClickOptions().setTimeout(25_000));
                field.fill(registration);
                break;
            } catch (TimeoutError notUsable) {
                if (attempt >= 3) {
                    throw notUsable;
                }
                log("WARNING: UAT server stall - the car number page was not usable, loading it again (" + attempt + "/2)");
            }
        }
        log("Fetching car details for " + registration);
        Locator fetch = button("Fetch Car Details");
        fetch.click();

        Locator personalUse = page.getByText("I confirm that this vehicle is for personal use");
        personalUse.waitFor(new Locator.WaitForOptions().setTimeout(STEP_TIMEOUT_MS));
        log("Car details: " + bodyText().replaceAll(".*Make & Model (.*?) Owner changed.*", "$1"));
        screenshot("car-details-fetched");
        personalUse.scrollIntoViewIfNeeded();
        personalUse.click();
        Locator viewPrices = button("View Prices");
        viewPrices.scrollIntoViewIfNeeded();
        viewPrices.click();

        // The app first asks about Personal Accident Cover (mandatory by law); keep it and confirm
        Locator confirmPa = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm").setExact(true));
        confirmPa.waitFor(new Locator.WaitForOptions().setTimeout(STEP_TIMEOUT_MS));
        confirmPa.click();

        // The quotes are in when the first quote card ("Know Your Insurer" link) is on the page
        Locator firstCard = page.getByText("Know Your Insurer").first();
        try {
            firstCard.waitFor(new Locator.WaitForOptions().setTimeout(QUOTES_TIMEOUT_MS));
        } catch (TimeoutError e) {
            Assert.fail("No quotes were shown within " + QUOTES_TIMEOUT_MS / 1000 + "s. Screen text: " + bodyText());
        }
        page.waitForTimeout(1_000);
        screenshot("quotes");
        if (quoteButton().count() == 0) {
            String html = (String) firstCard.evaluate("e => { let n = e; for (let i = 0; i < 6 && n.parentElement; i++) n = n.parentElement; return n.outerHTML; }");
            Assert.fail("The first quote card has no premium / Select button the test can find. Card HTML: "
                + html.replaceAll("\\s+", " ").substring(0, Math.min(4000, html.length())));
        }
    }

    /**
     * The button of a quote card that opens Confirm Plan: the premium button ("₹53,313 →") on the card, or
     * "Select" in the builds that label it so.
     */
    protected Locator quoteButton() {
        return page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("^\\s*(Select|₹\\s?[\\d,.]+)\\s*[→›>]?\\s*$")));
    }

    /** Opens Confirm Plan the way a user does (the quotes, then the premium button of the first quote); fails if it does not open. */
    protected void openConfirmPlan() {
        openQuotes();
        Locator select = quoteButton();
        String buttonText = select.first().innerText().replaceAll("\\s+", " ").trim();
        String card;
        try {
            card = select.first().locator("xpath=ancestor::div[contains(., 'Final Premium') or contains(., 'Know Your Insurer')][1]")
                .innerText(new Locator.InnerTextOptions().setTimeout(5_000)).replaceAll("\\s+", " ");
        } catch (RuntimeException e) {
            card = "";
        }
        chosenQuote = card.isEmpty() ? buttonText : card;
        log("Choosing the first quote (button \"" + buttonText + "\"): " + chosenQuote);
        select.first().scrollIntoViewIfNeeded();
        select.first().click();

        page.waitForURL(url -> url.contains(ROUTE_PART), new Page.WaitForURLOptions().setTimeout(STEP_TIMEOUT_MS));
        page.getByText(HEADING).first().waitFor(new Locator.WaitForOptions().setTimeout(STEP_TIMEOUT_MS));
        page.waitForTimeout(1_500);
        log("Confirm Plan route: " + page.url());
        screenshot("confirm-plan-opened");
    }

    protected Locator button(String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name));
    }

    /** How long the quotes may take to come in, in ms. */
    protected static final int QUOTES_WAIT_MS = 120_000;

    /** Waits up to {@code timeoutMs} for the condition; false when it never held. */
    protected boolean waitFor(java.util.function.BooleanSupplier condition, int timeoutMs) {
        try {
            page.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(timeoutMs));
            return true;
        } catch (TimeoutError notYet) {
            return false;
        }
    }

    protected static final String GO_BACK_TO_QUOTES = "Go Back to Quotes";
    protected static final String FINAL_PREMIUM = "Final Premium";
    protected static final String OWNER_DRIVER = "Owner Driver";
    protected static final String HEADING_HIGHLIGHT = "Bima Vahan Plan";
    protected static final String BASE_PLAN = "What's included in your base plan?";

    private static final Pattern FINAL_PREMIUM_AMOUNT = Pattern.compile("Final Premium\\s*₹\\s?([\\d,]+)");
    /** A rupee amount as the app writes it: "₹53,313", "₹1,27,339". */
    protected static final Pattern RUPEES = Pattern.compile("₹\\s?\\d[\\d,]*(\\.\\d+)?");
    /** Wording of a user-facing failure message. */
    protected static final Pattern ERROR_TEXT = Pattern.compile(
        "(?i)something\\s+went\\s+wrong|oops|unable\\s+to|failed|try\\s+again|error\\s+occurred|not\\s+able\\s+to|could\\s*n.t|couldn.t");

    /** The Final Premium on the plan summary, in rupees; 0 when not shown. */
    protected long finalPremium() {
        Matcher m = FINAL_PREMIUM_AMOUNT.matcher(bodyText());
        return m.find() ? Long.parseLong(m.group(1).replace(",", "")) : 0;
    }

    /** The rupee amount in a text, 0 when none. */
    protected static long rupees(String text) {
        Matcher m = RUPEES.matcher(text == null ? "" : text);
        return m.find() ? Long.parseLong(m.group().replaceAll("[^0-9.]", "").replaceAll("\\.\\d+$", "")) : 0;
    }

    /** The "Confirm Plan" button (the breadcrumb and the heading words are not buttons). */
    protected Locator confirmPlanButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CONFIRM_PLAN).setExact(true)).first();
    }

    /** The cover cards' titles of the covers that are selected ("Added"), e.g. [Owner Driver]. */
    protected List<String> addedCovers() {
        Object found = page.evaluate("""
            () => [...document.querySelectorAll('div')]
                .filter(d => d.children.length === 0 && d.textContent.trim() === 'Added')
                .map(d => { const card = d.closest('.self-start'); const t = card && card.querySelector('.font-semibold.text-purple');
                            return t ? t.textContent.trim() : ''; })
            """);
        @SuppressWarnings("unchecked")
        List<String> titles = (List<String>) found;
        return titles;
    }

    /** The "Add" toggle of the first add-on cover (under "Add-ons covers") that is not selected yet. */
    protected Locator addOnToggle() {
        return page.locator("xpath=//div[normalize-space(.)='Add-ons covers']/following::div[normalize-space(.)='Add' and not(*)][1]");
    }

    /** The title of the cover card the toggle belongs to. */
    protected String coverTitleOf(Locator toggle) {
        return (String) toggle.evaluate("e => { const c = e.closest('.self-start'); const t = c && c.querySelector('.font-semibold.text-purple');"
            + " return t ? t.textContent.trim() : ''; }");
    }

    /** Every text on the page (one per leaf element), in screen order. */
    protected List<String> texts() {
        Object found = page.locator("body *:not(:has(*))").evaluateAll(
            "els => els.map(e => (e.innerText || '').trim()).filter(t => t.length > 0)");
        @SuppressWarnings("unchecked")
        List<String> all = (List<String>) found;
        return all;
    }

    /** The area of the journey that scrolls (the page itself does not). */
    protected Locator scrollArea() {
        return page.locator("div.flex-1.overflow-y-auto").first();
    }

    /** The colour of a CSS property of an element as sRGB {r, g, b} (the page may use oklch, so a canvas converts it). */
    protected int[] colourOf(Locator element, String property) {
        Object found = element.evaluate("""
            (e, p) => {
                const cv = document.createElement('canvas'); cv.width = cv.height = 1;
                const ctx = cv.getContext('2d', { willReadFrequently: true });
                ctx.fillStyle = '#000'; ctx.fillStyle = getComputedStyle(e)[p];
                ctx.clearRect(0, 0, 1, 1); ctx.fillRect(0, 0, 1, 1);
                const d = ctx.getImageData(0, 0, 1, 1).data;
                return [d[0], d[1], d[2], d[3]];
            }
            """, property);
        @SuppressWarnings("unchecked")
        List<Number> c = (List<Number>) found;
        return new int[] {c.get(0).intValue(), c.get(1).intValue(), c.get(2).intValue()};
    }

    protected static int saturation(int[] c) {
        return Math.max(c[0], Math.max(c[1], c[2])) - Math.min(c[0], Math.min(c[1], c[2]));
    }

    protected static int brightness(int[] c) {
        return (c[0] + c[1] + c[2]) / 3;
    }

    protected static String hex(int[] c) {
        return c == null ? "none" : String.format("#%02X%02X%02X", c[0], c[1], c[2]);
    }

    /** Indian digit grouping, e.g. 127339 -> "₹1,27,339". */
    protected static String indianFormat(long amount) {
        String s = Long.toString(amount);
        if (s.length() <= 3) {
            return "₹" + s;
        }
        String last3 = s.substring(s.length() - 3);
        String rest = s.substring(0, s.length() - 3).replaceAll("(\\d)(?=(\\d\\d)+$)", "$1,");
        return "₹" + rest + "," + last3;
    }

    /** All visible text of the page, whitespace collapsed. */
    protected String bodyText() {
        return page.locator("body").innerText().replaceAll("\\s+", " ").trim();
    }

    /** Every text on the page that mentions "indicative", for the log when the expected tag is missing. */
    protected List<String> indicativeTexts() {
        Object found = page.locator("body *:not(:has(*))").evaluateAll(
            "els => els.map(e => (e.innerText || '').trim()).filter(t => /indicative/i.test(t))");
        @SuppressWarnings("unchecked")
        List<String> texts = (List<String>) found;
        return texts.stream().distinct().toList();
    }
}
