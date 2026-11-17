package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import config.TestConfig;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import pages.BasePage;
import pages.fourwheeler.CarNumberPage;
import pages.fourwheeler.QuotesListingPage;
import pages.fourwheeler.ReviewCarDetailsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared steps for Sprint 3 (BD2M-761), SC_01 - Module: 4 Wheeler - Quotes Listing / Product Comparison.
 * Module: Customer Creation &amp; other functionality, requirement US-UCM-01, designed by Sumit.
 * Target devices (sheet): Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26).
 *
 * Reaches the quotes listing the way a user does: fetch the test car (fourWheeler.registrationNumber
 * in config.properties), confirm personal use, tap View Prices, keep the Personal Accident Cover
 * and confirm. Builds on the review screen steps of the Review Car module.
 */
public abstract class QuotesListingTestBase extends FourWheelerReviewTestBase {

    /** Insurers are asked for prices one by one, so quotes get up to two minutes. */
    protected static final double QUOTES_TIMEOUT_MS = 120_000;

    /** Buttons that apply a change in a panel or editor. */
    private static final Pattern APPLY = Pattern.compile("^(Apply|Update|Done|Confirm|Save|Submit|Proceed|Recalculate)$",
        Pattern.CASE_INSENSITIVE);

    /** Car details read from the review screen before going to the quotes. */
    protected String selectedVehicle;
    protected String variantAndFuel;
    protected String ownedBy;

    /** Opens the quotes listing for the test car; it may still be fetching quotes. */
    protected QuotesListingPage openQuotesListing() {
        return openQuotesListing(registrationNumber());
    }

    /** Opens the quotes listing for the given car; it may still be fetching quotes. */
    protected QuotesListingPage openQuotesListing(String registration) {
        ReviewCarDetailsPage review = fetchCarDetails(registration);
        selectedVehicle = review.carDetail(ReviewCarDetailsPage.MAKE_AND_MODEL);
        variantAndFuel = review.carDetail(ReviewCarDetailsPage.VARIANT_AND_FUEL_TYPE);
        ownedBy = review.carDetail(ReviewCarDetailsPage.OWNED_BY);
        log("Selected vehicle: " + selectedVehicle + " | " + variantAndFuel + " | owned by " + ownedBy + " (" + registration + ")");

        review.setPersonalUseConfirmed(true);
        review.bringIntoView(review.viewPricesButton());
        review.viewPricesButton().click();
        // The app first asks about Personal Accident Cover (mandatory by law); keep it and confirm
        page.waitForTimeout(1_500);
        log("Personal Accident Cover sheet shown and confirmed: " + review.confirmPersonalAccidentCoverIfAsked());

        QuotesListingPage quotes = new QuotesListingPage(page);
        page.waitForCondition(quotes::isShown,
            new Page.WaitForConditionOptions().setTimeout(TestConfig.navigationTimeoutMs()));
        log("Quotes listing route: " + quotes.route());
        screenshot("quotes-listing-opened");
        return quotes;
    }

    /**
     * Enters the registration number and fetches the car's details. UAT's vehicle lookup sometimes
     * comes back empty ("Car details not fetched. Please try again or fill details manually.");
     * as the message says, it is tried again, up to {@link #CAR_FETCH_RETRIES} times. If it still
     * fails the test is skipped as blocked by UAT - the quotes can't be reached without the car.
     */
    private ReviewCarDetailsPage fetchCarDetails(String registration) {
        for (int attempt = 0; ; attempt++) {
            CarNumberPage entry = new CarNumberPage(page).open();
            screenshot("car-number-screen");
            log("Fetching car details for " + registration);
            ReviewCarDetailsPage review = entry.fetchCarDetails(registration);
            page.waitForTimeout(1_000);
            screenshot("review-screen");
            if (!CAR_NOT_FETCHED.matcher(review.semanticsText()).find()) {
                return review;
            }
            if (attempt >= CAR_FETCH_RETRIES) {
                throw new SkipException("Blocked by UAT: \"Car details not fetched. Please try again or fill details manually.\" for "
                    + registration + " on " + (attempt + 1) + " tries - the vehicle lookup returned no details, so the quotes can't be reached");
            }
            Reporter.log(BasePage.STALL_WARNING + " - car details not fetched for " + registration + ". Fetching again ("
                + (attempt + 1) + "/" + CAR_FETCH_RETRIES + ").", true);
        }
    }

    private static final Pattern CAR_NOT_FETCHED = Pattern.compile("car details not fetched", Pattern.CASE_INSENSITIVE);
    private static final int CAR_FETCH_RETRIES = 2;

