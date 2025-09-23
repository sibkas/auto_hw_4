import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class CardOrderTest {

    // Универсальный метод для генерации даты
    private String generateDate(int days, String pattern) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(pattern));
    }

    @BeforeEach
    void setup() {
        System.out.println("Открываем страницу для теста...");
        open("http://localhost:9999");
    }

    @Test
    void shouldSubmitCardOrderWithDirectDate() {
        // Ввод города напрямую
        $("[data-test-id='city'] input").setValue("Красноярск");

        // Генерация даты через 3 дня
        String formattedDate = generateDate(3, "dd.MM.yyyy");

        // Очистка поля даты и ввод даты напрямую
        var dateInput = $("[data-test-id='date'] input");
        dateInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateInput.setValue(formattedDate);

        // Ввод имени и телефона
        $("[data-test-id='name'] input").setValue("Иван Иванов");
        $("[data-test-id='phone'] input").setValue("+79991112233");

        // Согласие (селектор по тегу input)
        $("[data-test-id='agreement'] .checkbox__box").click();

        // Отправка формы
        $("button.button_view_extra").click();

        // Проверка полного текста уведомления
        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.text("Встреча успешно забронирована"))
                .shouldHave(Condition.text(formattedDate));
    }

    @Test
    void shouldSubmitFormWithAutocompleteCityAndDate() {
        // Ввод первых букв города
        $("[data-test-id='city'] input").setValue("Кр");

        // Выбор из автокомплита
        $$(".menu-item").findBy(Condition.exactText("Красноярск")).click();

        // Генерация даты через 7 дней
        String formattedDate = generateDate(7, "dd.MM.yyyy");

        // Ввод даты напрямую
        var dateInput = $("[data-test-id='date'] input");
        dateInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        dateInput.setValue(formattedDate);

        // Ввод имени и телефона
        $("[data-test-id='name'] input").setValue("Иван Иванов");
        $("[data-test-id='phone'] input").setValue("+79991112233");

        // Согласие (селектор по тегу input)
        $("[data-test-id='agreement'] .checkbox__box").click();

        // Отправка формы
        $("button.button_view_extra").click();

        // Проверка полного текста уведомления
        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.text("Встреча успешно забронирована"))
                .shouldHave(Condition.text(formattedDate));
    }
}
