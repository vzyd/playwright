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

public class AllureReportingTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    Video video;
    private Path screenshotDir;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
        );
    }

    @BeforeEach
    void initContext(TestInfo testInfo) {
        screenshotDir = Paths.get("screenshots/");
        try {
            Files.createDirectories(screenshotDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create screenshots directory", e);
        }

        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/")));
        page = context.newPage();
        video = page.video();
    }

    @RegisterExtension
    TestWatcher watcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext extensionContext, Throwable cause) {
            try {
                // Генерируем имя файла
                String testName = extensionContext.getDisplayName();
                Path screenshotPath = screenshotDir.resolve(testName + ".png");

                // Делаем и сохраняем скриншот
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                        .setPath(screenshotPath) // Сохраняем на диск
                        .setFullPage(true));

                // Прикрепляем в Аллюр
                saveScreenshotToAllure(screenshot, testName);
                System.out.println("Скриншот сохранен: " + screenshotPath);
            } catch (Exception e) {
                System.err.println ("Ошибка при создании скриншота: " + e.getMessage());
            }
        }

        @Attachment(value = "Скриншот при падении: {name}", type = "image/png")
        private byte[] saveScreenshotToAllure(byte[] screenshot, String name) {
            return screenshot;
        }
    };

    @AfterEach
    void tearDown(TestInfo testInfo) throws IOException {

        if (context != null) {
            if (video != null) {
                String videoName = testInfo.getDisplayName() + ".webm";
                Path videoPath = Paths.get("videos/", videoName);

                video.saveAs(videoPath);
                attachVideo(videoName);
            }
            context.close();
        }

    }
    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
       if (playwright != null){
           playwright.close();
       }
    }

    @Attachment(value = "Видео теста {name}", type = "video/webm")
    private byte[] attachVideo(String name) throws IOException {
        return Files.readAllBytes(
                Paths.get("videos/" + name + ".webm")
        );
    }

    @Test
    void testLoginWithScreenshots() {
        Allure.step("1. Открытие страницы логина", () -> {
           page.navigate("https://demoqa.com/login", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        });

        Allure.step("2. Заполнение учетных данных", () -> {
            page.waitForSelector("#userName", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.waitForSelector("#password", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

            page.fill("#userName", "testuser");
            page.fill("#password", "INVALID_PASSWORD");
        });

        Allure.step("3. Клик по кнопке входа", () -> {
            page.waitForSelector("#login", new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(5000));
            page.click("#login");

            page.waitForCondition(() ->
                    page.evaluate("() => document.readyState").equals("complete"),
                    new Page.WaitForConditionOptions().setTimeout(10000)
            );
        });

        Allure.step("4. Проверка успешного входа", () -> {
            page.waitForSelector("#userName-value",
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10000));

            assertTrue(page.isVisible("#userName-value"),
                    "Имя пользователя не отображается после входа");
        });
    }

}
