package tests.sprint3.bd2m_761;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.ReviewCarDetailsPage;

import java.util.regex.Pattern;

/**
 * SC_01_TC_005 - Verify that the vehicle summary card displays the correct Ownership.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * Checked on the "Your Car Details" card of the car details screen, without moving on to the
 * quotes: its "Owned By" value must be a real ownership type - Individual or Company /
 * Organisation / Corporate. The value's close-up is saved as evidence.
 */
public class SC01_TC005_CarDetailsOwnershipTest extends FourWheelerReviewTestBase {

    private static final Pattern OWNERSHIP = Pattern.compile("(?i)\\b(individual|company|organi[sz]ation|corporate|firm)\\b");

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_005: Verify that the vehicle summary card displays the correct Ownership.")
    public void verifyOwnershipIsDisplayed() {
        ReviewCarDetailsPage review = openReviewScreen();

        String ownership = assertCarDetailShown(review, ReviewCarDetailsPage.OWNED_BY);
        Assert.assertTrue(OWNERSHIP.matcher(ownership).find(), "The vehicle summary card does not display a valid ownership: "
            + "Owned By is '" + ownership + "'");
    }
}
