package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TracingDebugTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
        );
    }

    @BeforeEach
    void initContext(TestInfo testInfo) {
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        );
        page = context.newPage();
    }

    @AfterEach
    void saveTrace(TestInfo testInfo) throws IOException {

       String testName = testInfo.getTestMethod().get().getName();
       Path tracesDir = Paths.get("traces");
       Files.createDirectories(tracesDir);

       Path tracePath = tracesDir.resolve(testName + ".zip");
       context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
       attachTrace(tracePath.getFileName().toString());

       context.close();


    }
    @AfterAll
    static void closeBrowser() {
        playwright.close();
            }

    @Attachment(value = "Трассировка: {name}", type = "application/zip")
    private byte[] attachTrace(String name) throws IOException {
        return Files.readAllBytes(
                Paths.get("traces/" + name));
    }


    @Test
    void  successfulLoginTest() {
        Allure.step("1. Открытие страницы логина", () -> {
            page.navigate("https://demoqa.com/login", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        });

        Allure.step("2. Заполнение учетных данных", () -> {
            page.locator("#userName").waitFor();
            page.fill("#userName", "testuser");
            page.fill("#password", "Test@123");
        });

        Allure.step("3. Клик по кнопке входа", () -> {
            // 3. Ожидание кнопки перед кликом
            Locator loginButton = page.locator("#login");
            loginButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            loginButton.click();
        });

        Allure.step("4. Проверка успешного входа", () -> {
            // 7. Ожидание через локатор
            Locator userName = page.locator("#userName-value");
            userName.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(15000));

            assertTrue(userName.isVisible());
        });
    }

}
