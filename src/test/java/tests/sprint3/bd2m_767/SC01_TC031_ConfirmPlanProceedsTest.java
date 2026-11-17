package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.regex.Matcher;

/**
 * SC_01_TC_031 - Verify that tapping "Confirm Plan" proceeds to the next step passing the selected covers and final premium.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 *
 * The added covers and the Final Premium are noted, then "Confirm Plan" is tapped. Within 30s the app
 * must leave Confirm Plan for the next step without an error. When the next step shows a premium, it
 * must be the same Final Premium, and an added cover it names must be one of those added. What the
 * next step shows is logged; if it shows no premium (e.g. a login step), that is logged instead.
 * Runs on the web journey, see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC031_ConfirmPlanProceedsTest extends ConfirmPlanWebTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "sanity"},
        description = "SC_01_TC_031: Verify that tapping \"Confirm Plan\" proceeds to the next step passing the selected covers and final premium.")
    public void verifyConfirmPlanProceeds() {
        openConfirmPlan();
        long premium = finalPremium();
        List<String> selected = addedCovers();
        String before = page.url();
        log("Before Confirm Plan: Final Premium " + indianFormat(premium) + ", added covers " + selected + ", at " + before);
        confirmPlanButton().scrollIntoViewIfNeeded();
        confirmPlanButton().click();
        boolean moved = waitFor(() -> !page.url().equals(before), 30_000);
        page.waitForTimeout(2_000);
        String next = bodyText();
        List<Long> amounts = new java.util.ArrayList<>();
        Matcher m = RUPEES.matcher(next);
        while (m.find()) {
            amounts.add(rupees(m.group()));
        }
        log("Next step " + page.url() + ": " + (next.length() > 300 ? next.substring(0, 300) + "..." : next));
        screenshot("next-step-after-confirm-plan");
        Assert.assertTrue(moved, "Tapping \"Confirm Plan\" did not proceed: still on " + before + " after 30s");

        SoftAssert softly = new SoftAssert();
        softly.assertFalse(ERROR_TEXT.matcher(next).find(), "The next step shows an error: " + next);
        if (amounts.isEmpty()) {
            log("The next step (" + page.url() + ") shows no premium, so the passed premium and covers can't be seen there");
        } else {
            softly.assertTrue(amounts.contains(premium), "The next step shows " + amounts + " but not the Final Premium " + indianFormat(premium));
            softly.assertTrue(selected.stream().anyMatch(t -> next.toLowerCase().contains(t.toLowerCase())),
                "The next step names none of the added covers " + selected);
        }
        softly.assertAll();
    }
}
