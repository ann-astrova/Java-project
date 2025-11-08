package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import static org.example.View.warning;

public class Google_calendar {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential get_credentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = Google_calendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        //запрашивает все необходимые права доступа
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static String calendar2(String name, String place, String description, int interval, int count, String time_zone, String start_dt) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, get_credentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Парсим дату и время начала из строки ISO 8601 с учетом смещения
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(start_dt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            DateTime start = new DateTime(offsetDateTime.toInstant().toEpochMilli());

            // Время окончания события через 1 час
            DateTime end = new DateTime(start.getValue() + 3600000); // 1 час после начала

            // Создаем новое событие с повторением
            Event event = new Event()
                    .setSummary(name)
                    .setLocation(place) // Местоположение, если нужно
                    .setDescription(description)
                    .setStart(new EventDateTime().setDateTime(start).setTimeZone(time_zone)) // Устанавливаем время начала с часовым поясом
                    .setEnd(new EventDateTime().setDateTime(end).setTimeZone(time_zone))
                    .setRecurrence(Collections.singletonList("RRULE:FREQ=DAILY;INTERVAL=" + interval + ";COUNT=" + count)); // Повторение

            String calendarId = "primary"; // Основной календарь

            // Добавляем событие
            event = service.events().insert(calendarId, event).execute();
            System.out.println("Событие успешно создано с ID: " + event.getId());
            return String.format("Событие создано: %s\n", event.getHtmlLink());

        } catch (GeneralSecurityException e) {
            System.err.println("Ошибка безопасности: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }
        return "";
    }


    public static void delete_reminder(String name) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, get_credentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String calendarId = "primary"; // Основной календарь

            // Получаем список событий с указанным названием
            List<Event> events = service.events().list(calendarId)
                    .setQ(name) // Фильтруем по названию события
                    .execute()
                    .getItems();

            for (Event event : events) {
                // Удаляем каждое найденное событие
                service.events().delete(calendarId, event.getId()).execute();
                System.out.printf("Событие удалено: %s\n", event.getSummary());
            }

        } catch (GeneralSecurityException e) {
            System.err.println("Ошибка безопасности: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
        }
    }


    public static String convert_ISO8601(String date, String time, String time_zone) {
        // Разделяем строку даты на месяц и день
        String[] dateParts = date.split("\\.");
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[0]);

        // Получаем текущий год
        int year = LocalDateTime.now().getYear();

        // Создаем объект LocalDateTime
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, 0, 0);

        // Разделяем строку времени на часы и минуты
        String[] timeParts = time.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);

        // Устанавливаем часы и минуты
        dateTime = dateTime.withHour(hours).withMinute(minutes);

        // Преобразуем строку временной зоны в ZoneId
        ZoneId zoneId = get_zone(time_zone);

        // Преобразуем LocalDateTime в ZonedDateTime с учетом временной зоны
        System.out.println(dateTime.atZone(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return dateTime.atZone(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static ZoneId get_zone(String time_zone) {
        // Извлекаем смещение из строки временной зоны
        String offsetString = time_zone.substring(0, 10); // Например: "(GMT-11:00)"

        // Удаляем скобки и создаем ZoneId
        return ZoneId.ofOffset("GMT", ZoneOffset.of(offsetString.substring(4)));
    }

    // Функция для проверки времени в формате "часы:минуты"
    public static int check_time(String timeStr) {
        // Проверяем формат времени
        if (!timeStr.matches("\\d{1,2}:\\d{2}")) {
            warning ("Неверный формат времени\nПример: 10 часов 50 минут мая нужно записать как 10:50", 360, 150);
            return 1; // Неверный формат
        }

        try {
            // Пробуем распарсить время
            LocalTime time = LocalTime.parse(timeStr);
            return 0; // Время корректное
        } catch (DateTimeParseException e) {
            warning ("Время некорректное\nПопробуйте ещё раз", 360, 100);
            return 1; // Время некорректное
        }
    }

    // Функция для проверки даты в формате "месяц.число"
    public static int check_date(String dateStr) {
        // Проверяем формат даты
        if (!dateStr.matches("\\d{1,2}\\.\\d{1,2}")) {
            warning ("Неверный формат даты\nПример: 10 мая нужно записать как 10.05", 360, 140);
            return 1; // Неверный формат
        }

        String[] dateParts = dateStr.split("\\.");
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[0]);

        try {
            // Пробуем создать объект LocalDate с текущим годом
            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            return 0; // Дата корректная
        } catch (DateTimeParseException | IllegalArgumentException e) {
            warning ("Дата некорректная\n Если вы считаете, что такая дата есть,\nвозможно сейчас високосный год", 360, 200);
            return 1; // Дата некорректная
        }
    }


}
