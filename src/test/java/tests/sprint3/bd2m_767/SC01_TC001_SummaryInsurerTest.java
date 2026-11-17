package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_001 - Verify that the plan summary card displays the correct insurer for the selected quote.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 *
 * The first quote card's premium button is tapped on the listing (its insurer noted); on Confirm Plan
 * the plan summary card under "Confirm your Bima Vahan Plan" must name that same insurer.
 */
public class SC01_TC001_SummaryInsurerTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "sanity"},
        description = "SC_01_TC_001: Verify that the plan summary card displays the correct insurer for the selected quote.")
    public void verifySummaryInsurer() {
        QuotesListingPage screen = openConfirmPlan();
        Assert.assertNotNull(chosenInsurer, "Could not tell which insurer's quote was selected on the listing");
        java.util.List<String> texts = screen.semanticsTexts();
        int heading = texts.indexOf(HEADING);
        // The insurer name comes after the screen title, which itself contains "Bima Vahan"
        String shown = texts.stream().skip(heading + 1L).filter(t -> !t.trim().equalsIgnoreCase(HEADING)
            && QuotesListingPage.INSURER.matcher(t).find()).findFirst().orElse(null);
        log("Selected on the listing: " + chosenInsurer + "; plan summary card shows: " + shown);
        screenshot("summary-insurer");
        Assert.assertEquals(shown, chosenInsurer, "The plan summary card shows \"" + shown + "\", but the selected quote was "
            + chosenInsurer);
    }
}
