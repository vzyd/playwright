package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiUiTest {
    // Фрагмент 1: Константы и переменные
    private static final String BASE_URL = "https://demoqa.com";
    private static final String LOGIN_URL = BASE_URL + "/Account/v1/Login";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "Test@123";
    private static final int TIMEOUT = 30000;
    private static final long COOKIE_EXPIRATION_DAYS = 1;

    static Playwright playwright;
    static Browser browser;
    static APIRequestContext apiRequestContext;
    static String authToken;
    static String userId;
    static String userName;

    BrowserContext context;

    Page page;

    // Фрагмент 2: Глобальная настройка @BeforeAll
    @BeforeAll
    static void globalSetup() {
        playwright = Playwright.create();
        authenticateUser();
        setupApiContext();
        launchBrowser();
    }

    // Фрагмент 3: Подготовка контекста @BeforeEach
    @BeforeEach
    void setupBrowserContext() {
        context = browser.newContext();
        addAuthCookiesToContext();
        page = context.newPage();

    }

    // Фрагмент 4: Структура тестового метода
    @Test
    void testProfileAfterApiLogin() {
        navigateToProfile(); // UI - действия
        verifyUserProfileData(); // Сравнение UI и API
    }

    // Фрагмент 5: Навигация в профиль (UI)
    private void navigateToProfile() {
        Allure.step("1. Переход в пофиль через UI", () -> {
            page.navigate(BASE_URL + "/profile",
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            waitForLoaderToDisappear(); // Ожидание лоадера
            waitForProfileWrapper(); // Ожидание контейнера

            Locator userNameLocator = page.locator("#userName-value");
            assertTrue(userNameLocator.isVisible(), "Элемент #userName-value не отображается");

            String actualUserName = userNameLocator.textContent();
            assertNotNull(actualUserName, "Имя пользователя не отображается");
            System.out.println("Отображаемое имя пользователя" + actualUserName);
        });
    }

    // Фрагмент 6: Верификация данных профиля
    private void verifyUserProfileData() {
        Allure.step("2. Проверка данных профиля", () -> {
            JsonObject apiUserData = fetchUserDataFromApi(); // API запрос
            String apiUsername = apiUserData.get("username").getAsString();

            String uiUsername = page.textContent("#userName-value"); // UI данные
            assertEquals(apiUsername, uiUsername, "Логин в UI не совпадает с API");
        });
    }

    @AfterEach
    void closeBrowserContext() {
        context.close();
    }

    @AfterAll
    static void globalCleanup() {
        closeResources();
    }

    // Фрагмент 7: Получение данных через API
    private JsonObject fetchUserDataFromApi() {
        APIResponse apiResponse = apiRequestContext.get("/Account/v1/User" + userId);
        assertEquals(200, apiResponse.status(), "API запрос не удался");
        return JsonParser.parseString(apiResponse.text()).getAsJsonObject();
    }

    // Фрагмент 8: Закрытие ресурсов
    private static void closeResources() {
        if(apiRequestContext != null) {
            apiRequestContext.dispose();
        }
        if(browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    // Фрагмент 9: АРІ - аутентификация
    private static void authenticateUser() {
        // Создаем временный контекст для аутентификации
        APIRequestContext tempContext = playwright.request().newContext();

        try {
            APIResponse authResponse = tempContext.post(
                    LOGIN_URL,
                    RequestOptions.create().setData(Map.of(
                            "userName", USERNAME,
                            "password", PASSWORD
                    ))
            );

            assertEquals(200, authResponse.status(), "Ошибка аутентификации");

            JsonObject responseJson = JsonParser.parseString(authResponse.text()).getAsJsonObject();
            authToken = responseJson.get("token").getAsString();
            userId = responseJson.get("userId").getAsString();
            userName = responseJson.get("username").getAsString();

            assertNotNull(authToken, "Токен авторизации не получен");
            assertNotNull(userId, "User ID не получен");
        } finally {
            // Гарантируем закрытие контекста даже при исключении
            tempContext.dispose();
        }
    }

    // Фрагмент 10: Настройка АПИ-контента
    private static void setupApiContext() {
        apiRequestContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(BASE_URL)
                .setExtraHTTPHeaders(Map.of(
                        "Authorization", "Bearer " + authToken
                )));
    }

    // Фрагмент 11:  Запуск браузера
    private static void launchBrowser() {
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setSlowMo(1000)
                );
    }

    // Фрагмент 12: Добавление аутентификационных кук
    private void addAuthCookiesToContext() {
        long expirationTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(COOKIE_EXPIRATION_DAYS);
        Date expirationDate = new Date(expirationTime);

        context.addCookies(Arrays.asList(
                createCookie("token", authToken, expirationDate),
                createCookie("userId", userId, expirationDate),
                createCookie("userName", userName, expirationDate),
                createCookie("expires", expirationDate.toString(), expirationDate)
        ));
    }

    // Фрагмент 13: Создание объекта кики
    private Cookie createCookie(String name, String value, Date expires) {
        return new Cookie(name, value)
                .setDomain("demoqa.com")
                .setPath("/")
                .setExpires(expires.getTime() / 1000);
    }

    // Фрагмент 14: Ожидание исчезновения лоадера
    private  void waitForLoaderToDisappear() {
        page.locator(".loader:has-text('Loading')")
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(TIMEOUT));
    }

    // Фрагмент 15: Ожидание появления контейнера
    private void waitForProfileWrapper() {
        page.locator(".profile-wrapper")
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(TIMEOUT));
    }
}
