package io.github.adminconsole.demo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class AdminConsoleBrowserIT {
    @Test
    void adminCanLoginSearchBeansAndListJobs() {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage();
            page.navigate("http://127.0.0.1:18080/admin-console/index.html");
            page.locator("#user").fill("admin");
            page.locator("#pass").fill("admin123");
            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Login")).click();
            assertThat(page.locator("#out")).containsText("admin");
            assertThat(page.locator("#attachArthasButton")).isEnabled();
            assertThat(page.locator("#arthasStatus")).containsText("Ready to attach");
            page.locator("#attachArthasButton").click();
            assertThat(page.locator("#arthasStatus")).containsText("Attached");
            page.locator("#cmd").fill("sc io.github.adminconsole.demo.FeatureFlags");
            page.locator("#executeArthasButton").click();
            assertThat(page.locator("#arthasOut")).containsText("Matched classes:");
            assertThat(page.locator("#arthasOut")).containsText("io.github.adminconsole.demo.FeatureFlags");
            assertThat(page.locator("#arthasOut")).containsText("Exit code: 0");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Refresh")).first().click();
            assertThat(page.locator("#flags")).containsText("featureFlags.checkoutEnabled");
            page.locator("#flags select").selectOption("false");
            page.locator("#flags").getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Save")).click();
            assertThat(page.locator("#out")).containsText("updated");

            page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Refresh")).last().click();
            assertThat(page.locator("#jobs")).containsText("demoJobs.refreshCache");
            page.locator("#jobs .item").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHasText("demoJobs.refreshCache")).getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Trigger")).click();
            assertThat(page.locator("#out")).containsText("triggered");
        }
    }
}
