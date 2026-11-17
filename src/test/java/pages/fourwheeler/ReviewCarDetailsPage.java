package pages.fourwheeler;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import config.TestConfig;
import pages.FlutterPage;
import support.ScreenPixels;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 4 Wheeler journey, review step shown after "Fetch Car Details": the "Your Car Details" card
 * with the fetched values, the "Previous Policy Details" section, the personal-use
 * confirmation and "View Prices".
 */
public class ReviewCarDetailsPage extends FlutterPage {

    /** Route of this screen (the vehicle number screen before it is #/journey/motor/fourWheeler). */
    public static final String ROUTE = "/4w-vehicle-details";

    public static final String YOUR_CAR_DETAILS = "Your Car Details";
    public static final String PREVIOUS_POLICY_DETAILS = "Previous Policy Details";

    // Labels on the "Your Car Details" card, as the app spells them
    public static final String MAKE_AND_MODEL = "Make & Model";
    public static final String VARIANT_AND_FUEL_TYPE = "Variant & Fuel Type";
    public static final String RTO_CITY = "RTO - City";
    public static final String REGISTRATION_DATE = "Registration Date";
    public static final String MANUFACTURING_DATE = "Manufacturing Date";
    /** Label build 2.0.4+8 shows for the manufacturing date. */
    public static final String MFG_YEAR_AND_MONTH = "Mfg Year and Month";
    public static final String OWNED_BY = "Owned By";
    public static final String OWNERSHIP_TRANSFERRED = "Ownership transferred in last 1 year?";

    private static final List<String> CARD_LABELS = List.of(MAKE_AND_MODEL, VARIANT_AND_FUEL_TYPE, RTO_CITY,
        REGISTRATION_DATE, MANUFACTURING_DATE, MFG_YEAR_AND_MONTH, OWNED_BY, OWNERSHIP_TRANSFERRED);

    public static final String OWNERSHIP_HELPER_TEXT = "Based on ownership transfers recorded in RC";
    public static final String PERSONAL_USE_TEXT =
        "I confirm that this vehicle is for personal use and not a commercial vehicle.";
    public static final String FOOTER_NOTE =
        "Your vehicle details help insurers personalise the right protection and pricing";

    /** Shown under the checkbox after the user says the car is commercial. */
    public static final String COMMERCIAL_WARNING =
        "This journey is applicable only for private vehicles. Commercial vehicle journey is coming soon";

    /** Messages the app shows when details can't be fetched or are incomplete. */
    public static final List<String> FETCH_PROBLEM_MESSAGES =
        List.of("Vehicle Details Not Found", "Could not fetch vehicle details.");