    /** Opens the quotes listing and waits until the quotes are displayed; fails if none come. */
    protected QuotesListingPage openLoadedQuotes() {
        QuotesListingPage quotes = openQuotesListing();
        boolean finished = quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        screenshot("quotes");
        Assert.assertTrue(finished, "Quotes were not fetched within " + Math.round(QUOTES_TIMEOUT_MS / 1000)
            + "s - the listing still shows \"" + QuotesListingPage.FETCHING_MESSAGE + "\"");
        Assert.assertFalse(quotes.premiums().isEmpty(), "No quotes are displayed for " + selectedVehicle
            + ". Screen text: " + quotes.semanticsText());
        log(quotes.premiums().size() + " quote(s): " + quotes.premiums());
        if (quotes.dismissIndicativeToast()) {
            log("Closed the \"This premium is indicative ...\" toast covering the header");
        }
        return quotes;
    }

    /** Fuel type: the last word of "Variant &amp; Fuel Type", e.g. "ZXI PETROL" -&gt; "PETROL". */
    protected String fuelType() {
        String[] words = variantAndFuel.trim().split("\\s+");
        return words[words.length - 1];
    }

    /** Variant: "Variant &amp; Fuel Type" without its last word, e.g. "VDI ABS DIESEL" -&gt; "VDI ABS". */
    protected String variant() {
        String value = variantAndFuel.trim();
        int lastSpace = value.lastIndexOf(' ');
        return lastSpace < 0 ? value : value.substring(0, lastSpace);
    }

    /** Fails unless the vehicle summary card on the quotes listing shows {@code expected} for {@code what}. */
    protected void assertSummaryShows(QuotesListingPage quotes, String what, String expected) {
        quotes.scrollToTop();
        screenshot("vehicle-summary");
        boolean shown = quotes.shows(expected);
        log(what + " expected on the vehicle summary card: \"" + expected + "\" - " + (shown ? "shown" : "NOT shown"));
        Assert.assertTrue(shown, "The vehicle summary card does not display the " + what + " \"" + expected
            + "\" of the selected vehicle. Screen text: " + quotes.semanticsText());
    }

    /**
     * Taps an element and returns the texts that appeared because of it (a panel, sheet, editor
     * or new screen). The element is brought into view first.
     */
    protected List<String> tapAndCollectNewTexts(QuotesListingPage quotes, Locator target, String name) {
        Assert.assertTrue(target.count() > 0, "\"" + name + "\" is not on the quotes listing. Screen text: "
            + quotes.semanticsText());
        quotes.bringIntoView(target);
        List<String> before = quotes.semanticsTexts();
        quotes.pressOn(target);
        page.waitForTimeout(2_000);
        quotes.waitForImagesToSettle();
        List<String> after = quotes.semanticsTexts();
        List<String> added = new ArrayList<>(after);
        added.removeAll(before);
        log("Tapped \"" + name + "\": " + added.size() + " new text(s) " + added + "; route " + quotes.route());
        screenshot("after-tapping-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        return added;
    }

    /** The insurer whose name is the nearest above the given height (CSS px) - the quote card it belongs to. */
    protected static String insurerAbove(QuotesListingPage quotes, double y) {
        String best = null;
        double bestY = -1;
        for (String insurer : quotes.insurerNames().stream().distinct().toList()) {
            Locator name = quotes.text(insurer);
            BoundingBox box = name.count() > 0 ? name.boundingBox() : null;
            if (box != null && box.y < y && box.y > bestY) {
                best = insurer;
                bestY = box.y;
            }
        }
        return best;
    }

    /**
     * Taps a bottom action bar item (Add ons / Sort / Filters) and fails unless its panel opens:
     * new content appears with the panel's own controls (options, Apply / Reset / Clear / Close).
     * Returns the panel's texts.
     */
    protected List<String> assertOpensPanel(QuotesListingPage quotes, String item) {
        int editableBefore = editableCount();
        List<String> panel = tapAndCollectNewTexts(quotes, quotes.button(item), item);
        boolean controls = editableCount() > editableBefore || panel.stream()
            .anyMatch(t -> t.matches("(?is).*(apply|reset|clear|close|cancel|done|select|low to high|high to low).*"));
        Assert.assertTrue(!panel.isEmpty() && controls, "Tapping \"" + item + "\" in the bottom action bar did not open the "
            + item + " panel. New texts: " + panel);
        return panel;
    }

    /** Input fields, dropdowns, sliders, spin buttons and radio buttons on screen. */
    protected int editableCount() {
        int count = 0;
        for (AriaRole role : List.of(AriaRole.TEXTBOX, AriaRole.COMBOBOX, AriaRole.SLIDER, AriaRole.SPINBUTTON, AriaRole.RADIO)) {
            count += page.getByRole(role).count();
        }
        return count;
    }

    /**
     * Opens an editable control and fails unless something to edit it appears: an input, slider,
     * dropdown or options, or editing texts (Apply / Update / min / max ...). Returns the new texts.
     */
    protected List<String> assertOpensEditor(QuotesListingPage quotes, Locator control, String name) {
        int editableBefore = editableCount();
        List<String> added = tapAndCollectNewTexts(quotes, control, name);
        int editableAfter = editableCount();
        log(name + ": " + editableAfter + " editable control(s) after the tap (before " + editableBefore + ")");
        boolean editingTexts = added.stream().anyMatch(t -> t.matches("(?is).*(apply|update|done|confirm|save|enter|min|max|recommended|select).*"));
        Assert.assertTrue(editableAfter > editableBefore || editingTexts, "Tapping the " + name
            + " control did not open anything to edit it. New texts: " + added);
        return added;
    }

    /** Taps the first visible Apply / Update / Done / Confirm ... button; returns its name, or null. */
    protected String tapApplyIfShown() {
        Locator apply = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(APPLY));
        for (int i = 0; i < apply.count(); i++) {
            if (apply.nth(i).isVisible()) {
                String name = apply.nth(i).getAttribute("aria-label");
                apply.nth(i).click();
                log("Tapped \"" + name + "\"");
                return name;
            }
        }
        return null;
    }

