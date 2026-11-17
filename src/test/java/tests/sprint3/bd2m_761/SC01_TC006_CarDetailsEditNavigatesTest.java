package tests.sprint3.bd2m_761;

import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.ReviewCarDetailsPage;

/**
 * SC_01_TC_006 - Verify that the Edit (pencil) icon on the vehicle summary card navigates back to the car details screen.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * On the car details screen, taps "✎ Edit" on the "Your Car Details" card - the "Edit" button if
 * the app labels it, else the pencil next to the card's title. The car details screen for editing
 * must open within 15s: the app leaves the review route, or the editable car details form appears
 * (separate Fuel Type and Variant fields, or input / dropdown fields). Screenshots before and after.
 */
public class SC01_TC006_CarDetailsEditNavigatesTest extends FourWheelerReviewTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_006: Verify that the Edit (pencil) icon on the vehicle summary card navigates back to the car details screen.")
    public void verifyEditOpensCarDetails() {
        ReviewCarDetailsPage review = openReviewScreen();
        review.bringIntoView(review.yourCarDetailsTitle());
        screenshot("before-edit");
        int fieldsBefore = editableFields();

        review.tapEditCarDetails();
        log("Tapped Edit (pencil) on the \"" + ReviewCarDetailsPage.YOUR_CAR_DETAILS + "\" card");

        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline && !onCarDetailsForm(review, fieldsBefore)) {
            page.waitForTimeout(500);
        }
        review.waitForImagesToSettle();
        screenshot("car-details-screen");
        log("After Edit the app is on #" + review.route() + " with " + editableFields() + " editable field(s) (before "
            + fieldsBefore + "); Fuel Type field: " + review.textOrLabel("Fuel Type").count() + ", Variant field: "
            + review.textOrLabel("Variant").count());
        Assert.assertTrue(onCarDetailsForm(review, fieldsBefore), "Tapping Edit (pencil) on the \""
            + ReviewCarDetailsPage.YOUR_CAR_DETAILS + "\" card did not open the car details screen; the app is on #" + review.route());
    }

    /** The car details form: another route, more editable fields, or separate Fuel Type and Variant fields. */
    private boolean onCarDetailsForm(ReviewCarDetailsPage review, int fieldsBefore) {
        return !review.route().equals(ReviewCarDetailsPage.ROUTE)
            || editableFields() > fieldsBefore
            || (review.textOrLabel("Fuel Type").count() > 0 && review.textOrLabel("Variant").count() > 0);
    }

    private int editableFields() {
        return page.getByRole(AriaRole.TEXTBOX).count() + page.getByRole(AriaRole.COMBOBOX).count();
    }
}
