package pages.fourwheeler;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Reporter;
import pages.FlutterPage;
import pages.LoginPage;

/**
 * 4 Wheeler journey, first step (#/journey/motor/fourWheeler): "Get Your Car Insurance Quote",
 * the vehicle registration number field, "Brand new car?" and "Fetch Car Details".
 */
public class CarNumberPage extends FlutterPage {

    public static final String ROUTE = "/journey/motor/fourWheeler";

    private final Locator registrationField = page.getByRole(AriaRole.TEXTBOX,
            new Page.GetByRoleOptions().setName("Enter your vehicle registration Number"))
        .or(page.getByRole(AriaRole.TEXTBOX)).first();
    private final Locator fetchCarDetailsButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Fetch Car Details"));
    private final Locator brandNewCarButton = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Brand new car?"))
        .or(page.getByText("Brand new car?")).first();

    public CarNumberPage(Page page) {
        super(page);
    }

    /**
     * Opens the journey directly; if the app sends a visitor elsewhere first, goes in the
     * way a user would: login screen > "Private Car Insurance".
     */
    public CarNumberPage open() {
        openApp(ROUTE);
        waitForRouteMatching(route -> !route.isEmpty() && !route.equals("/splash"));
        if (!route().equals(ROUTE)) {
            new LoginPage(page).open();
            page.getByText("Private Car Insurance").first().click();
            waitForRoute(ROUTE);
        }
        fetchCarDetailsButton.waitFor();
        return this;
    }

    /**
     * Enters the number and checks the app took it. On iOS Flutter sets up its text input a
     * moment after the field is tapped, so text sent too early can be lost (the field stays
     * empty and "Fetch Car Details" stays disabled). Retries up to 3 times, typing key by key.
     */
    public CarNumberPage enterRegistrationNumber(String registrationNumber) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            registrationField.click();
            page.waitForTimeout(300 * attempt);
            if (attempt == 1) {
                registrationField.fill(registrationNumber);
            } else {
                page.keyboard().press("Control+A");
                page.keyboard().press("Backspace");
                page.keyboard().type(registrationNumber, new com.microsoft.playwright.Keyboard.TypeOptions().setDelay(50));
            }
            if (numberAccepted(registrationNumber)) {
                return this;
            }
            Reporter.log("Registration number not taken by the app on attempt " + attempt + ", entering it again", true);
        }
        throw new AssertionError("The app did not accept the registration number " + registrationNumber
            + " (field empty or \"Fetch Car Details\" disabled after 3 attempts)");
    }

    /** True within 3s when the field shows the number and "Fetch Car Details" is enabled. */
    private boolean numberAccepted(String registrationNumber) {
        String wanted = registrationNumber.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        long deadline = System.currentTimeMillis() + 3_000;
        while (System.currentTimeMillis() < deadline) {
            String shown;
            try {
                shown = registrationField.inputValue();
            } catch (PlaywrightException notAnInput) {
                shown = wanted;
            }
            if (shown.replaceAll("[^A-Za-z0-9]", "").toUpperCase().contains(wanted)
                && fetchCarDetailsButton.isEnabled()) {
                return true;
            }
            page.waitForTimeout(200);
        }
        return false;
    }

    /** Taps "Fetch Car Details" without waiting for the result (for loading-state checks). */
    public void tapFetchCarDetails() {
        fetchCarDetailsButton.click();
    }

    /** Enters the number, fetches, and waits for the review screen with the fetched details. */
    public ReviewCarDetailsPage fetchCarDetails(String registrationNumber) {
        enterRegistrationNumber(registrationNumber);
        tapFetchCarDetails();
        return new ReviewCarDetailsPage(page).waitUntilLoaded();
    }

    public Locator registrationField() {
        return registrationField;
    }

    public Locator fetchCarDetailsButton() {
        return fetchCarDetailsButton;
    }

    public Locator brandNewCarButton() {
        return brandNewCarButton;
    }
}