    private final Locator yourCarDetailsTitle = textOrLabel(YOUR_CAR_DETAILS);
    private final Locator previousPolicyTitle = textOrLabel(PREVIOUS_POLICY_DETAILS);
    private final Locator personalUseCheckbox = page.getByRole(AriaRole.CHECKBOX).first();
    private final Locator viewPricesButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("View Prices")).first();
    private final Locator backButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back")).first();
    private final Locator closeButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).first();
    /**
     * The "Is it a commercial vehicle?" dialog: its X ("Dismiss" button in Chromium), or its
     * title / question, which WebKit exposes as labels instead.
     */
    private final Locator commercialDialog = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dismiss"))
        .or(page.getByLabel("Dismiss"))
        .or(textOrLabel("Is it a commercial vehicle?"))
        .or(textOrLabel("Do you want to insure commercial car?"))
        .first();

    public ReviewCarDetailsPage(Page page) {
        super(page);
    }

    /** Waits for the fetched details to arrive (vehicle lookup is a network call, so navigation timeout). */
    public ReviewCarDetailsPage waitUntilLoaded() {
        yourCarDetailsTitle.waitFor(new Locator.WaitForOptions().setTimeout(TestConfig.navigationTimeoutMs()));
        waitForImagesToSettle();
        return this;
    }

    /**
     * Value shown for a "Your Car Details" label; null if the label is not on screen, "" if no
     * value arrived. Some values (e.g. RTO - City) load after the card appears, showing
     * loading dots meanwhile, so this waits up to the action timeout for a real value - never
     * taking the next label as the value.
     */
    public String carDetail(String label) {
        if (valueOf(label, YOUR_CAR_DETAILS) == null) {
            bringIntoView(textOrLabel(label));
        }
        long deadline = System.currentTimeMillis() + (long) TestConfig.defaultTimeoutMs();
        String value;
        do {
            value = valueOf(label, YOUR_CAR_DETAILS);
            if (value != null && isAnotherField(value)) {
                // Still loading: the next field (e.g. "Registration Date 26/10/2022") was read instead
                value = "";
            }
            if (value == null || !value.isBlank()) {
                return value;
            }
            page.waitForTimeout(500);
        } while (System.currentTimeMillis() < deadline);
        return value;
    }

    /** True when the text is a card label, alone or merged with its value ("Owned By Individual"). */
    private static boolean isAnotherField(String text) {
        return CARD_LABELS.stream().anyMatch(label -> text.equals(label) || text.startsWith(label + " "));
    }

    /**
     * The first of the labels found on the card, looking at the current screen first and only
     * then scrolling down; null if none appears. Use for fields whose label differs by build.
     */
    public String firstLabelShown(String... labels) {
        for (int step = 0; step <= 12; step++) {
            for (String label : labels) {
                if (valueOf(label, null) != null) {
                    return label;
                }
            }
            scrollDown(400);
        }
        return null;
    }

    /** Full-width strip of the screen holding a section title, where its completion tick sits. */
    public BoundingBox titleRow(String sectionTitle) {
        Locator title = textOrLabel(sectionTitle);
        bringIntoView(title);
        return title.boundingBox();
    }

    /**
     * Taps the Edit (pencil) icon on the "Your Car Details" card: the "Edit" button when
     * Flutter labels it, else the pencil spot just right of the card title (the green
     * completion tick sits at the far right of that row, so not there).
     */
    public void tapEditCarDetails() {
        BoundingBox title = titleRow(YOUR_CAR_DETAILS);
        Locator edit = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit"));
        for (int i = 0; i < edit.count(); i++) {
            BoundingBox box = edit.nth(i).boundingBox();
            if (box != null && Math.abs((box.y + box.height / 2) - (title.y + title.height / 2)) < title.height) {
                edit.nth(i).click();
                return;
            }
        }
        tapAt(title.x + title.width + 20, title.y + title.height / 2);
    }

    /** Vertical centre of the header bar (the "Car Insurance" title), for unlabelled header icons. */
    private double headerCentreY() {
        Locator headerTitle = page.getByText(Pattern.compile("^(Car|Motor) Insurance$")).first();
        BoundingBox box = headerTitle.count() > 0 ? headerTitle.boundingBox() : null;
        return box != null ? box.y + box.height / 2 : 71;
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
     * Taps the header Close (X) ("Close" button, else the top-right icon spot). If the app
     * asks to confirm leaving, confirms with "Leave". Returns true when that dialog appeared.
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
        } catch (com.microsoft.playwright.TimeoutError noDialog) {
            return false;
        }
    }

    /** Taps the "Previous Policy Details" section to open it. */
    public void openPreviousPolicyDetails() {
        bringIntoView(previousPolicyTitle);
        previousPolicyTitle.click();
    }

    /**
     * Ticks or unticks the personal use confirmation. The box opens ticked; unticking it makes
     * the app ask "Is it a commercial vehicle?", which is answered "Yes" (that is what unticks it;
     * "No" keeps it ticked).
     */
    public void setPersonalUseConfirmed(boolean confirmed) {
        bringIntoView(personalUseCheckbox);
        if (personalUseCheckbox.isChecked() == confirmed) {
            return;
        }
        if (confirmed) {
            tapPersonalUseCheckbox();
            if (isCommercialDialogShown()) {
                answerCommercialDialog(false);
            }
        } else if (openCommercialDialog()) {
            answerCommercialDialog(true);
        }
        page.waitForCondition(() -> personalUseCheckbox.isChecked() == confirmed,
            new Page.WaitForConditionOptions().setTimeout(5_000));
    }

    /** Taps the checkbox once the screen has stopped scrolling (a tap during the scroll can be lost). */
    public void tapPersonalUseCheckbox() {
        bringIntoView(personalUseCheckbox);
        page.waitForTimeout(500);
        personalUseCheckbox.click();
    }

    /**
     * Taps the ticked checkbox to untick it and waits for the "Is it a commercial vehicle?"
     * dialog; taps once more if the first tap was lost. True when the dialog is open.
     */
    public boolean openCommercialDialog() {
        bringIntoView(personalUseCheckbox);
        page.waitForTimeout(500);
        // One touch on the box's spot: on iOS (WebKit) neither a click on the checkbox's
        // semantics node nor a mouse press reliably reaches the app, a touch does. Only one:
        // a second tap would land on the dialog's barrier and close it again
        BoundingBox box = personalUseCheckbox.boundingBox();
        org.testng.Reporter.log(String.format("Personal use checkbox at x=%.0f y=%.0f %.0fx%.0f; tapping its centre",
            box.x, box.y, box.width, box.height), true);
        tapAt(box.x + box.width / 2, box.y + box.height / 2);
        return isCommercialDialogShown();
    }

    /**
     * True when "View Prices" is disabled: by its accessibility state, or by its look - the app
     * greys the button out but doesn't tell semantics it is disabled.
     */
    public boolean isViewPricesDisabled() {
        if (viewPricesButton.isDisabled()) {
            return true;
        }
        BoundingBox box = viewPricesButton.boundingBox();
        byte[] button = ScreenPixels.capture(page, box.x + box.width * 0.05, box.y + box.height * 0.2,
            box.width * 0.2, box.height * 0.6);
        return ScreenPixels.isGrey(button);
    }

    /**
     * True within 5s when the "Is it a commercial vehicle?" dialog (Yes / No / Dismiss) is open.
     * Chromium exposes the dialog to semantics; WebKit doesn't, so there it is found on screen.
     */
    public boolean isCommercialDialogShown() {
        long deadline = System.currentTimeMillis() + 8_000;
        while (System.currentTimeMillis() < deadline) {
            if (commercialDialog.count() > 0 || dialogBox() != null) {
                return true;
            }
            page.waitForTimeout(250);
        }
        return false;
    }

    /**
     * Answers the "Is it a commercial vehicle?" dialog: Yes (commercial) or No (personal).
     * Taps the labelled option when Flutter exposes it, else the option's spot in the dialog
     * (both phones: options 82% down the dialog, Yes at 37% and No at 56% of the screen width).
     */
    public void answerCommercialDialog(boolean commercial) {
        String answer = commercial ? "Yes" : "No";
        if (commercialDialog.count() > 0) {
            // Dialog options come after the card in the page, so the last "Yes"/"No" is the dialog's
            page.getByText(answer, new Page.GetByTextOptions().setExact(true))
                .or(page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(answer).setExact(true)))
                .or(page.getByLabel(answer, new Page.GetByLabelOptions().setExact(true)))
                .last().click();
        } else {
            double[] box = dialogBox();
            if (box == null) {
                throw new AssertionError("The \"Is it a commercial vehicle?\" dialog is not on screen");
            }
            tapAt(page.viewportSize().width * (commercial ? 0.373 : 0.563), box[0] + (box[1] - box[0]) * 0.82);
        }
        page.waitForCondition(() -> commercialDialog.count() == 0 && dialogBox() == null,
            new Page.WaitForConditionOptions().setTimeout(5_000));
    }

    /** {top, bottom} in CSS pixels of a dialog drawn over the darkened screen, or null. */
    private double[] dialogBox() {
        byte[] screen = page.screenshot();
        int[] rows = ScreenPixels.centredDialogRows(screen);
        if (rows == null) {
            return null;
        }
        double scale = ScreenPixels.scale(screen, page.viewportSize().width);
        return new double[] {rows[0] / scale, rows[1] / scale};
    }

    public Locator commercialWarning() {
        return textOrLabel(COMMERCIAL_WARNING);
    }

    /**
     * After "View Prices" the app asks about "Personal Accident Cover - Owner Driver" (mandatory by
     * law) before showing quotes. Keeps the cover (no opt-out reason) and taps Confirm.
     * Returns true when the sheet appeared.
     */
    public boolean confirmPersonalAccidentCoverIfAsked() {
        Locator sheet = textOrLabel("Personal Accident Cover - Owner Driver")
            .or(page.getByText(Pattern.compile("Don.t want to opt for PA cover\\?"))).first();
        try {
            sheet.waitFor(new Locator.WaitForOptions().setTimeout(15_000));
        } catch (com.microsoft.playwright.TimeoutError notAsked) {
            return false;
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm")).last().click();
        return true;
    }

    /** True when the screen shows the quotes listing ("Insurer Listing" / quotes route). */
    public boolean isQuotesListingShown() {
        return route().contains("/quotes/")
            || page.getByText(Pattern.compile("Insurer Listing|plans for you")).count() > 0;
    }

    public Locator yourCarDetailsTitle() {
        return yourCarDetailsTitle;
    }

    public Locator previousPolicyTitle() {
        return previousPolicyTitle;
    }

    public Locator personalUseCheckbox() {
        return personalUseCheckbox;
    }

    public Locator viewPricesButton() {
        return viewPricesButton;
    }

    public Locator backButton() {
        return backButton;
    }

    public Locator closeButton() {
        return closeButton;
    }

    public Locator text(String text) {
        return textOrLabel(text);
    }
}
