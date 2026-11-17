package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * SC_01_TC_016 - Verify that covers are grouped under the "Personal Accident Covers" heading.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Under Optional Covers a Personal Accident heading must be shown with its covers below it - Owner
 * Driver, Unnamed Passenger and Paid Driver. The app writes the heading "Personal Accident Cover"; a
 * wording difference from the TC's "Personal Accident Covers" is logged, not failed.
 */
public class SC01_TC016_PersonalAccidentGroupTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_016: Verify that covers are grouped under the \"Personal Accident Covers\" heading.")
    public void verifyPersonalAccidentGroup() {
        QuotesListingPage screen = openConfirmPlan();
        List<String> problems = checkGroup(screen, "Personal Accident Covers",
            Pattern.compile("(?i)^\\s*personal\\s+accident\\s+covers?\\s*$"), List.of("Owner Driver", "Unnamed Passenger", "Paid Driver"));
        Assert.assertTrue(problems.isEmpty(), "Personal Accident covers are not grouped as expected: " + problems);
    }
}
