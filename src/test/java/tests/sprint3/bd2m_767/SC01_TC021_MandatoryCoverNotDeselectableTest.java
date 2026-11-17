package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_021 - Verify that a mandatory cover (e.g. "Mandatory by Law") cannot be deselected.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Owner Driver is tagged "Mandatory by Law" and is selected on load. Its select box is tapped: it must
 * stay selected and the Final Premium must not change. If the app answers with a dialog (for example a
 * removal reason, as on the quotes listing), the dialog is recorded and dismissed without confirming,
 * and the cover must still be selected.
 */
public class SC01_TC021_MandatoryCoverNotDeselectableTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_021: Verify that a mandatory cover (e.g. \"Mandatory by Law\") cannot be deselected.")
    public void verifyMandatoryCoverStaysSelected() {
        QuotesListingPage screen = openConfirmPlan();
        Cover owner = find(covers(screen), OWNER_DRIVER);
        Assert.assertNotNull(owner, "No \"" + OWNER_DRIVER + "\" cover on Confirm Plan. Screen text: " + screen.semanticsText());
        Assert.assertTrue(isSelected(screen, owner), OWNER_DRIVER + " (mandatory) is not selected on load, so deselecting can't be checked");
        long premiumBefore = finalPremium(screen);
        List<String> before = screen.semanticsTexts();
        screenshot("mandatory-before-tap");

        tapControl(screen, owner);
        List<String> dialog = new ArrayList<>(screen.semanticsTexts());
        dialog.removeAll(before);
        if (!dialog.isEmpty()) {
            log("Tapping " + OWNER_DRIVER + "'s box opened: " + dialog);
            screenshot("mandatory-dialog");
            dismissDialog();
        }
        boolean stillSelected = isSelected(screen, owner);
        screen.scrollToTop();
        long premiumAfter = finalPremium(screen);
        log(String.format("%s after the tap: %s; Final Premium %s -> %s", OWNER_DRIVER, stillSelected ? "still selected" : "DESELECTED",
            indianFormat(premiumBefore), indianFormat(premiumAfter)));
        screenshot("mandatory-after-tap");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(stillSelected, "The mandatory cover " + OWNER_DRIVER + " was deselected by tapping its box");
        softly.assertEquals(premiumAfter, premiumBefore, "The Final Premium changed after tapping the mandatory cover's box");
        softly.assertAll();
    }

    /** Closes a dialog without confirming: Cancel / No / Close / Dismiss, else Escape. */
    private void dismissDialog() {
        for (String name : List.of("Cancel", "No", "Close", "Dismiss", "Keep")) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() > 0 && button.first().isVisible()) {
                button.first().click();
                page.waitForTimeout(1_000);
                return;
            }
        }
        page.keyboard().press("Escape");
        page.waitForTimeout(1_000);
    }
}
