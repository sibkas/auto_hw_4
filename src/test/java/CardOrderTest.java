import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.*;

public class CardOrderTest {

    @BeforeEach
    void setup() {
        Configuration.headless = true;
        open("http://localhost:9999");
        $("[data-test-id='city'] input").shouldBe(Condition.visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldSubmitCardOrderWithCalendarSelection() {
        // Ввод города напрямую
        $("[data-test-id='city'] input").setValue("Красноярск");

        // Получаем дату через 3 дня
        LocalDate targetDate = LocalDate.now().plusDays(3);
        String formattedDate = targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // Очистка поля даты с помощью выделения и удаления текста
        var dateInput = $("[data-test-id='date'] input");
        dateInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);

        // Вводим дату напрямую
        dateInput.setValue(formattedDate);

        // Ввод имени
        $("[data-test-id='name'] input").setValue("Иван Иванов");

        // Ввод телефона
        $("[data-test-id='phone'] input").setValue("+79991112233");

        // Выбор чекбокса согласия
        $("[data-test-id='agreement'] .checkbox__box").click();

        // Отправка формы
        $("button.button_view_extra").click();

        // Проверка уведомления с таймаутом в 15 секунд
        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(15))
                .shouldHave(Condition.text("Успешно!"))
                .shouldHave(Condition.text(targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    @Test
    void shouldSubmitFormWithAutocompleteCityAndCalendar() {
        // Вводим первые две буквы города
        $("[data-test-id='city'] input").setValue("Кр");

        // Ждем появления выпадающего списка, выбираем "Красноярск"
        $$(".menu-item").findBy(Condition.exactText("Красноярск")).click();

        // Открываем календарь
        $("[data-test-id='date'] .input__icon button").click();
        $(".calendar__layout").shouldBe(Condition.visible);

        // Выбираем дату через неделю
        LocalDate targetDate = LocalDate.now().plusWeeks(1);
        String day = String.valueOf(targetDate.getDayOfMonth());

        // Навигация по месяцам при необходимости
        // (чтобы месяц был другим от изначального)
        while (!$(".calendar__name").text().equalsIgnoreCase(targetDate.format(DateTimeFormatter.ofPattern("LLLL yyyy")))) {
            $(".calendar__arrow_direction_right").click();
        }

        // Кликаем по дню
        $$(".calendar__day").findBy(Condition.text(day)).click();

        // Вводим имя
        $("[data-test-id='name'] input").setValue("Иван Иванов");
        // Вводим телефон
        $("[data-test-id='phone'] input").setValue("+79991112233");

        // Выбираем чекбокс соглашения
        $("[data-test-id='agreement'] .checkbox__box").click();

        // Отправляем форму
        $("button.button_view_extra").click();

        // Проверяем появление уведомления
        $("[data-test-id='notification']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(15))
                .shouldHave(Condition.text("Успешно!"))
                .shouldHave(Condition.text(targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }
}
