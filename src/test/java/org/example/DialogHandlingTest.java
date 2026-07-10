package org.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DialogHandlingTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;


    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
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
    @DisplayName("Обработка диалоговых окон: alert, confirm, prompt")
    void testDialogHandling() {

        page.navigate("https://demoqa.com/alerts");
        // =========== 1. Обработка Alert ======================
        page.onceDialog(dialog -> {
            assertEquals("alert", dialog.type());
            assertEquals("You clicked a button", dialog.message());
            dialog.accept();
        });
        page.click("#alertButton");
        page.waitForTimeout(500); // Небольшая пауза для демонстрации

        // =========== 2. Обработка Confirm (Accept) ======================
        page.onceDialog(dialog -> {
            assertEquals("confirm", dialog.type());
            assertEquals("Do you confirm action?", dialog.message());
            dialog.accept();
        });
        page.click("#confirmButton");
        assertEquals("You selected Ok", page.locator("#confirmResult").innerText());

        // ========== 3. Обработка Confirm (Dismiss) =============
        page.onceDialog(dialog -> {
            assertEquals("confirm", dialog.type());
            dialog.dismiss();
        });
        page.click("#confirmButton");
        assertEquals("You selected Cancel", page.locator("#confirmResult").innerText());

        // =========== 4. Обработка Prompt ============
        page.onceDialog(dialog -> {
            assertEquals("prompt", dialog.type());
            assertEquals("Please enter your name", dialog.message());
            dialog.accept("John Doe");
        });
        page.click("#promtButton");
        assertEquals("You entered John Doe", page.locator("#promptResult").innerText());

        // ========== 5. Паралельная обработка ==============
        page.onDialog(dialog -> {
            if ("alert".equals(dialog.type())) {
                dialog.accept();
            }
        });
        page.click("#timerAlertButton");
        page.waitForTimeout(6000); // Ожидание таймерного алерта

    }

}
