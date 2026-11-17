package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.Arrays;
import java.util.List;

/**
 * SC_01_TC_001 - Verify that quotes are fetched and displayed for the selected vehicle.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        Customer Creation &amp; other functionality
 * Requirement:   US-UCM-01 - 4 Wheeler - Quotes Listing / Product Comparison
 * Scenario:      SC_01 - Validate the "4 Wheeler - Quotes Listing / Product Comparison" functionality.
 * Pre-requisite: User has entered car details
 * Test data:     Car with details entered (e.g. Maruti Suzuki Swift) - fourWheeler.registrationNumber
 * Step 1:        Navigate to the Quotes Listing screen for the selected vehicle
 * Expected:      Quotes should be fetched and displayed for the selected vehicle.
 * Type:          Positive
 * Designed by:   Sumit
 *
 * Fetches the test car, notes its Make &amp; Model, goes through View Prices to the quotes listing
 * and waits (up to 2 minutes) for "We are fetching the premium ..." to finish. At least one quote -
 * a premium amount - must then be displayed. The quotes are for the selected vehicle because the
 * journey started from that car; whether the listing also names the car is logged.
 */
public class SC01_TC001_QuotesFetchedForSelectedVehicleTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "positive"},
        description = "SC_01_TC_001: Verify that quotes are fetched and displayed for the selected vehicle.")
    public void verifyQuotesFetchedForSelectedVehicle() {
        QuotesListingPage quotes = openQuotesListing();

        long start = System.currentTimeMillis();
        boolean finished = quotes.waitForQuotes(QUOTES_TIMEOUT_MS);
        log(String.format("Quotes %s after %.1f s", finished ? "fetched" : "still fetching",
            (System.currentTimeMillis() - start) / 1000.0));
        screenshot("quotes");

        List<String> premiums = quotes.premiums();
        log(premiums.size() + " quote(s) displayed: " + premiums);
        String screenText = quotes.semanticsText();
        boolean namesVehicle = screenText.toUpperCase().contains(registrationNumber().toUpperCase())
            || (selectedVehicle != null && Arrays.stream(selectedVehicle.split("\\s+"))
                .filter(word -> word.length() > 2).anyMatch(word -> screenText.toUpperCase().contains(word.toUpperCase())));
        log("Selected vehicle " + selectedVehicle + (namesVehicle ? " is" : " is not") + " named on the quotes listing");

        Assert.assertTrue(finished, "Quotes were not fetched within " + Math.round(QUOTES_TIMEOUT_MS / 1000)
            + "s - the listing still shows \"" + QuotesListingPage.FETCHING_MESSAGE + "\"");
        Assert.assertFalse(premiums.isEmpty(), "No quotes are displayed for " + selectedVehicle + " ("
            + registrationNumber() + ")" + (quotes.showsNoQuotes() ? " - the listing says there are none" : "")
            + ". Screen text: " + screenText);
    }
}
