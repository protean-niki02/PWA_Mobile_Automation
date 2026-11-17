package tests.sprint3.bd2m_761;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.fourwheeler.QuotesListingPage;

import java.util.List;

/**
 * SC_01_TC_007 - Verify that the IDV control is editable.
 *
 * Sprint:        Sprint 3 - BD2M-761
 * Module:        4 Wheeler - Quotes Listing / Product Comparison
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  - / Regression
 *
 * On the quotes listing, taps the IDV control ("IDV ₹3,00,000 ✎"). It opens the IDV editor: "Select
 * your vehicle IDV", an "Insured Declared Value" field (₹3,00,000), a slider showing the IDV in green,
 * the allowed range under it (e.g. 1,00,000 - 5,00,000) and Update. The IDV must be editable - the
 * shown IDV must change:
 * <ol>
 *   <li>by typing: the field is tapped where it is drawn (Flutter's hidden input can't be clicked
 *       directly), cleared, and a value inside the range is typed - the middle of the range, rounded
 *       to a thousand (a number above the maximum, e.g. the old IDV with digits added, is rightly
 *       refused);</li>
 *   <li>if typing changes nothing (the field may be read-only), by the slider: its track is tapped
 *       at 75%, above the range labels.</li>
 * </ol>
 * Which way worked is logged. The change is not applied (no Update); SC_01_TC_008 covers that.
 * Screenshots of the editor before and after are saved.
 */
public class SC01_TC007_QuotesIdvEditableTest extends QuotesListingTestBase {

    @Test(groups = {"sprint3", "SC_01", "regression"},
        description = "SC_01_TC_007: Verify that the IDV control is editable.")
    public void verifyIdvControlIsEditable() {
        QuotesListingPage quotes = openLoadedQuotes();
        quotes.scrollToTop();
        List<String> editor = tapAndCollectNewTexts(quotes, quotes.idvControl(), "IDV");

        Locator label = quotes.text(IDV_FIELD_LABEL);
        List<Long> range = amounts(editor);
        Assert.assertTrue(label.count() > 0 && range.size() >= 2, "Tapping the IDV control did not open the IDV editor "
            + "(\"" + IDV_FIELD_LABEL + "\" and its range). Editor texts: " + editor);
        long min = range.stream().mapToLong(Long::longValue).min().getAsLong();
        long max = range.stream().mapToLong(Long::longValue).max().getAsLong();
        long before = shownIdv(quotes);
        log(String.format("IDV editor: shows %,d, allowed %,d to %,d", before, min, max));

        // 1. Type a new IDV into the field
        long target = Math.round((min + max) / 2.0 / 1000) * 1000;
        if (target == before) {
            target += 1000;
        }
        typeIntoIdvField(quotes, target);
        long afterTyping = shownIdv(quotes);
        screenshot("idv-after-typing");
        log(String.format("Typed %,d into the field: the editor now shows %,d", target, afterTyping));
        String how = afterTyping != before ? "typing into the field" : null;

        // 2. If typing changed nothing, move the slider
        long after = afterTyping;
        if (how == null) {
            BoundingBox minLabel = quotes.text(indianFormat(min)).boundingBox();
            BoundingBox maxLabel = quotes.text(indianFormat(max)).boundingBox();
            double left = minLabel.x;
            double right = maxLabel.x + maxLabel.width;
            quotes.tapAt(left + (right - left) * 0.75, minLabel.y - 25);
            page.waitForTimeout(1_500);
            after = shownIdv(quotes);
            screenshot("idv-after-slider");
            log(String.format("The field didn't take typing - tapped the slider at 75%%: the editor now shows %,d", after));
            how = after != before ? "the slider" : null;
        }

        Assert.assertNotNull(how, String.format("The IDV is not editable: it still shows %,d after typing %,d into the field "
            + "and after moving the slider", before, target));
        log(String.format("IDV changed from %,d to %,d by %s", before, after, how));
        Assert.assertTrue(after >= min && after <= max, String.format("The edited IDV %,d is outside the allowed %,d to %,d", after, min, max));
    }
}
