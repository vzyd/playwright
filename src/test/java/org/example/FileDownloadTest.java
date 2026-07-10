package org.example;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileDownloadTest {
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
//        browser.close();
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage(){
        context = browser.newContext(new Browser.NewContextOptions()
                .setAcceptDownloads(true));
        page = context.newPage();
        page.navigate("https://demoqa.com/upload-download");
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    @DisplayName("Тестирование загрузки файлов")
    void testFileDownload() throws IOException {
        // ========== 1. Обработка события загрузки =========
        // Ожидаем событие Download
        Download download = page.waitForDownload(() -> {
            page.click("#downloadButton");
        });


        // ========== 2. Сохранение файла ========
        // Создаем временный путь
//        Path temDir = Files.createTempDirectory("playwright-downloads");
//        Path filePath = temDir.resolve(download.suggestedFilename());

        Path dir = Path.of("src/test/java/org/example/downloads");
        Path filePath = dir.resolve(download.suggestedFilename());

        // Сохраняем файл
        download.saveAs(filePath);

        System.out.println("Файл сохранен: " + filePath);

        // ========== 3. Проверка файла ========
        // Проверяем существование
        assertTrue(Files.exists(filePath), "Файл должен существовать");

        // Проверяем размер
        long fileSize = Files.size(filePath);
        assertTrue(fileSize > 0, "Файл не должен быть пустым");

        // Проверяем расширение
        assertTrue(filePath.toString().endsWith(".jpeg"),
                "Файл должен быть в формате JPEG");

        // =========== 4. Проверка содержимого ==========
        String mimeType = Files.probeContentType(filePath);
        assertEquals("image/jpeg", mimeType, "MIME-тип должен быть image/jpeg");

        // ========== 5. Дополнительная проверка ============
        byte[]  fileContent = Files.readAllBytes(filePath);
        assertTrue(fileContent.length > 1000, "Размер файла должен быть больше 1КВ");


        // Удаляем временные файлы (в реальных тестах не нужно)
//        Files.deleteIfExists(filePath);
    }
}
