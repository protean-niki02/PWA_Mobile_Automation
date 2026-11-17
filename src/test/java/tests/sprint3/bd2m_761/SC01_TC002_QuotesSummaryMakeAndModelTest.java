package tests.sprint3.bd2m_761;

import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_002 - Verify that the vehicle summary card displays the correct Make &amp; Model.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        Customer Creation &amp; other functionality
 * Requirement:   US-UCM-01 - 4 Wheeler - Quotes Listing / Product Comparison
 * Scenario:      SC_01 - Validate the "4 Wheeler - Quotes Listing / Product Comparison" functionality.
 * Pre-requisite: User is on the Quotes Listing screen
 * Test data:     Car with Make &amp; Model (e.g. Maruti Suzuki Swift) - fourWheeler.registrationNumber
 * Step 1:        Navigate to the Quotes Listing screen &amp; check vehicle summary card
 * Expected:      Vehicle summary card should display the correct Make &amp; Model.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * "Correct" = the Make &amp; Model the review screen showed for the selected car.
 */
public class SC01_TC002_QuotesSummaryMakeAndModelTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "positive"},
        description = "SC_01_TC_002: Verify that the vehicle summary card displays the correct Make & Model.")
    public void verifySummaryShowsMakeAndModel() {
        QuotesListingPage quotes = openLoadedQuotes();
        assertSummaryShows(quotes, "Make & Model", selectedVehicle);
    }
}