    /**
     * Taps the "✕" of the first add-on chip - a "✕" or "Remove" button, else the chip's right end -
     * and returns the chip's text. Fails when no add-on chip is shown.
     */
    protected String removeFirstAddOnChip(QuotesListingPage quotes) {
        Locator chip = quotes.addOnChip();
        Assert.assertTrue(chip.count() > 0, "No add-on chip is displayed on the quotes listing. Screen text: " + quotes.semanticsText());
        quotes.bringIntoView(chip);
        String label = chip.getAttribute("aria-label");
        String text = (label != null && !label.isBlank() ? label : chip.textContent()).replace("✕", "").trim();
        screenshot("add-on-chip");
        Locator remove = quotes.text("✕").or(quotes.button("Remove")).first();
        if (remove.count() > 0 && remove.isVisible()) {
            quotes.pressOn(remove);
        } else {
            // The ✕ is drawn at the chip's right end - inside the chip's area, or just right of its label
            BoundingBox box = chip.boundingBox();
            quotes.tapAt(box.x + box.width - 10, box.y + box.height / 2);
            page.waitForTimeout(1_500);
            if (quotes.text(text).count() > 0) {
                quotes.tapAt(box.x + box.width + 14, box.y + box.height / 2);
            }
        }
        log("Tapped ✕ on add-on chip \"" + text + "\"");
        page.waitForTimeout(2_000);
        confirmAddOnRemovalIfAsked();
        return text;
    }

    /** Buttons that confirm removing an add-on, in order of preference ("Confirm" can mean "keep it" elsewhere). */
    private static final List<String> REMOVE_CONFIRMATIONS = List.of("Remove", "Yes", "Proceed", "Continue", "Submit", "Confirm");

    /**
     * Reasons the app offers for removing PA Owner Driver cover ("Are you sure? ... please select the
     * reason"); the first is used. They are drawn as radio buttons but not exposed as such.
     */
    private static final List<String> PA_REMOVAL_REASONS = List.of("I already have PA cover", "I don't have driving license");

    /** Title of the confirmation shown when removing an add-on. */
    protected static final String REMOVE_CONFIRMATION_TITLE = "Are you sure?";

