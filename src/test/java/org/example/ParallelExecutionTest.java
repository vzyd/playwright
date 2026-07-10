package org.example;


import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelExecutionTest {
    private Playwright playwright;
    private Browser browser;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterEach
    void teardown() {
        browser.close();
        playwright.close();
    }

    // Тест 1: Проверка заголовка Google
    @Test
    void testGoogleTitle() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://google.com");
        assertTrue(page.title().contains("Google"));
        context.close();
    }

    // Тест 2: Проверка Playwright documentation
    @Test
    void testPlaywrightDocs() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://playwright.dev/java");
        assertTrue(page.title().contains("Playwright"));
        context.close();
    }


    // Тест 3: Проверка  Wikipedia
    @Test
    void testWikipedia() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.navigate("https://en.wikipedia.org");
        assertTrue(page.title().contains("Wikipedia"));
        context.close();
    }

}
