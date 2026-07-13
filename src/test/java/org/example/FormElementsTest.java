package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormElementsTest {
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
    void testFormElements() {
        page.navigate("https://demoqa.com/automation-practice-form");
        // ========== Работа с радио-кнопками ==============
        page.locator("label[for='gender-radio-1']").click();
        boolean isMaleSelected = page.locator("#gender-radio-1").isChecked();
        assertTrue(isMaleSelected, "Радио-кнопка 'Male' должна быть выбрана");

        // ========= Работа с чекбоксами ========
        // Выбор двух чекбоксов
        page.locator("label[for='hobbies-checkbox-1']").click();
        page.locator("label[for='hobbies-checkbox-3']").click();

        // Проверка состояния
        boolean isSportsChecked = page.locator("label[for='hobbies-checkbox-1']").isChecked();
        boolean isMusicChecked = page.locator("label[for='hobbies-checkbox-3']").isChecked();
        assertTrue(isSportsChecked, "Чекбокс 'Sports' должен быть выбран");
        assertTrue(isMusicChecked, "Чекбокс 'Music' должен быть выбран");

        // Снятие выбора с одного чекбокса
        page.locator("label[for='hobbies-checkbox-3']").click();
        boolean isMusicUnchecked = !page.locator("label[for='hobbies-checkbox-3']").isChecked();
        assertTrue(isMusicUnchecked, "Чекбокс 'Music' должен быть снят");


        // ======== Работа с пыпадающим списком =========
        // Раскрытие списка
        page.locator("#state").click();

        // Выбор опции по значению
//        page.locator("#state").selectOption(new SelectOption().setValue("NCR"));
        page.locator("//div[@class='css-1nmdiq5-menu']//div[text()='NCR']").click();

        // Проверка выбора
        String selectedState = page.locator("#state div.css-1dimb5e-singleValue").innerText();
        assertEquals("NCR", selectedState, "Должен быть выбран штат NCR");

        // ========== Комбинированный пример ==========
        // Выбор опции по видимому тексту
        page.locator("#city").click();
//        page.locator("#city").selectOption(new SelectOption().setLabel("Delhi"));
        page.locator("//div[@class='css-1nmdiq5-menu']//div[text()='Delhi']").click();

        // Проверка комбинированного состояния
        String selectedCity = page.locator("#city div.css-1dimb5e-singleValue").innerText();
        assertEquals("Delhi", selectedCity, "Sports Должен быть выбран город Delhi");
        assertTrue(page.locator("#gender-radio-1").isChecked(), "Пол должен остаться выбранным");
        assertTrue(page.locator("#hobbies-checkbox-1").isChecked(), "Sports должен остаться выбранным");

    }
}
