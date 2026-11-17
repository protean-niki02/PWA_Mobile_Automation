package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_036 - Verify that the header displays the back arrow, "Bima Vahan Product" title and Close (X) icon.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The Confirm Plan header must show a back arrow, the title "Bima Vahan Product" and a Close (X). On the web
 * journey the back arrow is the "Back" control with its arrow; the title and the Close control are looked for
 * by their text / label. What the page's top area does show (site header, breadcrumb, controls) is logged.
 * Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC036_HeaderTest extends ConfirmPlanWebTestBase {

    /** The title the TC expects. */
    private static final String EXPECTED_TITLE = "Bima Vahan Product";
    private static final Pattern CLOSE = Pattern.compile("(?i)^\\s*(close|exit|cancel|dismiss|x|×|✕)\\s*$");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_036: Verify that the header displays the back arrow, \"Bima Vahan Product\" title and Close (X) icon.")
    public void verifyHeader() {
        openConfirmPlan();
        scrollArea().evaluate("e => { e.scrollTop = 0; }");
        Locator back = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true));
        boolean backShown = back.count() > 0 && back.first().isVisible() && back.first().locator("svg, img").count() > 0;
        Locator title = page.getByText(EXPECTED_TITLE, new Page.GetByTextOptions().setExact(true));
        boolean titleShown = title.count() > 0 && title.first().isVisible();
        Locator close = page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText(CLOSE))
            .or(page.locator("[aria-label*='close' i], [title*='close' i], [data-testid*='close' i]"));
        boolean closeShown = close.count() > 0 && close.first().isVisible();
        @SuppressWarnings("unchecked")
        List<String> top = (List<String>) page.locator("header, [role='button'], button").evaluateAll(
            "els => els.filter(e => e.getBoundingClientRect().top < 320).map(e => (e.getAttribute('aria-label') || e.innerText || e.tagName)"
                + ".trim().replace(/\\s+/g, ' ').slice(0, 40)).filter(t => t.length > 0)");
        log(String.format("Header: back arrow %s | title \"%s\" %s | Close (X) %s. Controls in the top area: %s", backShown ? "shown" : "NOT shown",
            EXPECTED_TITLE, titleShown ? "shown" : "NOT shown", closeShown ? "shown" : "NOT shown", top));
        screenshot("confirm-plan-header");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(backShown, "The header does not display the back arrow");
        softly.assertTrue(titleShown, "The header does not display the title \"" + EXPECTED_TITLE + "\"");
        softly.assertTrue(closeShown, "The header does not display the Close (X) icon");
        softly.assertAll();
    }
}
