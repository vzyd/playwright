package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ViewportSize;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrowserManagementTest {

    static Playwright playwright;
    static Browser browser;
//    BrowserContext context;
//    Page page;


    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(500));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

//    @BeforeEach
//    void createContextAndPage(){
//        context = browser.newContext();
//        page = context.newPage();
//    }
//
//    @AfterEach
//    void closeContext(){
//        context.close();
//    }

    @Test
    @DisplayName("Управление браузером: Контексты, страницы и эмуляция")
    void testAdvancedBrowserManagement() {
        // ========= 1. РАБОТА С КОНТЕКСТАМИ

        BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setLocale("ru-RU")
                .setPermissions(List.of("geolocation")));

        Page page1 = context1.newPage();

        // Изменяем стратегию ожидания загрузки
        page1.navigate("https://demoqa.com/login", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // Явное ожидание появления формы
        page1.waitForSelector("#userForm", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(15000));
        // Авторизация
        page1.fill("#userName", "testuser");
        page1.fill("#password", "Test123!");

        // Ожидание кликабельной кнопки
        page1.waitForSelector("#login", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED));
        page1.click("#login");

        // Ожидание завершения авторизации
        page1.waitForSelector("#userName-value", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

        assertTrue(page1.textContent("#userName-value").contains("testuser"),
                "Пользователь должен быть авторизован в контекст 1");

        // 2. РАБОТА СО СТРАНИЦАМИ
        Page page2 = context1.newPage();
        page2.navigate("https://demoqa.com/profile", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        page2.waitForSelector("#userName-value", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
        assertTrue(page2.isVisible("#userName-value"));


        // Ожидание выхода
//        page1.waitForSelector("#userForm", new Page.WaitForSelectorOptions()
//                .setState(WaitForSelectorState.VISIBLE));

        // 3.ИЗОЛИРОВАННЫЕ КОНТЕКСТЫ
        BrowserContext context2 = browser.newContext();
        Page page3 = context2.newPage();

        page3.navigate("https://demoqa.com/login", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page3.navigate("https://demoqa.com/login", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // Ждем появления формы логина
        page3.waitForSelector("#userForm", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
        assertTrue(page3.isVisible("#userForm"),
                "Форма логина должна быть видна в изолированном контексте");

        // 4. ЭМУЛЯЦИЯ УСТРОЙСТВ
        // Эмуляция iphone 12 pro - параметры вручную
        Browser.NewContextOptions iPhone12ProOptions = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (iPhone: CPU iPhone OS 14_0 like Mac OS X) " +
                        "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1")
                .setViewportSize(390, 844)
                .setDeviceScaleFactor(3)
                .setIsMobile(true)
                .setHasTouch(true);
        BrowserContext mobileContext = browser.newContext(iPhone12ProOptions);
        Page mobilePage = mobileContext.newPage();

        mobilePage.navigate("https://demoqa.com", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        ViewportSize size = mobilePage.viewportSize();
        assertEquals(390, size.width, "Ширина viewport должна соответствовать iPhone");
        assertEquals(844, size.height, "Высота viewport должна соответствовать iPhone");

        mobilePage.tap("text=Elements");
        mobilePage.waitForSelector(".element-list", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
        assertTrue(mobilePage.isVisible(".element-list"), "Мобильное меню должно открыться");

        // ========= 5. СОХРАНЕНИЕ СОСТОЯНИЯ =============
        context1.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("auth-state.json")));

        BrowserContext restoredContext =  browser.newContext(new Browser.NewContextOptions()
                .setStorageStatePath(Paths.get("auth-state.json")));

        // Закрываем контексты
        context1.close();
        context2.close();
        mobileContext.close();
        restoredContext.close();





    }


}
