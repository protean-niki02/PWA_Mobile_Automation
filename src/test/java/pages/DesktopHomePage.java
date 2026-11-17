package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DesktopHomePage extends BasePage {

    private final Locator homeLogo = page.getByRole(AriaRole.LINK,
        new Page.GetByRoleOptions().setName("Bima Sugam Home"));
    private final Locator loginHeading = page.getByRole(AriaRole.HEADING,
        new Page.GetByRoleOptions().setName("Login").setExact(true));
    private final Locator loginViaOtpButton = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login via OTP"));

    public DesktopHomePage(Page page) {
        super(page);
    }

    public DesktopHomePage open() {
        navigate("/");
        return this;
    }

    public Locator homeLogo() {
        return homeLogo;
    }

    public Locator loginHeading() {
        return loginHeading;
    }

    public Locator loginViaOtpButton() {
        return loginViaOtpButton;
    }
}
