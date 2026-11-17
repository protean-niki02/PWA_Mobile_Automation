package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_017 - Verify that covers are grouped under the "Add-on Covers" heading.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Under Optional Covers an Add-on heading must be shown with its covers below it - Zero Depreciation,
 * Engine &amp; Gear Protection, Breakdown Assistance (RSA), Consumables, Return to Invoice and
 * Geographical Extension. The app writes the heading "Add-ons covers"; a wording difference from the
 * TC's "Add-on Covers" is logged, not failed.
 */
public class SC01_TC017_AddOnGroupTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_017: Verify that covers are grouped under the \"Add-on Covers\" heading.")
    public void verifyAddOnGroup() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> problems = checkGroup(screen, "Add-on Covers", Pattern.compile("(?i)^\\s*add-?ons?\\s+covers?\\s*$"),
            List.of("Zero Depreciation", "Engine & Gear Protection", "Breakdown Assistance (RSA)", "Consumables", "Return to Invoice",
                "Geographical Extension"));
        Assert.assertTrue(problems.isEmpty(), "Add-on covers are not grouped as expected: " + problems);
    }
}
