package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_022 - Verify that a mandatory cover is indicated accordingly.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * The mandatory cover (Owner Driver) must carry a "Mandatory by Law" tag inside its own card - below
 * its title and above the next cover - and no other cover may carry it.
 */
public class SC01_TC022_MandatoryCoverIndicatedTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_022: Verify that a mandatory cover is indicated accordingly.")
    public void verifyMandatoryCoverIndicated() {
        QuotesListingPage screen = openConfirmPlan();
        List<Cover> covers = covers(screen);
        Cover owner = find(covers, OWNER_DRIVER);
        Assert.assertNotNull(owner, "No \"" + OWNER_DRIVER + "\" cover on Confirm Plan. Screen text: " + screen.semanticsText());
        Cover next = covers.size() > covers.indexOf(owner) + 1 ? covers.get(covers.indexOf(owner) + 1) : null;

        Locator tag = screen.text(MANDATORY);
        Assert.assertTrue(tag.count() > 0, "No \"" + MANDATORY + "\" indication on Confirm Plan. Screen text: " + screen.semanticsText());
        screen.bringIntoView(tag);
        BoundingBox tagBox = tag.boundingBox();
        BoundingBox ownerBox = titleOf(screen, owner).boundingBox();
        BoundingBox nextBox = next == null ? null : titleOf(screen, next).boundingBox();
        boolean inOwnerCard = tagBox.y > ownerBox.y && (nextBox == null || tagBox.y < nextBox.y);
        long tags = screen.semanticsTexts().stream().filter(t -> t.trim().equalsIgnoreCase(MANDATORY)).count();
        log(String.format("\"%s\" at y %.0f; %s title at y %.0f; next cover %s at y %s; tags on screen: %d", MANDATORY, tagBox.y,
            OWNER_DRIVER, ownerBox.y, next == null ? "-" : next.title(), nextBox == null ? "-" : String.format("%.0f", nextBox.y), tags));
        screenshot("mandatory-indicated");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(inOwnerCard, "\"" + MANDATORY + "\" is not shown inside the " + OWNER_DRIVER + " card");
        softly.assertEquals(tags, 1L, "\"" + MANDATORY + "\" is shown " + tags + " times; only the mandatory cover should carry it");
        softly.assertAll();
    }
}
