package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_033 - Verify that the Close (X) button exits the flow.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Confirm Plan must have a Close (X) control; tapping it must take the user out of the 4 Wheeler journey
 * within 15s (confirming "Leave" if the app asks): not on a /journey page, and a screen is shown. Where the
 * user lands is logged. If the screen has no Close control the test fails and lists the controls it has.
 * Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC033_CloseExitsFlowTest extends ConfirmPlanWebTestBase {

    private static final Pattern CLOSE = Pattern.compile("(?i)^\\s*(close|exit|cancel|dismiss|x|×|✕)\\s*$");

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_033: Verify that the Close (X) button exits the flow.")
    public void verifyCloseExitsFlow() {
        openConfirmPlan();
        String route = page.url();
        Locator close = page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText(CLOSE))
            .or(page.locator("[aria-label*='close' i], [title*='close' i], [data-testid*='close' i]"));
        if (close.count() == 0) {
            @SuppressWarnings("unchecked")
            List<String> controls = (List<String>) page.locator("button, [role='button']").evaluateAll(
                "els => els.map(e => (e.getAttribute('aria-label') || e.innerText || '').trim().replace(/\\s+/g, ' ')).filter(t => t.length > 0)");
            screenshot("no-close-control");
            log("Controls on Confirm Plan: " + controls);
            Assert.fail("Confirm Plan has no Close (X) control. Its buttons are: " + controls);
        }
        close.first().click();
        String confirmed = confirmLeavingIfAsked();
        boolean exited = waitFor(() -> !page.url().contains("/journey"), 15_000);
        page.waitForTimeout(1_000);
        String landed = bodyText();
        log("Close (X): " + route + " -> " + page.url() + "; confirmation " + (confirmed == null ? "not asked" : "\"" + confirmed + "\"")
            + "; landed on " + (landed.length() > 200 ? landed.substring(0, 200) + "..." : landed));
        screenshot("after-close");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(exited, "Close (X) did not exit the 4 Wheeler flow within 15s; still on " + page.url());
        softly.assertFalse(landed.isEmpty(), "After Close (X) the app shows a blank screen");
        softly.assertAll();
    }

    /** If the app asks to confirm leaving (Leave / Yes / Exit / Confirm), confirms it and returns the button, else null. */
    private String confirmLeavingIfAsked() {
        page.waitForTimeout(1_500);
        for (String name : List.of("Leave", "Yes", "Exit", "Yes, Exit", "Yes, Leave")) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() > 0 && button.first().isVisible()) {
                screenshot("leave-confirmation");
                button.first().click();
                return name;
            }
        }
        return null;
    }
}
