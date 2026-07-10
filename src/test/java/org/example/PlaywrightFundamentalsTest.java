package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaywrightFundamentalsTest {

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
    void closeContext(){
        context.close();
    }

    @Test
    @DisplayName("Основы Playwright: Навигация, поиск элементов и взаимодействие")
    void testPlaywrightFundamentals() {
        // ?------- 1. Навигация и Ожидание -----
        page.navigate("https://demoqa.com");

        // Явное ожидание вместо Thread.sleep()
        page.waitForSelector(".card", new Page.WaitForSelectorOptions().setTimeout(10000));

        // ?------ 2. ПОИСК ЭЛЕМЕНТОВ ------
        // Стабильный CSS селектор
        Locator elementCard = page.locator("div.card:has-text('Elements')");
        elementCard.click();

        // Поиск по тексту
        page.locator("li.btn-light:has-text('Text Box')").click();

        // Поиск по роли ARIA (лучшая практика)
        Locator fullNameLabel = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions()
                .setName("Full Name"));

        // ?-------- 3. ВЗАИМОДЕЙСТВИЕ С ЭЛЕМЕНТАМИ -----------
        // Fill vs Type
        fullNameLabel.fill("Ivan Ivanov"); // Быстрое заполнение
        Locator emailInput = page.locator("#userEmail");
        emailInput.type("test@example.com"); // Посимвольный ввод

        Locator addressAria = page.locator("#currentAddress");
        addressAria.fill("Str. Good, Ap. 45");

        // Клик по кнопке
        Locator submitButton = page.locator("#submit");
        submitButton.click();

        // ?--------- 4. ПРОВЕРКИ И ПОЛУЧЕНИЕ ДАННЫХ
        // Ожидание появления результата
        page.waitForSelector("#output");

        // Проверка текста
        Locator nameResult = page.locator("#name");
        assertTrue(nameResult.textContent().contains("Ivan Ivanov"),
                "Неверное имя в результате");

        // Проверка атрибута
        Locator emailResult = page.locator("#email");
        assertEquals("test@example.com", emailResult.textContent()
                .replace("Email:", "").trim(),
                "Неверный имайл в результате");

        // ?------ 5. РАБОТА С ЧЕКБОКСАМИ и РАДИО КНОПКАМИ ------------

        // Radio-buttons
        page.locator("li:has-text('Radio Button')").click();
        Locator impressiveRadio = page.locator("label:has-text('Impressive')");
        impressiveRadio.check();

        assertTrue(impressiveRadio.isChecked(), "Radio button Impressive должен быть выбран");



        // Checkboxes
        page.locator("li:has-text('Check Box')").click();
//        Locator homeCheckBox = page.locator("span:has-text('Home') .rc-tree-checkbox");
        Locator homeCheckBox = page.locator("//span[@title='Home']/preceding-sibling::span[@role='checkbox']");
        homeCheckBox.check();

        assertTrue(homeCheckBox.isChecked(), "Чекбокс Home должен быть выбран");







    }
}
