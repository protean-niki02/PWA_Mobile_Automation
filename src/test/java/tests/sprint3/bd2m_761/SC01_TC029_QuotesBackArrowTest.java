package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;
import pages.fourwheeler.ReviewCarDetailsPage;

import java.util.List;

/**
 * SC_01_TC_029 - Verify that the back arrow returns the user to the previous screen.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  -
 *
 * The quotes listing is reached from the "Get Your Car Insurance Quote" review screen
 * (#/4w-vehicle-details) via "View Prices". Once the quotes are loaded, tapping the header's back
 * arrow must:
 * <ul>
 *   <li>leave the quotes listing within 15s;</li>
 *   <li>return to that review screen ("Your Car Details" with "View Prices");</li>
 *   <li>keep the car the user chose (its Make &amp; Model is still shown), so the user can carry on.</li>
 * </ul>
 * If the app asks to confirm going back, that is logged and confirmed. Screenshots before and after.
 */
public class SC01_TC029_QuotesBackArrowTest extends QuotesListingTestBase {

    private static final int BACK_TIMEOUT_MS = 15_000;

    @Test(groups = {"sprint3", "SC_01"},
        description = "SC_01_TC_029: Verify that the back arrow returns the user to the previous screen.")
    public void verifyBackArrowReturnsToPreviousScreen() {
        QuotesListingPage quotes = openLoadedQuotes();
        String listingRoute = quotes.route();
        screenshot("before-back");

        quotes.tapBack();
        confirmGoingBackIfAsked();
        ReviewCarDetailsPage review = new ReviewCarDetailsPage(page);
        boolean returned = waitForReview(review, listingRoute);
        review.waitForImagesToSettle();
        String route = review.route();
        String makeAndModel = returned ? review.carDetail(ReviewCarDetailsPage.MAKE_AND_MODEL) : null;
        log(String.format("Back arrow: #%s -> #%s; review screen shown: %s; Make & Model: %s (chosen: %s)", listingRoute, route,
            returned, makeAndModel, selectedVehicle));
        screenshot("after-back");

        SoftAssert softly = new SoftAssert();
        softly.assertNotEquals(route, listingRoute, "Tapping the back arrow did not leave the quotes listing");
        softly.assertTrue(returned, "Tapping the back arrow did not return to the previous screen (\""
            + ReviewCarDetailsPage.YOUR_CAR_DETAILS + "\" review screen, #" + ReviewCarDetailsPage.ROUTE + "); now on #" + route
            + ". Screen text: " + review.semanticsText());
        softly.assertTrue(!returned || (makeAndModel != null && makeAndModel.equalsIgnoreCase(selectedVehicle)),
            "Back on the review screen the chosen car is not kept: Make & Model shows \"" + makeAndModel + "\", chosen \""
                + selectedVehicle + "\"");
        softly.assertAll();
    }

    /** If the app asks to confirm going back (Leave / Yes / Go back / Confirm), logs and confirms it. */
    private void confirmGoingBackIfAsked() {
        page.waitForTimeout(1_500);
        for (String name : List.of("Leave", "Yes", "Go back", "Go Back", "Confirm")) {
            Locator button = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
            if (button.count() > 0 && button.first().isVisible()) {
                log("The app asked to confirm going back - tapped \"" + name + "\"");
                button.first().click();
                return;
            }
        }
    }

    /** Waits for the review screen: its route, or "Your Car Details" with "View Prices" off the listing. */
    private boolean waitForReview(ReviewCarDetailsPage review, String listingRoute) {
        try {
            page.waitForCondition(() -> !review.route().equals(listingRoute) && (review.route().equals(ReviewCarDetailsPage.ROUTE)
                    || (review.semanticsText().contains(ReviewCarDetailsPage.YOUR_CAR_DETAILS) && review.viewPricesButton().count() > 0)),
                new Page.WaitForConditionOptions().setTimeout(BACK_TIMEOUT_MS));
            page.waitForTimeout(1_000);
            return true;
        } catch (com.microsoft.playwright.TimeoutError notBack) {
            return false;
        }
    }
}
