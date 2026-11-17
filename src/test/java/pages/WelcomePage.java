package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

/**
 * Onboarding screen (#/onboarding): photo grid tiles at the top, the Bima Sugam logo,
 * "Welcome to Bima Sugam", an auto-advancing carousel of slides and "Get Started".
 */
public class WelcomePage extends FlutterPage {

    public static final String ROUTE = "/onboarding";

    /** The carousel shows four indicator dots. */
    public static final int SLIDE_COUNT = 4;

    private static final double SLIDE_CHANGE_TIMEOUT_MS = 15_000;

    private final Locator welcomeHeading = page.getByText("Welcome to Bima Sugam");
    private final Locator getStartedButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Get Started"));

    public WelcomePage(Page page) {
        super(page);
    }

    /** Opens the app and waits until it has moved on from the splash to onboarding. */
    public WelcomePage open() {
        openApp("");
        waitForRoute(ROUTE);
        waitForImagesToSettle();
        return this;
    }

    public Locator welcomeHeading() {
        return welcomeHeading;
    }

    public Locator getStartedButton() {
        return getStartedButton;
    }

    public void getStarted() {
        getStartedButton.click();
    }

    /**
     * Moves to the next onboarding slide and returns its text. Waits for the carousel to
     * advance by itself first; if it doesn't, swipes left once.
     */
    public String nextSlide(String currentSlideText) {
        try {
            waitForSemanticsTextChange(currentSlideText, SLIDE_CHANGE_TIMEOUT_MS);
        } catch (PlaywrightException notAdvanced) {
            swipeLeft();
            waitForSemanticsTextChange(currentSlideText, SLIDE_CHANGE_TIMEOUT_MS);
        }
        return semanticsText();
    }

    private void swipeLeft() {
        var viewport = page.viewportSize();
        double y = viewport.height * 0.6;
        page.mouse().move(viewport.width * 0.8, y);
        page.mouse().down();
        page.mouse().move(viewport.width * 0.2, y, new com.microsoft.playwright.Mouse.MoveOptions().setSteps(10));
        page.mouse().up();
    }
}
