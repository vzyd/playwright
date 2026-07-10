package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoQATextBoxTestDataProvider {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    // DTO to save test data
    record FormData(String fullName, String email, String currentAddress, String permanentAddress) {}

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(2000));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage(){
        context = browser.newContext();
        page = context.newPage();
        page.navigate("https://demoqa.com/text-box", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.locator("#userName").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    static Stream<Arguments> formDataProvider() {
        return Stream.of(
               Arguments.of(new FormData("Ivan Ivanov", "ivan@test.com", "st. Golden 5", "st. Mainstr 10")),
               Arguments.of(new FormData("Anna Smith", "anna@test.com", "Baker Street 221B", "Central street 15"))
        );
    }

    @ParameterizedTest(name = "Form test: {0}")
    @MethodSource("formDataProvider")
    void testTextBoxForm(FormData data) {
        // Ожидание готовности формы перед заполнением
        page.waitForSelector("#userName", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE));

        // Заполнение формы
//        page.locator("#userName").fill(data.fullName());
//        page.locator("#userEmail").fill(data.email());
//        page.locator("#currentAddress").fill(data.currentAddress());
//        page.locator("#permanentAddress").fill(data.permanentAddress());

        // Клик с обработкой возможного лоадера
//        Locator  submitButton = page.locator("#submit");
//        submitButton.click();

        // Явное ожидание результатов вместо мгновенной проверки
//        Locator output = page.locator("#output");
//        output.waitFor(new Locator.WaitForOptions()
//                .setState(WaitForSelectorState.VISIBLE)
//                .setTimeout(5000));

        fillField("#userName", data.fullName());
        fillField("#userEmail", data.email());
        fillField("#currentAddress", data.currentAddress());
        fillField("#permanentAddress", data.permanentAddress());

        clickWithRetry("#submit", 3);
        waitForOutput();



        // Проверки с учетом форматирования вывода
//        String resultText = output.innerText();
//        assertTrue(resultText.contains(data.fullName()), "Не найдено полное имя");
//        assertTrue(resultText.contains(data.email()), "Не найден имайл");
//        assertTrue(resultText.contains(data.currentAddress()), "Не найден текущий адрес");
//        assertTrue(resultText.contains(data.permanentAddress()), "Не найден постоянный адрес");
    }

    private void fillField(String selector, String value) {
        Locator field = page.locator(selector);
        field.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        field.fill(value);
    }

    private void clickWithRetry(String selector, int attempts) {
        Locator button = page.locator(selector);
        for (int i = 0; i < attempts; i++) {
            try {
                button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                button.click();

                page.waitForTimeout(300);
                return;
            } catch (TimeoutError e) {
                if (i == attempts - 1) throw e;
                page.reload();
                page.waitForSelector("#userName", new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE));
            }
        }
    }

    private void waitForOutput() {
        try {
            page.waitForSelector("#output", new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(8000));
        } catch (TimeoutError e){
            Locator output = page.locator("#output");
            output.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(5000));

            page.waitForFunction("document.querySelector(#output).style.display !== 'none'");
        }
    }

}
