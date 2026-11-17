package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Login screen (#/login): mobile number entry and "Get OTP". */
public class LoginPage extends FlutterPage {

    public static final String ROUTE = "/login";

    private final Locator mobileNumberField = page.getByLabel("Enter Mobile Number")
        .or(page.getByText("Enter Mobile Number")).first();
    private final Locator getOtpButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Get OTP"));

    public LoginPage(Page page) {
        super(page);
    }

    /**
     * Opens #/login directly. A first-time visitor may be sent to onboarding instead,
     * in which case this continues through "Get Started" like a user would.
     */
    public LoginPage open() {
        openApp(ROUTE);
        if (!route().equals(ROUTE)) {
            WelcomePage welcome = new WelcomePage(page);
            waitForRouteMatching(route -> route.equals(ROUTE) || route.equals(WelcomePage.ROUTE));
            if (route().equals(WelcomePage.ROUTE)) {
                welcome.getStarted();
            }
            waitForRoute(ROUTE);
        }
        waitForImagesToSettle();
        return this;
    }

    public Locator mobileNumberField() {
        return mobileNumberField;
    }

    public Locator getOtpButton() {
        return getOtpButton;
    }
}
