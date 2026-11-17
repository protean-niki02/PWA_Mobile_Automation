package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_018 - Verify that covers are grouped under the "Additional Own Damage Covers" heading.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Under Optional Covers an Additional Own Damage heading must be shown with its covers below it -
 * Non-Electrical Accessories, Electrical /Electronic Accessories and CNG/LPG/Bi-Fuel Kit (External). The
 * app writes the heading "Additional Own Damage"; a wording difference from the TC's "Additional Own
 * Damage Covers" is logged, not failed.
 */
public class SC01_TC018_AdditionalOwnDamageGroupTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_018: Verify that covers are grouped under the \"Additional Own Damage Covers\" heading.")
    public void verifyAdditionalOwnDamageGroup() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> problems = checkGroup(screen, "Additional Own Damage Covers",
            Pattern.compile("(?i)^\\s*additional\\s+own\\s+damage(\\s+covers?)?\\s*$"),
            List.of("Non-Electrical Accessories", "Electrical /Electronic Accessories", "CNG/LPG/Bi-Fuel Kit (External)"));
        Assert.assertTrue(problems.isEmpty(), "Additional Own Damage covers are not grouped as expected: " + problems);
    }
}
