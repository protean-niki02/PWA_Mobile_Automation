package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import pages.FlutterPage;
import pages.fourwheeler.QuotesListingPage;
import support.ScreenPixels;
import tests.sprint3.bd2m_761.QuotesListingTestBase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base for BD2M-767 - 4 Wheeler - Confirm Plan / Add-ons Configuration.
 * <p>
 * Opens the Confirm Plan screen the way a user does: the quotes listing for the test car (BD2M-761),
 * then a quote card's premium button. The insurer, premium and IDV chosen there are kept for the tests.
 * <p>
 * The screen as the app builds it (UAT 2.0.5+3): header "Confirm Plan" with back arrow and Close (X);
 * "Confirm your Bima Vahan Plan"; the plan summary card (insurer, metrics, IDV, Final Premium, Product
 * Details, View Breakup); "What's included in your base plan?" (expands); Optional Covers with
 * "Premiums given are indicative premium" and four groups - Personal Accident Cover, Add-ons covers,
 * Additional Own Damage, Additional Third Party. Each cover shows its premium ("₹ 413", or "-"), title,
 * a select box at the right of the title and a description cut short with "...more". Owner Driver is
 * tagged "Mandatory by Law" and has Tenure options (1 Year / 3 Year). At the bottom "Not yet sure about
 * the best plan for your needs?" with "Go Back to Quotes", and the "Confirm Plan" button.
 * <p>
 * The select boxes have no checked state for accessibility, so whether a cover is selected is read
 * from the box's pixels: a filled (dark) box is selected.
 */
public abstract class ConfirmPlanTestBase extends QuotesListingTestBase {

    /** Route part of the Confirm Plan screen: #/journey/en/motor/fourWheeler/QuoteAddonPage/&lt;leadId&gt;. */
    protected static final String ROUTE_PART = "/QuoteAddonPage/";
    protected static final String HEADER_TITLE = "Confirm Plan";
    protected static final String HEADING = "Confirm your Bima Vahan Plan";
    protected static final String HEADING_HIGHLIGHT = "Bima Vahan Plan";
    protected static final String CONFIRM_PLAN = "Confirm Plan";
    protected static final String OPTIONAL_COVERS = "Optional Covers";
    protected static final String BASE_PLAN = "What's included in your base plan?";
    protected static final String INDICATIVE = "Premiums given are indicative premium";
    protected static final String FINAL_PREMIUM = "Final Premium";
    protected static final String MANDATORY = "Mandatory by Law";
    protected static final String OWNER_DRIVER = "Owner Driver";
    protected static final String GO_BACK_TO_QUOTES = "Go Back to Quotes";

    /** A rupee amount as the app writes it: "₹53,313", "₹ 413". */
    protected static final Pattern RUPEES = Pattern.compile("₹\\s?\\d[\\d,]*");
    /** A cover's premium: an amount, or "-" when the insurer gives none. */
    private static final Pattern COVER_PRICE = Pattern.compile("^\\s*(₹\\s?\\d[\\d,]*|-)\\s*$");
    /** The four cover groups, as the app or the TCs word them. */
    protected static final Pattern GROUP_HEADING = Pattern.compile(
        "(?i)^\\s*(personal\\s+accident\\s+covers?|add-?ons?\\s+covers?|additional\\s+own\\s+damage(\\s+covers?)?|additional\\s+third\\s+party(\\s+covers?)?)\\s*$");
    /** A description cut short: ends with "...more". */
    protected static final Pattern CUT_SHORT = Pattern.compile("(?i)(\\.\\.\\.|…)\\s*more\\s*$");
    /** A filled select box: at least this share of its pixels is dark. */
    private static final double SELECTED_DARK_SHARE = 0.3;
    private static final int OPEN_TIMEOUT_MS = 30_000;

    /** The quote card whose premium button opened Confirm Plan. */
    protected String chosenInsurer;
    protected String chosenPremium;
    protected long chosenIdv;
    /** The add-on chips the listing showed (e.g. "PA Owner Driver") - selected before Confirm Plan opened. */
    protected List<String> chosenChips = new ArrayList<>();

    /** One optional cover as the screen lists it. */
    public record Cover(String group, String price, String title, String description) {
        public boolean hasPrice() {
            return RUPEES.matcher(price).find();
        }
    }

