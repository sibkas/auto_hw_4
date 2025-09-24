import com.codeborne.selenide.Condition;

import com.codeborne.selenide.Selenide;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.*;

public class CardOrderTest {

    // Универсальный метод для генерации даты
    private String generateDate(int days, String pattern) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(pattern));
    }

    @BeforeEach
    void setup() {

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
        String expectedTextPart = "Встреча успешно забронирована на " + formattedDate;

        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.text(expectedTextPart));
    }

    @Test
    void shouldSubmitFormWithAutocompleteCityAndDate() {
        // Ввод первых букв города
        $("[data-test-id='city'] input").setValue("Кр");

        // Выбор из автокомплита
        $$(".menu-item").findBy(Condition.exactText("Красноярск")).click();

        // Получаем дату через 7 дней
        LocalDate targetDate = LocalDate.now().plusDays(7);
        String expectedMonth = targetDate.format(DateTimeFormatter.ofPattern("LLLL", new Locale("ru"))).toLowerCase();
        String expectedYear = String.valueOf(targetDate.getYear());
        String formattedDate = targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // Открываем календарь
        $("[data-test-id='date'] .input__icon button").click();
        $(".calendar__layout").shouldBe(Condition.visible);

        // Навигация к нужному месяцу, с ограничением по количеству прыжков (максимум 12)
        int maxScrolls = 12;
        int scrolls = 0;
        while (scrolls < maxScrolls) {
            $(".calendar__arrow.calendar__arrow_direction_right:not(.calendar__arrow_double)").click();

            Selenide.sleep(500);
            String monthYearText = $(".calendar__name").text();
            String[] parts = monthYearText.split(" ");
            String monthText = parts[0].toLowerCase();
            String yearText = parts[1];


            if (monthText.equals(expectedMonth) && yearText.equals(expectedYear)) {
                break;
            }
            scrolls++;
        }
        if (scrolls == maxScrolls) {
            throw new RuntimeException("Не удалось найти нужный месяц и год в календаре");
        }

        // Кликаем по нужному дню
        String day = String.valueOf(targetDate.getDayOfMonth());
        $$(".calendar__day").findBy(Condition.text(day)).click();

        // Проверяем, что дата в поле обновилась
        $("[data-test-id='date'] input").shouldHave(Condition.value(formattedDate));


        // Заполняем имя, телефон и соглашаемся с условиями
            $("[data-test-id='name'] input").setValue("Иван Иванов");
            $("[data-test-id='phone'] input").setValue("+79991112233");
            $("[data-test-id='agreement'] .checkbox__box").click();

            // Отправляем форму
            $("button.button_view_extra").click();

            // Проверяем появление уведомления с датой
            String expectedTextPart = "Встреча успешно забронирована на " + formattedDate;
            $("[data-test-id='notification']")
                    .shouldBe(Condition.visible, Duration.ofSeconds(15))
                    .shouldHave(Condition.text(expectedTextPart));
        }


    }


