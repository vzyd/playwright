package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AdvancedWaitsDemo {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;


    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(500));
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
    void testWaitsInRealScenario() {
        //? 1. Автоматические ожидания
        page.navigate("https://demoqa.com/dynamic-properties");
        
        // Кнопка станет активной через 5с - автоожидание сработает!
        page.locator("#enableAfter").click();

        // Поле появится через 5с
        page.locator("#visibleAfter").click(); // И здесь автоожидание сработает!
        
        //? 2. Явные ожидания для сложных условий
        // Ждем появления элемента с таймаутом 7 секунд
        page.waitForSelector("#visibleAfter",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(7000));
        // Ожидаем изменения CSS-свойства (кастомное условие)
//        page.waitForFunction(
//                "() => window.getComputedStyle(document.querySelector('#colorChange')).color === 'rgb(220, 53, 69)'",
//                new Page.WaitForSelectorOptions().setTimeout(8000)
//        );
        // Ожидание перехода на страницу
//        page.waitForURL("**/checkout/confirmation",
//                new Page.WaitForURLOptions().setTimeout(5000));
        
        //? 3. Умные ассерты с ожиданием
        // Проверка текста с автоматическим ожиданием
        assertThat(page.locator("//h1[text()='Dynamic Properties']/following-sibling::p"))
                .hasText("This text has random Id",
                        new LocatorAssertions.HasTextOptions().setTimeout(5000));
        
        // ПРАВИЛЬНАЯ проверка видимости и активности
        Locator checkoutButton = page.locator("#visibleAfter");
        
        assertThat(checkoutButton)
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(3000));
        assertThat(checkoutButton)
                .isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(3000));
        
        // Дополнительная проверка атрибута
//        assertThat(checkoutButton)
//                .hasAttribute("data-status", "active",
//                        new LocatorAssertions.HasAttributeOptions().setTimeout(2000));

    }

}
