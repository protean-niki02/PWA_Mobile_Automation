package tests.sprint3.bd2m_770;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import config.DeviceProfile;
import config.TestConfig;
import org.testng.Assert;
import org.testng.SkipException;
import pages.FlutterPage;
import support.ImageCompare;
import support.ScreenPixels;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

/**
 * Base for BD2M-770 - Policy Detail Screen -&gt; OVD (My Policies - Policy Details Screen, Mobile).
 * <p>
 * Every test starts as the sheet says: "Customer logged in &amp; has an active motor policy &amp; has opened
 * the Policy Details screen from a policy card in My Policies". How to get there is test data, because
 * the login needs an OTP and the app's start page differs per environment. Set in
 * {@code src/test/resources/config.properties} (or pass {@code -D...}):
 * <ul>
 *   <li>{@code policyDetails.startUrl} - the address that opens My Policies (or Policy Details) for a
 *       customer who has an active motor policy, e.g. one carrying a logged-in session;</li>
 *   <li>{@code policyDetails.cardText} - optional: text on the policy card to tap (default "HDFC ERGO").</li>
 * </ul>
 * While {@code policyDetails.startUrl} is empty every BD2M-770 test is SKIPPED, not failed.
 * <p>
 * The screen is read as text (Flutter semantics when the app is the Flutter PWA, else the page's own text),
 * so the tests check what the sheet's Expected Result names. Checks that need the approved Mobile Figma
 * compare with an image in {@code src/test/resources/approved-design/policy-details/}; without one they
 * are skipped.
 */
public abstract class PolicyDetailsTestBase extends BaseTest {

    protected static final String TAB_POLICY = "Policy Details";
    protected static final String TAB_VEHICLE = "Vehicle Details";
    protected static final String TAB_COVERAGE = "Coverage & Premium";

    private static final int STEP_MS = 1_200;
    private static final int OPEN_TIMEOUT_MS = 60_000;
    private static final Path APPROVED = Path.of("src", "test", "resources", "approved-design", "policy-details");
    private static final Pattern ERROR_TEXT = Pattern.compile(
        "(?i)something\\s+went\\s+wrong|oops|unable\\s+to|failed|try\\s+again|error\\s+occurred|application\\s+error");

    /** Hex colour of the active tab segment as the sheet gives it (TC 36). */
    protected static final int[] ACTIVE_TAB = {0x4C, 0x5B, 0xB5};

    private boolean flutter;
    private Screen screen;

    private final class Screen extends FlutterPage {
        Screen(Page p) {
            super(p);
        }
    }

