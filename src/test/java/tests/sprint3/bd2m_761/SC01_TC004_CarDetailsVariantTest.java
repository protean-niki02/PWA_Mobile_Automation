package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.ReviewCarDetailsPage;

import java.util.regex.Pattern;

/**
 * SC_01_TC_004 - Verify that the vehicle summary card displays the correct Variant.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * Checked on the "Your Car Details" card of the car details screen, without moving on to the
 * quotes: its "Variant &amp; Fuel Type" value (e.g. "ZXI PETROL") must carry a variant - what is left
 * once the fuel type is taken off (e.g. "ZXI"). The value's close-up is saved as evidence.
 */
public class SC01_TC004_CarDetailsVariantTest extends FourWheelerReviewTestBase {

    private static final Pattern FUEL_TYPE = Pattern.compile("(?i)\\s*\\b(petrol|diesel|cng|lpg|electric|ev|hybrid)\\b\\s*");

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_004: Verify that the vehicle summary card displays the correct Variant.")
    public void verifyVariantIsDisplayed() {
        ReviewCarDetailsPage review = openReviewScreen();

        String value = assertCarDetailShown(review, ReviewCarDetailsPage.VARIANT_AND_FUEL_TYPE);
        String variant = FUEL_TYPE.matcher(value).replaceAll(" ").trim();
        log("Variant: " + variant);
        Assert.assertFalse(variant.isEmpty(), "The vehicle summary card does not display a variant: Variant & Fuel Type is '"
            + value + "' (fuel type only)");
    }
}
