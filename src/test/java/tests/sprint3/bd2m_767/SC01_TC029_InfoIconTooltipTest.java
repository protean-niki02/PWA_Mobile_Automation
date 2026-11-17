package tests.sprint3.bd2m_767;

import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * SC_01_TC_029 - Verify that tapping the info "i" icon shows its tooltip/detail.
 *
 * Sprint:        Sprint 3 - BD2M-767
 * Module:        4 Wheeler - Confirm Plan / Add-ons Configuration
 * Platform:      Android, iOS
 * Target device: Samsung Galaxy S24 Ultra (Android 14.0), iPhone 17 Pro (iOS 26)
 * Type / Suite:  Functional / Regression
 *
 * On the web journey (UAT 2.0.5+7+) the info "i" icons ({@code <span aria-label="Show info">} holding
 * Info_icon.png) are in the add-ons configuration panel ("Add Extra Protection") of the quotes listing: one
 * beside the panel and group headings and one beside every cover. Each opens a tooltip (a black bubble that
 * appears while the icon is hovered / touched). Every icon is touched in turn; its tooltip must appear
 * with text. The Confirm Plan screen itself has no info icon (only the non-interactive one beside its
 * "Premium is indicative ..." note), which is logged.
 */
public class SC01_TC029_InfoIconTooltipTest extends ConfirmPlanWebTestBase {

    private static final String PANEL_ICONS = "span[aria-label='Show info']";

    @Test(groups = {"sprint3", "SC_01", "bd2m_767", "regression"},
        description = "SC_01_TC_029: Verify that tapping the info \"i\" icon shows its tooltip/detail.")
    public void verifyInfoIconShowsTooltip() {
        openQuotes();
        Locator icons = page.locator(PANEL_ICONS);
        int count = icons.count();
        log(count + " info icon(s) in the add-ons panel");
        Assert.assertTrue(count > 0, "No info \"i\" icon (aria-label \"Show info\") is on the add-ons panel. Screen text: " + bodyText());

        // Do the app's tooltip (group-hover) styles apply on this device? Tailwind only applies hover styles
        // where the device can hover; a phone reports (hover: none).
        Object hoverCapable = page.evaluate("() => matchMedia('(hover: hover)').matches");
        Object hoverRuleInMedia = page.evaluate("""
            () => {
                let inMedia = 0, plain = 0;
                for (const sheet of document.styleSheets) {
                    let rules; try { rules = sheet.cssRules; } catch (e) { continue; }
                    for (const rule of rules) {
                        if (rule.cssText.includes('group-hover')) {
                            // Tailwind v4 nests the media query inside the rule: .group-hover\\:block { @media (hover:hover) {...} }
                            if (/@media\\s*\\(hover:\\s*hover\\)/.test(rule.cssText) || (rule.media && /hover/.test(rule.media.mediaText))) inMedia++; else plain++;
                        }
                    }
                }
                return 'hover rules inside @media (hover:hover): ' + inMedia + ', outside: ' + plain;
            }
            """);
        log("Device can hover: " + hoverCapable + "; " + hoverRuleInMedia);

        SoftAssert softly = new SoftAssert();
        List<String> shown = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Locator icon = icons.nth(i);
            icon.scrollIntoViewIfNeeded();
            // The tooltip is the bubble that follows the icon inside the same wrapper
            Locator tooltip = icon.locator("xpath=following-sibling::div[1]");
            String owner = icon.locator("xpath=ancestor::div[contains(@class,'items-center')][1]").innerText().replaceAll("\\s+", " ").trim();
            // Hover only: some icons sit inside a clickable section header, and tapping those collapses the panel
            icon.hover();
            page.waitForTimeout(500);
            boolean visible = tooltip.isVisible();
            String text = visible ? tooltip.innerText().replaceAll("\\s+", " ").trim() : "";
            log(String.format("Icon %d (%s): tooltip %s%s", i + 1, owner.length() > 40 ? owner.substring(0, 40) : owner,
                visible ? "shown: " : "NOT shown", text));
            if (i == 0) {
                screenshot("info-tooltip");
            }
            softly.assertTrue(visible && !text.isEmpty(), "Info icon " + i + " (" + owner + ") shows no tooltip text");
            if (visible && !text.isEmpty()) {
                shown.add(text);
            }
            page.mouse().move(0, 0);
        }
        softly.assertAll();
        log(shown.size() + " of " + count + " info icons showed their tooltip");
    }
}