    /**
     * Removing an add-on - PA Owner Driver cover is mandatory by law - asks "Are you sure? ... please
     * select the reason", with Proceed disabled until a reason is picked. If a confirmation appears,
     * picks the first reason (a radio / checkbox, else by its text, e.g. "I already have PA cover"),
     * confirms with the most removal-like button and waits for the confirmation to close. Logs what
     * was shown either way.
     */
    private void confirmAddOnRemovalIfAsked() {
        for (String name : REMOVE_CONFIRMATIONS) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() == 0 || !button.first().isVisible()) {
                continue;
            }
            screenshot("remove-add-on-confirmation");
            pickRemovalReason();
            screenshot("remove-add-on-reason-picked");
            button.first().click();
            log("Confirmed removing the add-on with \"" + name + "\"");
            long closeBy = System.currentTimeMillis() + 8_000;
            while (System.currentTimeMillis() < closeBy && page.getByText(REMOVE_CONFIRMATION_TITLE).count() > 0) {
                page.waitForTimeout(250);
            }
            log("Confirmation " + (page.getByText(REMOVE_CONFIRMATION_TITLE).count() == 0 ? "closed" : "STILL OPEN after confirming"));
            page.waitForTimeout(1_000);
            return;
        }
        log("No confirmation asked when removing the add-on");
    }

    private void pickRemovalReason() {
        for (AriaRole role : List.of(AriaRole.RADIO, AriaRole.CHECKBOX)) {
            Locator reason = page.getByRole(role);
            for (int i = 0; i < reason.count(); i++) {
                if (reason.nth(i).isVisible() && !reason.nth(i).isChecked()) {
                    reason.nth(i).click();
                    log("Removal asked for a reason - picked \"" + reason.nth(i).getAttribute("aria-label") + "\"");
                    return;
                }
            }
        }
        QuotesListingPage quotes = new QuotesListingPage(page);
        for (String text : PA_REMOVAL_REASONS) {
            Locator reason = quotes.text(text);
            if (reason.count() > 0 && reason.isVisible()) {
                quotes.pressOn(reason);
                page.waitForTimeout(500);
                log("Removal asked for a reason - picked \"" + text + "\"");
                return;
            }
        }
        log("Removal confirmation offered no reason to pick");
    }

    // ---- IDV editor ("Select your vehicle IDV": Insured Declared Value field, slider, range, Update) ----

    protected static final String IDV_FIELD_LABEL = "Insured Declared Value";
    private static final Pattern INDIAN_AMOUNT = Pattern.compile("\\d{1,3}(,\\d{2})*,\\d{3}");

    /** The IDV the open editor shows: the first rupee amount after the "Insured Declared Value" label. */
    protected long shownIdv(QuotesListingPage quotes) {
        List<String> texts = quotes.semanticsTexts();
        int start = 0;
        while (start < texts.size() && !texts.get(start).contains(IDV_FIELD_LABEL)) {
            start++;
        }
        Pattern rupees = Pattern.compile("₹\\s?" + INDIAN_AMOUNT.pattern());
        for (int i = start; i < texts.size(); i++) {
            java.util.regex.Matcher m = rupees.matcher(texts.get(i));
            if (m.find()) {
                return digits(m.group());
            }
        }
        return 0;
    }

    /**
     * Types an IDV into the editor's field. Flutter's hidden input can't be clicked directly, so the
     * field is tapped where it is drawn (just under its label), cleared and typed into.
     */
    protected void typeIntoIdvField(QuotesListingPage quotes, long idv) {
        BoundingBox label = quotes.text(IDV_FIELD_LABEL).boundingBox();
        quotes.tapAt(page.viewportSize().width / 2.0, label.y + label.height + 35);
        page.waitForTimeout(500);
        page.keyboard().press("ControlOrMeta+A");
        page.keyboard().press("Backspace");
        page.keyboard().type(Long.toString(idv));
        page.waitForTimeout(1_500);
    }

    /** Rupee amounts (Indian digit grouping, e.g. 19,80,100) in the texts, e.g. an editor's range. */
    protected static List<Long> amounts(List<String> texts) {
        List<Long> found = new ArrayList<>();
        for (String text : texts) {
            java.util.regex.Matcher m = INDIAN_AMOUNT.matcher(text);
            while (m.find()) {
                found.add(digits(m.group()));
            }
        }
        return found;
    }

    /** Indian digit grouping, e.g. 500000 -&gt; "5,00,000". */
    protected static String indianFormat(long amount) {
        String s = Long.toString(amount);
        if (s.length() <= 3) {
            return s;
        }
        return s.substring(0, s.length() - 3).replaceAll("\\B(?=(\\d{2})+$)", ",") + "," + s.substring(s.length() - 3);
    }

    protected static long digits(String text) {
        String d = text == null ? "" : text.replaceAll("\\D", "");
        return d.isEmpty() ? 0 : Long.parseLong(d);
    }

    /** Test data from config.properties; skips the test while it is empty. */
    protected String quotesTestData(String key, String what) {
        String value = TestConfig.optional(key);
        if (value.isEmpty()) {
            throw new SkipException(what + " is not provided - set " + key
                + " in src/test/resources/config.properties to run this test case");
        }
        return value;
    }

    /** True when the app has left the 4 Wheeler journey (not on a journey route or the review screen). */
    protected boolean hasExitedFlow(QuotesListingPage quotes) {
        String route = quotes.route();
        return !route.startsWith("/journey") && !route.equals(ReviewCarDetailsPage.ROUTE) && !quotes.isShown();
    }
}
