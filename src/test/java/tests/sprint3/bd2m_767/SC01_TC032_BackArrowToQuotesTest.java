package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * SC_01_TC_032 - Verify that the back arrow returns the user to the quotes listing.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Confirm Plan is opened from the quotes listing; tapping the back arrow ("Back" at the top right of the
 * page content, beside the breadcrumb) must return to the quotes listing (.../quotes/&lt;leadId&gt;)
 * within 20s, with quotes shown again. Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC032_BackArrowToQuotesTest extends ConfirmPlanWebTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_032: Verify that the back arrow returns the user to the quotes listing.")
    public void verifyBackArrowReturnsToQuotes() {
        openConfirmPlan();
        String route = page.url();
        Locator back = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true));
        Assert.assertTrue(back.count() > 0, "No back arrow (\"Back\" button) on Confirm Plan. Screen text: " + bodyText());
        back.first().scrollIntoViewIfNeeded();
        back.first().click();
        boolean returned = waitFor(() -> page.url().contains("/quotes"), 20_000);
        boolean quotesShown = returned && waitFor(() -> page.getByText("Know Your Insurer").count() > 0, QUOTES_WAIT_MS);
        log("Back arrow: " + route + " -> " + page.url() + "; quotes shown: " + quotesShown
            + (quotesShown ? ", " + page.getByText("Know Your Insurer").count() + " quote card(s)" : ""));
        screenshot("after-back-arrow");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(returned, "The back arrow did not return to the quotes listing; now on " + page.url());
        softly.assertTrue(quotesShown, "Back on the quotes listing no quotes are shown. Screen text: " + bodyText());
        softly.assertAll();
    }
}
