package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The splash screen the user lands on after launch (BD2M-683): an auto-scrolling image grid
 * at the top, the Bima Sugam logo, "Welcome to Bima Sugam", the tagline, the support line, an
 * illustration, the primary and secondary action buttons and a decorative background shape.
 *
 * In the app this is the first screen after the launch splash ({@link SplashScreen}); build
 * 2.0.4+8 routes it as #/onboarding, so the route is not fixed here.
 */
public class SplashLandingScreen extends FlutterPage {

    public static final String WELCOME_HEADING = "Welcome to Bima Sugam";
    public static final String TAGLINE = "Of the Industry . By the Industry . For the Nation";
    public static final String SUPPORT_LINE = "Start your journey to complete financial security.";

    /** The tagline's three phrases in order, whatever separates them (".", "·", "|", spaces). */
    private static final Pattern TAGLINE_PATTERN = Pattern.compile(
        "Of the Industry\\W*By the Industry\\W*For the Nation", Pattern.CASE_INSENSITIVE);

    /** The support line, with or without its full stop. */
    private static final Pattern SUPPORT_LINE_PATTERN = Pattern.compile(
        "Start your journey to complete financial security\\.?", Pattern.CASE_INSENSITIVE);

    /**
     * The image grid ends above the logo, which sits above the heading: the grid's bottom
     * edge is at about this share of the heading's distance from the top.
     */
    private static final double GRID_BOTTOM_SHARE_OF_HEADING_TOP = 0.7;

    /** Action buttons are at least this tall (CSS px); carousel dots and icons are smaller. */
    private static final double MIN_ACTION_BUTTON_HEIGHT = 28;

    public SplashLandingScreen(Page page) {
        super(page);
    }

    /** Launches the app and waits until it has moved on from the launch splash to this screen. */
    public SplashLandingScreen open() {
        openApp("");
        waitForLanding();
        return this;
    }

    /** Waits until the app has left the launch splash and this screen's images have arrived. */
    public void waitForLanding() {
        waitForRouteMatching(route -> !route.isEmpty() && !route.equals(SplashScreen.FLUTTER_SPLASH_ROUTE));
        welcomeHeading().waitFor();
        waitForImagesToSettle();
    }

    public Locator welcomeHeading() {
        return textOrLabel(WELCOME_HEADING);
    }

    public Locator tagline() {
        return matching(TAGLINE_PATTERN);
    }

    public Locator supportLine() {
        return matching(SUPPORT_LINE_PATTERN);
    }

    /** True when the tagline is in the screen's text, also when Flutter merges it with other text. */
    public boolean showsTagline() {
        return tagline().count() > 0 || TAGLINE_PATTERN.matcher(semanticsText()).find();
    }

    public boolean showsSupportLine() {
        return supportLine().count() > 0 || SUPPORT_LINE_PATTERN.matcher(semanticsText()).find();
    }

    /** A button by its exact accessible name. */
    public Locator button(String label) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(label).setExact(true)).first();
    }

    /**
     * Visible buttons below the image grid that are big enough to be action buttons, top to
     * bottom. Includes buttons without an accessible name, so label checks can catch them.
     */
    public List<Locator> actionButtons() {
        double gridBottom = gridBottom();
        List<Locator> buttons = new ArrayList<>();
        Locator all = page.getByRole(AriaRole.BUTTON);
        for (int i = 0; i < all.count(); i++) {
            Locator button = all.nth(i);
            BoundingBox box = button.isVisible() ? button.boundingBox() : null;
            if (box != null && box.y >= gridBottom && box.height >= MIN_ACTION_BUTTON_HEIGHT) {
                buttons.add(button);
            }
        }
        buttons.sort(Comparator.comparingDouble(b -> b.boundingBox().y));
        return buttons;
    }

    /** Accessible name of a button: its label, or its text; "" when it has none. */
    public static String nameOf(Locator button) {
        String label = button.getAttribute("aria-label");
        String name = label != null && !label.isBlank() ? label : button.innerText();
        return name == null ? "" : name.replaceAll("\\s+", " ").trim();
    }

    /**
     * Waits up to {@code timeoutMs} for the screen's carousel to move to another slide (its text
     * changes from {@code currentText}); false when it didn't.
     */
    public boolean waitForNextSlide(String currentText, double timeoutMs) {
        try {
            waitForSemanticsTextChange(currentText, timeoutMs);
            return true;
        } catch (com.microsoft.playwright.PlaywrightException stayed) {
            return false;
        }
    }

    /** Bottom edge (CSS px) of the image grid at the top of the screen. */
    public double gridBottom() {
        Locator heading = welcomeHeading();
        BoundingBox box = heading.count() > 0 && heading.isVisible() ? heading.boundingBox() : null;
        return box != null ? box.y * GRID_BOTTOM_SHARE_OF_HEADING_TOP : page.viewportSize().height * 0.3;
    }

    private Locator matching(Pattern pattern) {
        return page.getByText(pattern).or(page.getByLabel(pattern)).first();
    }
}
