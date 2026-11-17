package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_032 - Verify that the screen title reflects the active sort applied by the user.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The Sort panel offers "Sort by Premium": Low to High (default) and High to Low. Once the quotes are
 * loaded:
 * <ol>
 *   <li>the title is read with the default sort (Low to High);</li>
 *   <li>Sort &gt; "High to Low" is applied - the premiums must now run from highest to lowest (so the
 *       sort really is active), and the title must reflect it: it changes from the default title or
 *       names the order (high to low / highest first / descending);</li>
 *   <li>Sort &gt; "Low to High" is applied again - the title must reflect that too (back to the default
 *       or naming low to high).</li>
 * </ol>
 * The titles and premium orders are logged, with screenshots of each.
 */
public class SC01_TC032_QuotesTitleReflectsSortTest extends QuotesListingTestBase {

    private static final int RESORT_TIMEOUT_MS = 20_000;
    private static final String LOW_TO_HIGH = "Low to High";
    private static final String HIGH_TO_LOW = "High to Low";
    private static final Pattern TITLE = Pattern.compile("Here\\s*are\\s*your\\s*quotes", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAMES_HIGH_TO_LOW = Pattern.compile("(?i)high\\s*to\\s*low|highest|descending|high\\s*-\\s*low");
    private static final Pattern NAMES_LOW_TO_HIGH = Pattern.compile("(?i)low\\s*to\\s*high|lowest|ascending|low\\s*-\\s*high");

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_032: Verify that the screen title reflects the active sort applied by the user.")
    public void verifyTitleReflectsActiveSort() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        String defaultTitle = title(quotes);
        List<Long> defaultOrder = amounts(quotes.premiums());
        log("Default sort (" + LOW_TO_HIGH + "): title \"" + defaultTitle + "\", premiums " + defaultOrder);
        screenshot("title-default-sort");

        applySort(quotes, HIGH_TO_LOW);
        String highTitle = title(quotes);
        List<Long> highOrder = amounts(quotes.premiums());
        log("After " + HIGH_TO_LOW + ": title \"" + highTitle + "\", premiums " + highOrder);
        screenshot("title-high-to-low");

        applySort(quotes, LOW_TO_HIGH);
        String lowTitle = title(quotes);
        List<Long> lowOrder = amounts(quotes.premiums());
        log("After " + LOW_TO_HIGH + " again: title \"" + lowTitle + "\", premiums " + lowOrder);
        screenshot("title-low-to-high");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(highOrder.size() < 2 || descending(highOrder), "\"" + HIGH_TO_LOW
            + "\" was not applied: the premiums are not highest first " + highOrder);
        softly.assertTrue(lowOrder.size() < 2 || ascending(lowOrder), "\"" + LOW_TO_HIGH
            + "\" was not applied again: the premiums are not lowest first " + lowOrder);
        softly.assertTrue(!highTitle.equalsIgnoreCase(defaultTitle) || NAMES_HIGH_TO_LOW.matcher(highTitle).find(),
            "The title does not reflect the active sort: with \"" + HIGH_TO_LOW + "\" applied it still reads \"" + highTitle
                + "\", the same as with the default \"" + LOW_TO_HIGH + "\"");
        softly.assertTrue(lowTitle.equalsIgnoreCase(defaultTitle) || NAMES_LOW_TO_HIGH.matcher(lowTitle).find(),
            "With \"" + LOW_TO_HIGH + "\" applied again the title reads \"" + lowTitle + "\" - neither the default \""
                + defaultTitle + "\" nor naming " + LOW_TO_HIGH);
        softly.assertAll();
    }

    /** The screen title ("N Here are your quotes sorted by price ..."). */
    private String title(QuotesListingPage quotes) {
        Locator title = quotes.text(TITLE);
        Assert.assertTrue(title.count() > 0, "The screen title (\"Here are your quotes ...\") is not shown. Screen text: "
            + quotes.semanticsText());
        return title.innerText().replaceAll("\\s+", " ").trim();
    }

    /** Opens Sort, picks the option, closes the panel if it stays open, and waits for the list to settle. */
    private void applySort(QuotesListingPage quotes, String option) {
        List<String> before = quotes.premiums();
        tapAndCollectNewTexts(quotes, quotes.button(QuotesListingPage.SORT), QuotesListingPage.SORT);
        Locator choice = quotes.text(option);
        Assert.assertTrue(choice.count() > 0, "The Sort panel has no \"" + option + "\". Screen text: " + quotes.semanticsText());
        quotes.pressOn(choice);
        page.waitForTimeout(1_500);
        if (quotes.text("Sort by Premium").count() > 0) {
            Locator dismiss = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^(Dismiss|Close)$")));
            if (dismiss.count() > 0) {
                quotes.pressOn(dismiss.first());
            } else {
                page.keyboard().press("Escape");
            }
            page.waitForTimeout(1_000);
        }
        // Re-sorting may only reorder the cards; equal premiums don't change at all, so wait briefly
        quotes.waitForRefetch(before, RESORT_TIMEOUT_MS);
        quotes.scrollToTop();
    }

    private static boolean descending(List<Long> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > values.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private static boolean ascending(List<Long> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < values.get(i - 1)) {
                return false;
            }
        }
        return true;
    }
}
