package tests.sprint3.bd2m_767;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.fourwheeler.QuotesListingPage;

/**
 * SC_01_TC_024 - Verify that deselecting an optional/add-on cover updates the Final Premium.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * An add-on cover (e.g. Zero Depreciation) is first selected so there is one to deselect; once the
 * Final Premium has risen, the same box is tapped again. Within 30s the box must show unselected and
 * the Final Premium must come down - back to what it was before the cover was selected.
 */
public class SC01_TC024_DeselectCoverUpdatesPremiumTest extends ConfirmPlanTestBase {

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_024: Verify that deselecting an optional/add-on cover updates the Final Premium.")
    public void verifyDeselectingCoverUpdatesPremium() {
        QuotesListingPage screen = openConfirmPlan();
        long base = finalPremium(screen);
        Cover cover = firstUnselected(screen, covers(screen), "add-on");
        Assert.assertNotNull(cover, "No unselected add-on cover with a premium. Screen text: " + screen.semanticsText());

        tapControl(screen, cover);
        screen.scrollToTop();
        long withCover = waitForFinalPremiumChange(screen, base, 30_000);
        Assert.assertTrue(withCover > base, "Selecting " + cover.title() + " did not raise the Final Premium (" + indianFormat(base) + " -> "
            + indianFormat(withCover) + "), so deselecting it can't be checked");
        screenshot("with-cover-selected");

        tapControl(screen, cover);
        boolean deselected = !isSelected(screen, cover);
        screenshot("cover-deselected");
        screen.scrollToTop();
        long after = waitForFinalPremiumChange(screen, withCover, 30_000);
        log(String.format("%s: Final Premium %s -> selected %s -> deselected %s; box %s", cover.title(), indianFormat(base),
            indianFormat(withCover), indianFormat(after), deselected ? "unselected" : "STILL selected"));
        screenshot("final-premium-after-deselecting");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(deselected, "Tapping " + cover.title() + "'s box again did not deselect it");
        softly.assertTrue(after < withCover, "Deselecting " + cover.title() + " did not update the Final Premium: " + indianFormat(withCover)
            + " -> " + indianFormat(after));
        softly.assertEquals(after, base, "After deselecting " + cover.title() + " the Final Premium is " + indianFormat(after)
            + ", not back to " + indianFormat(base));
        softly.assertAll();
    }
}
