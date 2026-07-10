package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тестовый класс для проверки динамической загрузки контента с использованием трассировки.
 * Демонстрирует интеграцию проверки сетевых запросов и создание динамических артефактов.
 */

public class DynamicLoadingApiTest {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    BrowserContext context;

    /**
     * Тест проверки динамического контента:
     * 1. Инициализация браузера с включенной трассировкой
     * 2. Переход на тестовую страницу
     * 3. Мониторинг сетевых ответов с валидацией статусов
     * 4. Взаимодействие с элементами интерфейса
     * 5. Сохранение трассировочных данных при успешном выполнении
     */

    @BeforeEach
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
                        .setSlowMo(5000)
        );
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void teardown() {
        if(page != null) page.close();
        if(context != null) context.close();
        if(browser != null) browser.close();
        if(playwright != null) playwright.close();
    }

    @Test
    void testDynamicLoading() {
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true));
        page = context.newPage();
        page.navigate("https://the-internet.herokuapp.com/dynamic_loading/1");

        page.onResponse(response -> {
            if (response.url().contains("/dynamic_loading")) {
                assertEquals(200, response.status(), "Неверный статус ответа для URL: " + response.url());
            }
        });

        page.click("button");
        Locator finishText = page.locator("#finish");
        finishText.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        assertEquals("Hello World!", finishText.textContent().trim(),
                "Текст элемента не соответствует ожидаемому");

        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace/trace-success.zip")));

    }
}
