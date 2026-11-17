package tests.sprint3.bd2m_761;

import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_003 - Verify that the vehicle summary card displays the correct Fuel type.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        Customer Creation &amp; other functionality
 * Requirement:   US-UCM-01 - 4 Wheeler - Quotes Listing / Product Comparison
 * Scenario:      SC_01 - Validate the "4 Wheeler - Quotes Listing / Product Comparison" functionality.
 * Pre-requisite: User is on the Quotes Listing screen
 * Test data:     Car with Fuel type (e.g. Petrol) - fourWheeler.registrationNumber
 * Step 1:        Navigate to the Quotes Listing screen &amp; check vehicle summary card
 * Expected:      Vehicle summary card should display the correct Fuel type.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * "Correct" = the fuel type the review screen showed (last word of "Variant &amp; Fuel Type").
 */
public class SC01_TC003_QuotesSummaryFuelTypeTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "positive"},
        description = "SC_01_TC_003: Verify that the vehicle summary card displays the correct Fuel type.")
    public void verifySummaryShowsFuelType() {
        QuotesListingPage quotes = openLoadedQuotes();
        assertSummaryShows(quotes, "Fuel type", fuelType());
    }
}
