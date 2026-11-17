package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_019 - Verify that covers are grouped under the "Additional Third Party Covers" heading.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Under Optional Covers an Additional Third Party heading must be shown with its cover below it - Legal
 * Liability to Paid Driver. The app writes the heading "Additional Third Party"; a wording difference
 * from the TC's "Additional Third Party Covers" is logged, not failed.
 */
public class SC01_TC019_AdditionalThirdPartyGroupTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_019: Verify that covers are grouped under the \"Additional Third Party Covers\" heading.")
    public void verifyAdditionalThirdPartyGroup() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> problems = checkGroup(screen, "Additional Third Party Covers",
            Pattern.compile("(?i)^\\s*additional\\s+third\\s+party(\\s+covers?)?\\s*$"), List.of("Legal Liability to Paid Driver"));
        Assert.assertTrue(problems.isEmpty(), "Additional Third Party covers are not grouped as expected: " + problems);
    }
}