    @Override
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.ANDROID;
    }

    // ---- reaching the screen ----

    /** Opens My Policies for the test customer and taps the policy card, so the Policy Details screen is shown. */
    protected void openPolicyDetails() {
        String start = TestConfig.optional("policyDetails.startUrl");
        String mobile = TestConfig.optional("policies.mobileNumber");
        String otp = TestConfig.optional("policies.otp");
        if (start.isEmpty() && (mobile.isEmpty() || otp.isEmpty())) {
            throw new SkipException("Test data missing: set policies.mobileNumber and policies.otp (a customer with an active motor policy and"
                + " a fixed UAT OTP) in src/test/resources/config.properties, or policyDetails.startUrl (an address that opens My Policies)"
                + " - or pass them with -D");
        }
        if (!start.isEmpty()) {
            page.navigate(start);
        } else {
            loginWithOtp(mobile, otp);
        }
        screen = new Screen(page);
        flutter = waitFor(() -> Boolean.TRUE.equals(page.evaluate(
            "() => !!document.querySelector('flutter-view, flt-glass-pane, flt-semantics-placeholder, flt-semantics')")), 15_000);
        if (flutter) {
            screen.enableSemantics();
        }
        screen.waitForImagesToSettle();
        screenshot("start-screen");
        if (!shows(TAB_VEHICLE)) {
            String card = TestConfig.optional("policyDetails.cardText");
            if (textOrLabel(card.isEmpty() ? "HDFC ERGO" : card).count() == 0 && shows("My Policies")) {
                tap("My Policies");   // from the home screen, open My Policies in the bottom navigation
            }
            String cardText = card.isEmpty() ? "HDFC ERGO" : card;
            Locator policyCard = textOrLabel(cardText);
            Assert.assertTrue(policyCard.count() > 0, "No policy card with \"" + cardText + "\" in My Policies. Screen text: " + screenText());
            press(policyCard.first());
            Assert.assertTrue(waitFor(() -> shows(TAB_VEHICLE), OPEN_TIMEOUT_MS),
                "The Policy Details screen did not open after tapping the policy card. Screen text: " + screenText());
        }
        log("Policy Details screen open at " + page.url());
        screenshot("policy-details-opened");
    }

    /**
     * Logs in as the test customer: mobile number, Get OTP, the fixed UAT OTP, then waits until the app
     * has left the login screen. The OTP is typed into the first field that is not the mobile number, so
     * it works for one box or one box per digit; a Verify / Login button is tapped if the app shows one.
     */
    private void loginWithOtp(String mobile, String otp) {
        pages.LoginPage login = new pages.LoginPage(page).open();
        log("Login screen open at " + page.url());
        Locator mobileField = login.mobileNumberField();
        mobileField.click();
        mobileField.fill(mobile);
        screenshot("login-mobile-entered");
        login.getOtpButton().click();
        page.waitForTimeout(2_500);

        Locator boxes = page.getByRole(AriaRole.TEXTBOX);
        Assert.assertTrue(waitFor(() -> boxes.count() > 0, 20_000), "No OTP field after Get OTP. Screen text: " + screenText());
        String last4 = mobile.substring(Math.max(0, mobile.length() - 4));
        Locator target = null;
        for (int i = 0; i < boxes.count(); i++) {
            String value = boxes.nth(i).inputValue();
            if (!value.contains(last4)) {
                target = boxes.nth(i);
                break;
            }
        }
        Assert.assertNotNull(target, "No OTP field apart from the mobile number. Screen text: " + screenText());
        target.click();
        page.keyboard().type(otp, new com.microsoft.playwright.Keyboard.TypeOptions().setDelay(80));
        screenshot("login-otp-entered");
        Locator submit = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("(?i)^(verify|login|log in|submit|continue|proceed|confirm)")));
        if (submit.count() > 0 && submit.first().isVisible() && submit.first().isEnabled()) {
            submit.first().click();
        }
        boolean in = waitFor(() -> !page.url().contains("/login"), 30_000);
        screenshot("after-login");
        Assert.assertTrue(in, "Login did not complete: still on the login screen after entering the OTP. Screen text: " + screenText());
        log("Logged in; now at " + page.url());
    }

    /** Switches to a tab of the 3-segment strip and waits for it. */
    protected void openTab(String tab) {
        tap(tab);
    }

    /** Makes sure a section is open: if its content label is not shown, taps the section header once. */
    protected void ensureExpanded(String section, String childLabel) {
        if (!shows(childLabel)) {
            tap(section);
        }
        Assert.assertTrue(shows(childLabel), "\"" + section + "\" did not open to show \"" + childLabel + "\". Screen text: " + screenText());
    }

    // ---- reading the screen ----

    /** All text of the screen (Flutter semantics labels, or the page text), whitespace collapsed. */
    protected String screenText() {
        return (String) page.evaluate("""
            () => {
                const host = document.querySelector('flt-semantics-host') || document.body;
                const labels = [...host.querySelectorAll('[aria-label]')].map(e => e.getAttribute('aria-label'));
                return (labels.join(' | ') + ' | ' + host.innerText).replace(/\\s+/g, ' ').trim();
            }
            """);
    }

    private static String norm(String s) {
        return s.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    protected boolean shows(String label) {
        return norm(screenText()).contains(norm(label));
    }

    /** Fails (listing every one) unless all the labels are on the screen. */
    protected void assertShown(String... labels) {
        String text = norm(screenText());
        List<String> missing = new ArrayList<>();
        for (String label : labels) {
            if (!text.contains(norm(label))) {
                missing.add(label);
            }
        }
        screenshot("checked-screen");
        log("Expected on the screen: " + List.of(labels) + (missing.isEmpty() ? " - all shown" : " - MISSING " + missing));
        Assert.assertTrue(missing.isEmpty(), "Not shown on the screen: " + missing + ". Screen text: " + screenText());
    }

    protected void assertNotShown(String label) {
        Assert.assertFalse(shows(label), "\"" + label + "\" is shown on the screen but should not be. Screen text: " + screenText());
    }

    /** The element carrying the text or label (several may match; the first is used). */
    protected Locator textOrLabel(String text) {
        return page.getByText(text).or(page.getByLabel(text)).first();
    }

    // ---- acting on the screen ----

    private void press(Locator target) {
        target.scrollIntoViewIfNeeded();
        if (flutter) {
            screen.pressOn(target);
        } else {
            target.click();
        }
        page.waitForTimeout(STEP_MS);
    }

    /** Taps the element showing the text; fails if it is not on the screen. */
    protected void tap(String label) {
        Locator target = textOrLabel(label);
        Assert.assertTrue(target.count() > 0, "\"" + label + "\" is not on the screen. Screen text: " + screenText());
        press(target);
    }

    /** The header's back chevron (a control named "back", or the first small control at the left of the header). */
    protected Locator backControl() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("(?i)back|navigate up")))
            .or(page.locator("[aria-label*='back' i]")).first();
    }

    /** The summary card's expand caret (a control named expand / collapse / more). */
    protected Locator summaryCaret() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("(?i)expand|collapse|caret|chevron|toggle")))
            .or(page.locator("[aria-label*='expand' i], [aria-label*='collapse' i]")).first();
    }

    /** The phone number's reveal (eye) icon. */
    protected Locator eyeIcon() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("(?i)eye|reveal|show|hide|visibility")))
            .or(page.locator("[aria-label*='eye' i], [aria-label*='reveal' i]")).first();
    }

    /** Taps a control located by {@code control} and fails if it is not there. */
    protected void pressControl(Locator control, String name) {
        Assert.assertTrue(control.count() > 0, "The " + name + " is not on the screen. Screen text: " + screenText());
        press(control);
    }

    /** First tap must expand (more text), second must collapse again (less text). */
    protected void expandThenCollapse(Runnable tapIt, String name) {
        int before = screenText().length();
        tapIt.run();
        int expanded = screenText().length();
        screenshot("after-first-tap");
        tapIt.run();
        int collapsed = screenText().length();
        screenshot("after-second-tap");
        log(String.format("%s: text %d -> %d (first tap) -> %d (second tap)", name, before, expanded, collapsed));
        Assert.assertTrue(expanded > before, name + " did not expand on the first tap (screen text " + before + " -> " + expanded + " chars)");
        Assert.assertTrue(collapsed < expanded, name + " did not collapse on the second tap (screen text " + expanded + " -> " + collapsed + " chars)");
    }

    /** Runs the action and returns the API requests the app sent meanwhile (any request to an api-uat address). */
    protected List<String> apiRequestsDuring(Runnable action) {
        List<String> sent = new ArrayList<>();
        java.util.function.Consumer<com.microsoft.playwright.Request> listener = r -> {
            if (r.url().contains("api-uat")) {
                sent.add(r.method() + " " + r.url().replaceAll("^https?://[^/]+", ""));
            }
        };
        page.onRequest(listener);
        try {
            action.run();
            page.waitForTimeout(1_500);
        } finally {
            page.offRequest(listener);
        }
        return sent;
    }

    // ---- checks ----

    /** The screen still shows the Policy Details tabs, no error text, and the page still answers. */
    protected void assertScreenIntact(String when) {
        Object alive = page.evaluate("() => 1 + 1");
        String text = screenText();
        screenshot("intact-" + when.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        Assert.assertEquals(alive, 2, "The page does not answer " + when);
        Assert.assertFalse(ERROR_TEXT.matcher(text).find(), "An error is shown " + when + ": " + text);
        Assert.assertTrue(norm(text).contains(norm(TAB_POLICY)) || norm(text).contains(norm(TAB_VEHICLE)),
            "The Policy Details screen is gone " + when + ". Screen text: " + text);
    }

    protected void assertNoHorizontalOverflow() {
        @SuppressWarnings("unchecked")
        List<Number> w = (List<Number>) page.evaluate("() => [document.documentElement.scrollWidth, window.innerWidth]");
        Assert.assertTrue(w.get(0).doubleValue() <= w.get(1).doubleValue() + 1,
            String.format("The page is %.0f px wide on a %.0f px screen (sideways overflow)", w.get(0).doubleValue(), w.get(1).doubleValue()));
    }

    /** Compares the whole screen with the approved Mobile Figma image of the test case; skips when none is provided. */
    protected void assertMatchesApproved(String tcId) {
        int w = page.viewportSize().width;
        int h = page.viewportSize().height;
        Path approved = APPROVED.resolve(tcId + "_" + w + "x" + h + ".png");
        byte[] actual = ScreenPixels.capture(page, 0, 0, w, h);
        saveEvidence("screen_" + w + "x" + h + ".png", actual);
        if (!Files.exists(approved)) {
            throw new SkipException("No approved Mobile Figma image for " + tcId + " at " + w + "x" + h + ": put it in " + approved);
        }
        try {
            ImageCompare.Result r = ImageCompare.compare(Files.readAllBytes(approved), actual, 0.03);
            log("Compared with " + approved + ": " + r.detail());
            Assert.assertTrue(r.matches(), "The screen does not match the approved Mobile Figma (" + r.detail() + ")");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + approved, e);
        }
    }

    // ---- pixels ----

    protected static BufferedImage decode(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read a screen capture", e);
        }
    }

    /** The most common colour in the picture of the element's box (its fill). */
    protected int[] fillOf(Locator element) {
        element.scrollIntoViewIfNeeded();
        BoundingBox b = element.boundingBox();
        Assert.assertNotNull(b, "The element has no box on the screen");
        BufferedImage img = decode(ScreenPixels.capture(page, b.x, b.y, b.width, b.height));
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                counts.merge(img.getRGB(x, y) & 0xFFFFFF, 1, Integer::sum);
            }
        }
        int best = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0);
        return new int[] {(best >> 16) & 0xff, (best >> 8) & 0xff, best & 0xff};
    }

    /** Whether a pixel of about this colour (each channel within {@code tolerance}) is in the picture. */
    protected static boolean hasColour(byte[] png, int[] rgb, int tolerance) {
        BufferedImage img = decode(png);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                if (Math.abs(((p >> 16) & 0xff) - rgb[0]) <= tolerance && Math.abs(((p >> 8) & 0xff) - rgb[1]) <= tolerance
                    && Math.abs((p & 0xff) - rgb[2]) <= tolerance) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean closeTo(int[] a, int[] b, int tolerance) {
        return Math.abs(a[0] - b[0]) <= tolerance && Math.abs(a[1] - b[1]) <= tolerance && Math.abs(a[2] - b[2]) <= tolerance;
    }

    protected static String hex(int[] c) {
        return String.format("#%02X%02X%02X", c[0], c[1], c[2]);
    }

    /** Whether a pixel of about this colour is within {@code pad} px of the element's box (e.g. an indicator under a label). */
    protected boolean hasColourNear(Locator element, int[] rgb, int tolerance, double pad) {
        BoundingBox b = element.boundingBox();
        Assert.assertNotNull(b, "The element has no box on the screen");
        double x = Math.max(0, b.x - pad);
        double y = Math.max(0, b.y - pad);
        double w = Math.min(page.viewportSize().width - x, b.width + 2 * pad);
        double h = Math.min(page.viewportSize().height - y, b.height + 2 * pad);
        return hasColour(ScreenPixels.capture(page, x, y, w, h), rgb, tolerance);
    }

    /** Scrolls the content and fails if the picture of the screen does not change. */
    protected void assertContentScrolls(String name) {
        byte[] before = screenPicture();
        page.mouse().move(page.viewportSize().width / 2.0, page.viewportSize().height / 2.0);
        page.mouse().wheel(0, 600);
        page.waitForTimeout(800);
        byte[] after = screenPicture();
        saveEvidence("scrolled-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase() + ".png", after);
        Assert.assertFalse(ImageCompare.compare(before, after, 0.005).matches(), name + " content did not scroll");
    }

    /** The bottom navigation ("My Policies") must stay in place while the content scrolls. */
    protected void assertBottomNavPinned() {
        Locator nav = textOrLabel("My Policies");
        Assert.assertTrue(nav.count() > 0, "No bottom navigation (\"My Policies\") on the screen");
        BoundingBox start = nav.boundingBox();
        page.mouse().move(page.viewportSize().width / 2.0, page.viewportSize().height / 2.0);
        for (int i = 0; i < 4; i++) {
            page.mouse().wheel(0, 500);
            page.waitForTimeout(500);
            BoundingBox now = nav.boundingBox();
            Assert.assertNotNull(now, "The bottom navigation disappeared while scrolling");
            Assert.assertTrue(Math.abs(now.y - start.y) <= 2, String.format("The bottom navigation moved from y %.0f to %.0f while scrolling", start.y, now.y));
        }
    }

    /** No text element may reach beyond the left or right edge of the screen. */
    protected void assertNothingClipped(String where) {
        @SuppressWarnings("unchecked")
        List<String> clipped = (List<String>) page.evaluate("""
            () => [...document.querySelectorAll('body *')].filter(e => e.children.length === 0 && (e.innerText || '').trim().length > 0)
                .filter(e => { const r = e.getBoundingClientRect(); return r.width > 0 && (r.left < -1 || r.right > window.innerWidth + 1); })
                .map(e => e.innerText.trim().slice(0, 40))
            """);
        screenshot("layout-" + where.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        Assert.assertTrue(clipped.isEmpty(), "Content is clipped at the screen edge on " + where + ": " + clipped);
        assertNoHorizontalOverflow();
    }

    protected byte[] screenPicture() {
        return ScreenPixels.capture(page, 0, 0, page.viewportSize().width, page.viewportSize().height);
    }

    protected boolean waitFor(BooleanSupplier condition, int timeoutMs) {
        try {
            page.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(timeoutMs));
            return true;
        } catch (TimeoutError notYet) {
            return false;
        }
    }
}
