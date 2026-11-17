package tests.sprint3.bd2m_761;

import org.testng.annotations.Test;
import pages.fourwheeler.ReviewCarDetailsPage;

/**
 * SC_01_TC_002 - Verify that "Your Car Details" card displays the auto-fetched Make & Model value.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Review Car & Previous Policy Fetched Details
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Sanity
 */
public class SC01_TC002_CarDetailsMakeAndModelTest extends FourWheelerReviewTestBase {

    @Test(groups = {"sprint3", "SC_01", "functional", "sanity"},
        description = "SC_01_TC_002: Verify that \"Your Car Details\" card displays the auto-fetched Make & Model value.")
    public void verifyMakeAndModelIsDisplayed() {
        ReviewCarDetailsPage review = openReviewScreen();

        assertCarDetailShown(review, ReviewCarDetailsPage.MAKE_AND_MODEL);
    }
}
