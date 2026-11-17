package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * SC_01_TC_028 - Verify that the "Premiums given are indicative premium" tag is displayed.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * "Premiums given are indicative premium" must be shown under the Optional Covers heading, above the
 * first cover group, fully inside the screen. Its text colour is logged (the design shows it green on a
 * light green tag). Runs on the web journey (UAT 2.0.5+7+), see {@link ConfirmPlanWebTestBase}.
 */
public class SC01_TC028_IndicativePremiumTagTest extends ConfirmPlanWebTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_028: Verify that the \"Premiums given are indicative premium\" tag is displayed.")
    public void verifyIndicativePremiumTag() {
        openConfirmPlan();
        Locator tag = page.getByText(INDICATIVE).first();
        if (page.getByText(INDICATIVE).count() == 0) {
            log("Texts on Confirm Plan that mention \"indicative\": " + indicativeTexts());
            screenshot("indicative-tag-missing");
            Assert.fail("\"" + INDICATIVE + "\" is not shown on Confirm Plan. Only these texts mention \"indicative\": "
                + indicativeTexts());
        }
        tag.scrollIntoViewIfNeeded();
        BoundingBox box = tag.boundingBox();
        BoundingBox heading = page.getByText(OPTIONAL_COVERS, new Page.GetByTextOptions().setExact(true)).first().boundingBox();
        Locator firstGroupLocator = page.getByText("Personal Accident Cover", new Page.GetByTextOptions().setExact(true)).first();
        BoundingBox firstGroup = firstGroupLocator.count() == 0 ? null : firstGroupLocator.boundingBox();
        String colour = (String) tag.evaluate("e => getComputedStyle(e).color + ' on ' + getComputedStyle(e).backgroundColor");
        log(String.format("Tag at (%.0f, %.0f); Optional Covers at y %.0f; first group at y %s; colour %s", box.x, box.y, heading.y,
            firstGroup == null ? "-" : String.format("%.0f", firstGroup.y), colour));
        tag.screenshot(new Locator.ScreenshotOptions().setPath(evidenceFile("indicative-tag.png")));
        screenshot("indicative-tag");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(box.y > heading.y, "\"" + INDICATIVE + "\" is not under the Optional Covers heading");
        softly.assertTrue(firstGroup == null || box.y < firstGroup.y, "\"" + INDICATIVE + "\" is not above the cover groups");
        softly.assertTrue(box.x >= 0 && box.x + box.width <= page.viewportSize().width + 1, "\"" + INDICATIVE + "\" is cut off at the side");
        softly.assertAll();
    }
}
