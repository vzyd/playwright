package org.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class IFrameTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;


    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(2000));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage(){
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testIFrameWorkflow() {
        // 1.
        page.navigate("https://demoqa.com/frames");

        // 2. Захватываем первый фрайм
        FrameLocator firstFrame = page.frameLocator("#frame1");

        // 3. Проверяем текст внутри фрейма
        assertThat(firstFrame.locator("#sampleHeading"))
                .hasText("This is a sample page");

        // 4. Границы фрейма
        page.locator("#frame1").evaluate("e => e.style.border = '3px solid red'");

        // 5. Переходим на страницу с вложенными фреймами
        page.locator("'Nested Frames'").click();

        // 6. Работа с иерархией фреймов
        FrameLocator parentFrame = page.frameLocator("#frame1");
        FrameLocator childFrame = parentFrame.frameLocator("iframe");

        // 7. Проверяем текст в дочернем фрейме
        assertThat(childFrame.locator("body"))
                .containsText("Child Iframe");

        // 8. Делаем скриншот содержимого фрейма
        parentFrame.locator("body")
                .screenshot(new Locator.ScreenshotOptions()
                        .setPath(Paths.get("parent_frame.png")));

        // 9. Демострация работы с динамическими фреймами
        //    (В учебных целях - используем тот же фрейм)
        page.frameLocator("(//iframe[contains(@id, 'frame')])[1]")
                .locator("body")
                .click();

        System.out.println("Все шаги выполнены успешно");
    }
}