    /** Opens Confirm Plan from the first quote card; fails if it does not open. */
    protected QuotesListingPage openConfirmPlan() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        List<String> texts = quotes.semanticsTexts();
        Locator premium = quotes.premiumButton();
        Assert.assertTrue(premium.count() > 0, "No premium button on the quote cards. Screen text: " + quotes.semanticsText());
        quotes.bringIntoView(premium);
        chosenInsurer = insurerAbove(quotes, premium.boundingBox().y);
        String label = premium.getAttribute("aria-label");
        chosenPremium = label == null || label.isBlank() ? premium.innerText() : label;
        chosenIdv = cardIdv(texts, chosenInsurer);
        int firstCard = chosenInsurer == null ? texts.size() : Math.max(0, texts.indexOf(chosenInsurer));
        for (String text : texts.subList(0, Math.min(firstCard, texts.size()))) {
            if (QuotesListingPage.ADD_ON.matcher(text).find()) {
                chosenChips.add(text.trim());
            }
        }
        log("Choosing " + chosenInsurer + " at " + chosenPremium + ", IDV " + chosenIdv + ", add-on chips " + chosenChips);
        quotes.pressOn(premium);
        page.waitForCondition(() -> quotes.route().contains(ROUTE_PART) || quotes.shows(HEADING),
            new Page.WaitForConditionOptions().setTimeout(OPEN_TIMEOUT_MS));
        page.waitForTimeout(1_500);
        quotes.waitForImagesToSettle();
        quotes.scrollToTop();
        // On iOS the accessibility tree can lag a moment behind the drawn screen
        waitFor(() -> finalPremium(quotes) > 0, 10_000);
        log("Confirm Plan route: " + quotes.route());
        screenshot("confirm-plan-opened");
        return quotes;
    }

    /** The IDV on the listing card of the insurer: the first amount after "IDV" following its name. */
    private static long cardIdv(List<String> texts, String insurer) {
        int from = insurer == null ? -1 : texts.indexOf(insurer);
        for (int i = Math.max(0, from); i < texts.size() - 1; i++) {
            if (texts.get(i).trim().equalsIgnoreCase("IDV") && RUPEES.matcher(texts.get(i + 1)).find()) {
                return digits(texts.get(i + 1));
            }
        }
        return 0;
    }

    // ---- plan summary card ----

    /** The Final Premium on the plan summary card (rupees), 0 when not shown. */
    protected long finalPremium(QuotesListingPage screen) {
        List<String> texts = screen.semanticsTexts();
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).trim().equalsIgnoreCase(FINAL_PREMIUM)) {
                for (int j = i + 1; j < Math.min(texts.size(), i + 3); j++) {
                    if (RUPEES.matcher(texts.get(j)).find()) {
                        return digits(texts.get(j));
                    }
                }
            }
        }
        return 0;
    }

    /** The IDV on the plan summary card (rupees), 0 when not shown. */
    protected long summaryIdv(QuotesListingPage screen) {
        List<String> texts = screen.semanticsTexts();
        int from = texts.indexOf(HEADING);
        for (int i = Math.max(0, from); i < texts.size() - 1; i++) {
            if (texts.get(i).trim().equalsIgnoreCase("IDV") && RUPEES.matcher(texts.get(i + 1)).find()) {
                return digits(texts.get(i + 1));
            }
        }
        return 0;
    }

    /** Waits up to {@code timeoutMs} for the Final Premium to differ from {@code before}; returns the premium then. */
    protected long waitForFinalPremiumChange(QuotesListingPage screen, long before, int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        long now = finalPremium(screen);
        while (System.currentTimeMillis() < end && (now == before || now == 0)) {
            page.waitForTimeout(500);
            now = finalPremium(screen);
        }
        page.waitForTimeout(1_000);
        return finalPremium(screen);
    }

    // ---- optional covers ----

    /**
     * The optional covers in screen order: each premium ("₹ 413" or "-") is followed by the cover's
     * title, then its description; the group is the last group heading above it.
     */
    protected List<Cover> covers(QuotesListingPage screen) {
        List<String> texts = screen.semanticsTexts();
        int start = texts.indexOf(OPTIONAL_COVERS);
        List<Cover> covers = new ArrayList<>();
        String group = "";
        for (int i = Math.max(0, start); i < texts.size(); i++) {
            String text = texts.get(i).trim();
            if (GROUP_HEADING.matcher(text).matches()) {
                group = text;
            } else if (COVER_PRICE.matcher(text).matches() && i + 1 < texts.size() && !group.isEmpty()) {
                String title = texts.get(i + 1).trim();
                String description = "";
                for (int j = i + 2; j < Math.min(texts.size(), i + 5); j++) {
                    if (texts.get(j).trim().length() > 30) {
                        description = texts.get(j).trim();
                        break;
                    }
                }
                covers.add(new Cover(group, text, title, description));
                i++;
            } else if (text.equals(GO_BACK_TO_QUOTES)) {
                break;
            }
        }
        return covers;
    }

    /** The cover's title on screen (exact text). */
    protected Locator titleOf(QuotesListingPage screen, Cover cover) {
        return screen.text(Pattern.compile("^\\s*" + FlutterPage.regexQuote(cover.title()) + "\\s*$"));
    }

    /**
     * The cover's select box: a small button at the right end of its title row, else the spot where the
     * app draws it (49 px from the right edge, on the title row). The title is brought into view first.
     */
    protected BoundingBox controlOf(QuotesListingPage screen, Cover cover) {
        Locator title = titleOf(screen, cover);
        screen.bringIntoView(title);
        BoundingBox row = title.boundingBox();
        double width = page.viewportSize().width;
        double rowY = row.y + row.height / 2;
        Locator buttons = page.getByRole(AriaRole.BUTTON);
        for (int i = 0; i < buttons.count(); i++) {
            BoundingBox box = buttons.nth(i).boundingBox();
            if (box != null && box.width <= 70 && box.height <= 70 && box.x > width * 0.65
                && Math.abs(box.y + box.height / 2 - rowY) < 40) {
                return box;
            }
        }
        BoundingBox spot = new BoundingBox();
        spot.x = width - 49 - 13;
        spot.y = rowY - 13;
        spot.width = 26;
        spot.height = 26;
        return spot;
    }

    /** Whether the select box is ticked: its square is filled (dark) rather than outlined. */
    protected boolean isSelected(BoundingBox control) {
        BufferedImage box = decode(ScreenPixels.capture(page, control.x, control.y, control.width, control.height));
        int dark = 0;
        for (int y = 0; y < box.getHeight(); y++) {
            for (int x = 0; x < box.getWidth(); x++) {
                dark += brightness(box.getRGB(x, y)) < 110 ? 1 : 0;
            }
        }
        return dark >= box.getWidth() * box.getHeight() * SELECTED_DARK_SHARE;
    }

    protected boolean isSelected(QuotesListingPage screen, Cover cover) {
        return isSelected(controlOf(screen, cover));
    }

    /** Taps the cover's select box. */
    protected void tapControl(QuotesListingPage screen, Cover cover) {
        BoundingBox control = controlOf(screen, cover);
        screen.tapAt(control.x + control.width / 2, control.y + control.height / 2);
        page.waitForTimeout(1_500);
    }

    /** The first cover of a group whose title matches, or null. */
    protected static Cover find(List<Cover> covers, String title) {
        return covers.stream().filter(c -> c.title().equalsIgnoreCase(title)).findFirst().orElse(null);
    }

    /** The first unselected cover with a premium in the given group (e.g. "Add-ons covers"), or null. */
    protected Cover firstUnselected(QuotesListingPage screen, List<Cover> covers, String groupWord) {
        for (Cover cover : covers) {
            if (cover.group().toLowerCase().contains(groupWord.toLowerCase()) && cover.hasPrice() && !isSelected(screen, cover)) {
                return cover;
            }
        }
        return null;
    }

    /**
     * Checks one cover group: its heading is shown (the app's wording may differ slightly from the TC's,
     * which is logged) and it holds covers - the expected ones among them. Returns the problems found.
     */
    protected List<String> checkGroup(QuotesListingPage screen, String tcHeading, Pattern heading, List<String> expectedCovers) {
        List<String> texts = screen.semanticsTexts();
        String shownHeading = texts.stream().filter(t -> heading.matcher(t.trim()).matches()).findFirst().orElse(null);
        List<Cover> inGroup = covers(screen).stream().filter(c -> heading.matcher(c.group()).matches()).toList();
        List<String> titles = inGroup.stream().map(Cover::title).toList();
        log(String.format("Group \"%s\": heading shown as %s; covers under it: %s", tcHeading,
            shownHeading == null ? "NOT shown" : "\"" + shownHeading + "\"", titles));
        List<String> problems = new ArrayList<>();
        if (shownHeading == null) {
            problems.add("heading \"" + tcHeading + "\" is not shown");
        } else {
            Locator head = screen.text(Pattern.compile("^\\s*" + FlutterPage.regexQuote(shownHeading) + "\\s*$"));
            screen.bringIntoView(head);
            screenshot("group-" + tcHeading.replaceAll("[^A-Za-z]+", "-").toLowerCase());
            if (!shownHeading.equalsIgnoreCase(tcHeading)) {
                log("Heading wording differs from the TC: app \"" + shownHeading + "\", TC \"" + tcHeading + "\"");
            }
        }
        if (inGroup.isEmpty()) {
            problems.add("no covers are listed under \"" + tcHeading + "\"");
        }
        for (String expected : expectedCovers) {
            if (titles.stream().noneMatch(t -> t.equalsIgnoreCase(expected))) {
                problems.add("\"" + expected + "\" is not under \"" + tcHeading + "\"");
            }
        }
        return problems;
    }

    // ---- base plan ----

    /** Own Damage covered events (IRDAI standard wording), each as the words that name it. */
    protected static final List<Pattern> OD_EVENTS = List.of(
        Pattern.compile("(?i)fire|explosion|self.?ignition|lightning"),
        Pattern.compile("(?i)burglary|housebreaking|theft"),
        Pattern.compile("(?i)riot|strike"),
        Pattern.compile("(?i)earthquake"),
        Pattern.compile("(?i)flood|typhoon|hurricane|storm|tempest|inundation|cyclone|hailstorm|frost"),
        Pattern.compile("(?i)accident"),
        Pattern.compile("(?i)malicious"),
        Pattern.compile("(?i)terroris"),
        Pattern.compile("(?i)transit|rail|inland waterway|elevator"),
        Pattern.compile("(?i)landslide|rockslide"));
    protected static final List<String> OD_EVENT_NAMES = List.of("Fire / explosion / self-ignition / lightning",
        "Burglary / housebreaking / theft", "Riot and strike", "Earthquake", "Flood / storm / cyclone / hailstorm",
        "Accidental external means", "Malicious act", "Terrorist activity", "Transit by road / rail / waterway / lift / air",
        "Landslide / rockslide");
    protected static final Pattern EVENTS_COVERED = Pattern.compile("(?i)(\\d+)\\s+events?\\s+covered");

    /** Expands "What's included in your base plan?" (unless already open) and returns the screen's texts then. */
    protected List<String> expandBasePlan(QuotesListingPage screen) {
        if (!basePlanOpen(screen.semanticsTexts())) {
            tapBasePlan(screen, "base plan");
        }
        return screen.semanticsTexts();
    }

    /**
     * Whether the base plan's own content is on screen: its "Own Damage Cover" heading or "N events
     * covered" line (not just the words "own damage", which the cover group "Additional Own Damage" has too).
     */
    protected static boolean basePlanOpen(List<String> texts) {
        return texts.stream().anyMatch(t -> t.trim().equalsIgnoreCase("Own Damage Cover") || EVENTS_COVERED.matcher(t).find());
    }

    /**
     * Taps the "What's included in your base plan?" heading and returns the texts that appeared. The app
     * fetches the section's content when it opens (three loading dots meanwhile), so after an expanding
     * tap it waits up to 20s for the content (Own Damage / Third Party) before collecting.
     */
    protected List<String> tapBasePlan(QuotesListingPage screen, String name) {
        Locator section = screen.text(BASE_PLAN);
        Assert.assertTrue(section.count() > 0, "\"" + BASE_PLAN + "\" is not on the Confirm Plan screen. Screen text: "
            + screen.semanticsText());
        screen.bringIntoView(section);
        List<String> before = screen.semanticsTexts();
        boolean wasOpen = basePlanOpen(before);
        screen.pressOn(section.first());
        page.waitForTimeout(1_500);
        if (!wasOpen) {
            boolean loaded = waitFor(() -> basePlanOpen(screen.semanticsTexts()), 20_000);
            log("Base plan content " + (loaded ? "loaded" : "NOT loaded within 20s"));
            page.waitForTimeout(800);
        }
        List<String> added = new ArrayList<>(screen.semanticsTexts());
        added.removeAll(before);
        log("Tapped \"" + name + "\": " + added.size() + " new text(s) " + added + "; route " + screen.route());
        screenshot("after-tapping-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        return added;
    }

    // ---- header and buttons ----

    /** The header title (the topmost "Confirm Plan"-like text in the header bar), or "". */
    protected String headerTitle(QuotesListingPage screen) {
        Locator title = headerTitleLocator();
        return title == null ? "" : title.innerText().trim();
    }

    /** The header's title text: the text closest to the top of the screen, inside the header bar. */
    protected Locator headerTitleLocator() {
        Locator texts = page.locator("flt-semantics span, flt-semantics[aria-label]");
        Locator best = null;
        double bestY = Double.MAX_VALUE;
        for (int i = 0; i < Math.min(texts.count(), 40); i++) {
            Locator text = texts.nth(i);
            BoundingBox box = text.boundingBox();
            String words = text.innerText().trim().isEmpty() ? String.valueOf(text.getAttribute("aria-label")) : text.innerText().trim();
            if (box != null && box.y < 110 && box.y > 30 && words.length() > 2 && !"null".equals(words) && box.y < bestY) {
                best = text;
                bestY = box.y;
            }
        }
        return best;
    }

    /** The header row's vertical centre (CSS px). */
    protected double headerRowY() {
        Locator title = headerTitleLocator();
        BoundingBox box = title == null ? null : title.boundingBox();
        return box == null ? 71 : box.y + box.height / 2;
    }

    /** The bottom "Confirm Plan" button (the lowest one; the header title reads the same). */
    protected Locator confirmPlanButton() {
        Locator buttons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(CONFIRM_PLAN).setExact(true));
        Locator best = null;
        double bestY = -1;
        for (int i = 0; i < buttons.count(); i++) {
            BoundingBox box = buttons.nth(i).boundingBox();
            if (box != null && box.y > bestY) {
                best = buttons.nth(i);
                bestY = box.y;
            }
        }
        return best != null ? best : page.getByText(CONFIRM_PLAN).last();
    }

    /** Taps the header back arrow (labelled "Back", else its spot at the left of the header). */
    protected void tapHeaderBack(QuotesListingPage screen) {
        Locator back = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back"));
        if (back.count() > 0 && back.first().isVisible()) {
            screen.pressOn(back.first());
        } else {
            screen.tapAt(34, headerRowY());
        }
    }

    /** Taps the header Close (X) (labelled "Close", else its spot at the right of the header). */
    protected void tapHeaderClose(QuotesListingPage screen) {
        Locator close = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close"));
        if (close.count() > 0 && close.first().isVisible()) {
            screen.pressOn(close.first());
        } else {
            screen.tapAt(page.viewportSize().width - 34, headerRowY());
        }
    }

    /** If the app asks to confirm leaving (Leave / Yes / Exit / Confirm), confirms it and returns the button, else null. */
    protected String confirmLeavingIfAsked() {
        page.waitForTimeout(1_500);
        for (String name : List.of("Leave", "Yes", "Exit", "Confirm", "Yes, Exit", "Yes, Leave")) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() > 0 && button.first().isVisible()) {
                screenshot("leave-confirmation");
                button.first().click();
                return name;
            }
        }
        return null;
    }

    /** Waits up to {@code timeoutMs} for the condition; false when it never held. */
    protected boolean waitFor(java.util.function.BooleanSupplier condition, int timeoutMs) {
        try {
            page.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(timeoutMs));
            return true;
        } catch (TimeoutError notYet) {
            return false;
        }
    }

    /** Taps an element and returns the texts that appeared because of it. */
    protected List<String> tapAndCollect(QuotesListingPage screen, Locator target, String name) {
        Assert.assertTrue(target.count() > 0, "\"" + name + "\" is not on the Confirm Plan screen. Screen text: " + screen.semanticsText());
        screen.bringIntoView(target);
        List<String> before = screen.semanticsTexts();
        screen.pressOn(target.first());
        page.waitForTimeout(2_000);
        List<String> added = new ArrayList<>(screen.semanticsTexts());
        added.removeAll(before);
        log("Tapped \"" + name + "\": " + added.size() + " new text(s) " + added + "; route " + screen.route());
        screenshot("after-tapping-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        return added;
    }

    // ---- pixels ----

    /** A white button (10%+ of the spot near-white) with a dark mark inside it - the back arrow's button. */
    protected static boolean iconOnWhiteButton(BufferedImage spot) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1, white = 0;
        for (int y = 0; y < spot.getHeight(); y++) {
            for (int x = 0; x < spot.getWidth(); x++) {
                if (brightness(spot.getRGB(x, y)) > 230) {
                    white++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (white < spot.getWidth() * spot.getHeight() / 10) {
            return false;
        }
        int dark = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                dark += brightness(spot.getRGB(x, y)) < 120 ? 1 : 0;
            }
        }
        return dark >= (maxX - minX + 1) * (maxY - minY + 1) / 100;
    }

    /** A white mark on the dark header (0.5% - 40% of the spot near-white) - the X's strokes. */
    protected static boolean whiteMark(BufferedImage spot) {
        int white = 0;
        for (int y = 0; y < spot.getHeight(); y++) {
            for (int x = 0; x < spot.getWidth(); x++) {
                white += brightness(spot.getRGB(x, y)) > 220 ? 1 : 0;
            }
        }
        double share = white / (double) (spot.getWidth() * spot.getHeight());
        return share >= 0.005 && share <= 0.4;
    }

    /**
     * The average ink colour (pixels clearly darker or more coloured than the background) at the start
     * of the text's first line ({@code start}) or the end of its last line.
     */
    protected static int[] inkColour(BufferedImage img, boolean start) {
        int top = -1, bottom = -1, left = Integer.MAX_VALUE, right = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (isInk(img.getRGB(x, y))) {
                    top = top < 0 ? y : top;
                    bottom = y;
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
        }
        if (right < 0) {
            return null;
        }
        int lineHeight = Math.max(1, Math.min(bottom - top + 1, (int) (img.getHeight() * 0.6)));
        int y0 = start ? top : bottom - lineHeight + 1;
        int span = (right - left + 1) / 5;
        int x0 = start ? left + span / 4 : right - span;
        long r = 0, g = 0, b = 0, n = 0;
        for (int y = Math.max(0, y0); y < Math.min(img.getHeight(), y0 + lineHeight); y++) {
            for (int x = x0; x < x0 + span; x++) {
                int rgb = img.getRGB(x, y);
                if (isInk(rgb)) {
                    r += (rgb >> 16) & 0xff;
                    g += (rgb >> 8) & 0xff;
                    b += rgb & 0xff;
                    n++;
                }
            }
        }
        return n == 0 ? null : new int[] {(int) (r / n), (int) (g / n), (int) (b / n)};
    }

    private static boolean isInk(int rgb) {
        int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
        return (r + g + b) / 3 < 170 || Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 90;
    }

    protected static int saturation(int[] c) {
        return Math.max(c[0], Math.max(c[1], c[2])) - Math.min(c[0], Math.min(c[1], c[2]));
    }

    protected static String hex(int[] c) {
        return c == null ? "none" : String.format("#%02X%02X%02X", c[0], c[1], c[2]);
    }

    protected static int brightness(int rgb) {
        return (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
    }

    /** Captures part of the screen (CSS px), saves it as evidence and returns it decoded. */
    protected BufferedImage captureAndSave(String name, double x, double y, double w, double h) {
        byte[] png = ScreenPixels.capture(page, x, Math.max(0, y), w, h);
        saveEvidence(name, png);
        return decode(png);
    }

    protected static BufferedImage decode(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read a screen capture", e);
        }
    }

    /** The rupee amount in a text, 0 when none. */
    protected static long rupees(String text) {
        Matcher m = RUPEES.matcher(text == null ? "" : text);
        return m.find() ? digits(m.group()) : 0;
    }
}
