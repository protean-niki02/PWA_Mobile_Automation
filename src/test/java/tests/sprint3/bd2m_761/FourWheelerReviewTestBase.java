package tests.sprint3.bd2m_761;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import config.DeviceProfile;
import config.TestConfig;
import org.testng.Assert;
import org.testng.SkipException;
import pages.fourwheeler.CarNumberPage;
import pages.fourwheeler.ReviewCarDetailsPage;
import support.ImageCompare;
import support.ScreenPixels;

import java.util.regex.Pattern;

/**
 * Shared steps for Sprint 3 (BD2M-761), SC_01 - Module: 4 Wheeler - Review Car & Previous Policy Fetched Details.
 * Target devices (sheet): Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26).
 *
 * Test data comes from config.properties: fourWheeler.registrationNumber (complete fetch)
 * and fourWheeler.incompleteRegistrationNumber (incomplete fetch). A test whose data is not
 * set is SKIPPED, not failed.
 */
public abstract class FourWheelerReviewTestBase extends BaseTest {

    /**
     * Minimum width (CSS pixels) of a solid green shape in a section's title row to count as
     * the completion tick. The tick badge is about 30 wide; the card's green border line is 1-2.
     */
    protected static final double TICK_MIN_WIDTH = 16;

    /** Share of pixels allowed to differ from the approved screen design. */
    protected static final double MAX_DESIGN_DIFF = 0.05;

    private static final Pattern EMPTY_VALUE = Pattern.compile("(?i)-+|n/?a|null|select.*|not available");

    @Override
    protected DeviceProfile defaultDevice() {
        return DeviceProfile.ANDROID;
    }

    protected String registrationNumber() {
        return testData("fourWheeler.registrationNumber");
    }

    protected String incompleteRegistrationNumber() {
        return testData("fourWheeler.incompleteRegistrationNumber");
    }

    private String testData(String key) {
        String value = TestConfig.optional(key);
        if (value.isEmpty()) {
            throw new SkipException("Test data missing: set " + key
                + " in src/test/resources/config.properties (or pass -D" + key + "=...)");
        }
        return value;
    }

    /** Opens the 4 Wheeler journey, fetches the test car, and returns the review screen. */
    protected ReviewCarDetailsPage openReviewScreen() {
        String registration = registrationNumber();
        CarNumberPage entry = new CarNumberPage(page).open();
        screenshot("car-number-screen");
        log("Fetching car details for " + registration);
        ReviewCarDetailsPage review = entry.fetchCarDetails(registration);
        screenshot("review-screen");
        return review;
    }

    /**
     * Checks a "Your Car Details" label shows a real fetched value (not empty, "-", "NA",
     * "Select..."), saves a screenshot of it, and returns the value.
     */
    protected String assertCarDetailShown(ReviewCarDetailsPage review, String label) {
        String value = review.carDetail(label);
        log(label + ": " + value);
        Assert.assertNotNull(value, "'" + label + "' is not shown on the " + ReviewCarDetailsPage.YOUR_CAR_DETAILS + " card");
        Assert.assertFalse(value.isBlank() || EMPTY_VALUE.matcher(value.trim()).matches(),
            "'" + label + "' has no fetched value: '" + value + "'");
        screenshot("value-" + label.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase());
        return value;
    }

    /**
     * True when a green completion tick is drawn in the section's title row: a solid green
     * shape at least {@link #TICK_MIN_WIDTH} wide (the card's thin green border doesn't count).
     * The row image is saved as evidence. Flutter draws the tick without a label, so pixels are checked.
     */
    protected boolean hasCompletionTick(ReviewCarDetailsPage review, String sectionTitle) {
        BoundingBox row = review.titleRow(sectionTitle);
        Assert.assertNotNull(row, "'" + sectionTitle + "' is not on screen");
        byte[] rowImage = ScreenPixels.captureRow(page, row, 12);
        String name = sectionTitle.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
        saveEvidence("title-row-" + name + ".png", rowImage);
        double scale = ScreenPixels.scale(rowImage, page.viewportSize().width);
        double widest = ScreenPixels.widestGreenShape(rowImage) / scale;
        log(String.format("%s title row: widest green shape %.0f px wide (tick needs %.0f)", sectionTitle, widest, TICK_MIN_WIDTH));
        return widest >= TICK_MIN_WIDTH;
    }

    /** Fetches a car and waits for either the review card or a fetch-problem message. */
    protected ReviewCarDetailsPage fetchAndWaitForOutcome(String registration) {
        CarNumberPage entry = new CarNumberPage(page).open();
        screenshot("car-number-screen");
        entry.enterRegistrationNumber(registration);
        entry.tapFetchCarDetails();
        ReviewCarDetailsPage review = new ReviewCarDetailsPage(page);
        Locator outcome = review.yourCarDetailsTitle();
        for (String message : ReviewCarDetailsPage.FETCH_PROBLEM_MESSAGES) {
            outcome = outcome.or(page.getByText(message));
        }
        outcome.first().waitFor(new Locator.WaitForOptions().setTimeout(TestConfig.navigationTimeoutMs()));
        review.waitForImagesToSettle();
        screenshot("fetch-outcome");
        return review;
    }

    /** Compares the current screen with an approved design image; saves both as evidence. */
    protected void assertMatchesDesign(byte[] approved, String approvedName) {
        byte[] app = page.screenshot(new Page.ScreenshotOptions());
        saveEvidence("design-approved_" + approvedName, approved);
        saveEvidence("design-app_" + approvedName, app);
        ImageCompare.Result result = ImageCompare.compare(approved, app, MAX_DESIGN_DIFF);
        log(approvedName + ": " + (result.matches() ? "MATCHES" : "DIFFERS") + " - " + result.detail());
        Assert.assertTrue(result.matches(), "Screen does not match the approved design " + approvedName + ": " + result.detail());
    }
}
