package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.ReviewCarDetailsPage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SC_01_TC_003 - Verify that the vehicle summary card displays the correct Fuel type.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * Checked on the "Your Car Details" card of the car details screen, without moving on to the
 * quotes: its "Variant &amp; Fuel Type" value (e.g. "ZXI PETROL") must end in a fuel type - Petrol,
 * Diesel, CNG, LPG, Electric / EV or Hybrid. The value's close-up is saved as evidence.
 */
public class SC01_TC003_CarDetailsFuelTypeTest extends FourWheelerReviewTestBase {

    private static final Pattern FUEL_TYPE = Pattern.compile("(?i)\\b(petrol|diesel|cng|lpg|electric|ev|hybrid)\\b");

    @Test(groups = {"sprint3", "SC_01", "functional", "regression"},
        description = "SC_01_TC_003: Verify that the vehicle summary card displays the correct Fuel type.")
    public void verifyFuelTypeIsDisplayed() {
        ReviewCarDetailsPage review = openReviewScreen();

        String value = assertCarDetailShown(review, ReviewCarDetailsPage.VARIANT_AND_FUEL_TYPE);
        Matcher fuel = FUEL_TYPE.matcher(value);
        Assert.assertTrue(fuel.find(), "The vehicle summary card does not display a fuel type: Variant & Fuel Type is '" + value + "'");
        log("Fuel type: " + fuel.group(1).toUpperCase());
    }
}
