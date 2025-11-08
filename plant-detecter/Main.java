package org.example;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.*;
//import javax.swing.text.html.ImageView;
import java.awt.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.example.Database.*;
import static org.example.Google_calendar.*;
import static org.example.Time_zones.time_zones;
import static org.example.View.message;
import static org.example.View.warning;

public class Main extends Application{
    private TextField input; //для ввода
    private Firestore db;
    private ArrayList<Plant> plants;
    private Plant the_plant;
    private String the_user;
    private String the_login;

    public static void main(String[] args) {

        launch(args);
    }

    String getFileExtension(String filename) //работа с путём к файлу
    {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            System.out.println(filename.substring(dotIndex + 1));
            return filename.substring(dotIndex + 1);
        }
        return "";
    }


    private void picture (Pane root) throws Exception {

        // Создаем JFrame
        JFrame frame = new JFrame("File Chooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        // Создаем JFileChooser
        JFileChooser fileChooser = new JFileChooser();

        // Открываем диалог выбора файла
        int returnValue = fileChooser.showOpenDialog(null);

        // Проверяем, был ли выбран файл
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(frame, "Выбранный файл: " + selectedFile.getAbsolutePath());
            File picture = new File(selectedFile.getAbsolutePath());
            if (!picture.exists()) //если такого пути нет вообще
            {
                View.text_output ("", 80, 220);
                View.warning("Введенный адрес неправильный.\nПопробуйте еще раз", 300, 130);
            }
            else {
                String ext = getFileExtension(selectedFile.getAbsolutePath());
                if (Objects.equals(ext, "PNG") || Objects.equals(ext, "JPG") || Objects.equals(ext, "png") || Objects.equals(ext, "jpg")){


                    VisionApiExample vae = new VisionApiExample();
                    String inform = vae.detectLabels(selectedFile.getAbsolutePath());

                    if (inform.indexOf("Flower") >= 0 || inform.indexOf("Shrub") >= 0 || inform.indexOf("Fruit tree") >= 0 || inform.indexOf("Tree") >= 0 || inform.indexOf("Conifers") >= 0){
                        View.remove_warnings(root);
                        message("Изображение добавлено", 305, 80);
                        the_plant.set_pic(picture);
                        the_plant.set_info(inform);

                        ad_picture(the_plant.get_name(), selectedFile.getAbsolutePath(), inform, db, the_user);


                    }
                    else if (inform.equals("No")){
                        View.remove_warnings(root);
                        View.warning ("Информация о растении: Не найдена\nПопробуйте загрузить другую картинку", 300, 100);
                    }
                    else{
                        View.remove_warnings(root);
                        View.warning ("На вашей картинке нет растения\nПопробуйте загрузить другую", 310, 140);
                    }
                }
                else
                {
                    View.text_output ("", 80, 220);
                    View.warning("Неправильный формат файла, попробуйте ввести другой", 400, 80);
                }
            }
        }
        else
        {
            message("Выбор файла отменен", 300, 80);
        }
    }


    private int check_new_val(String user_input, String warning_message, int w_width, int w_height)
    {
        try {// Проверяем, является ли ввод положительным числом
            if (!user_input.matches("^(0|[1-9]\\d*)(\\.\\d+)?$"))
            {
                View.warning(warning_message, w_width, w_height);
                return 1;
            }
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            View.warning(warning_message, w_width, w_height);
            return 1;
        }
        return 0;
    }

    private void change_inf (Pane root, Button back, Stage stage){
        View.text_output("", 100, 100);

        // Поля для ввода
        TextField height_f = new TextField(String.valueOf(the_plant.get_height()));
        height_f.setLayoutX(250);
        height_f.setLayoutY(200);

        TextField volume_f = new TextField(String.valueOf(the_plant.get_vol()));
        volume_f.setLayoutX(250);
        volume_f.setLayoutY(250);


        TextField temperature_f = new TextField(String.valueOf(the_plant.get_temperature()));
        temperature_f.setLayoutX(250);
        temperature_f.setLayoutY(300);

        // Метки для полей ввода
        Label height_l = new Label("Высота цветка(см):");
        height_l.setLayoutX(80);
        height_l.setLayoutY(200);
        height_l.setFont(new Font("Arial", 18)); // Установка шрифта и размера

        Label volume_l = new Label("Объём горшка(л):");
        volume_l.setLayoutX(80);
        volume_l.setLayoutY(250);
        volume_l.setFont(new Font("Arial", 18)); // Установка шрифта и размера

        Label temperature_l = new Label("Температура(°C):");
        temperature_l.setLayoutX(80);
        temperature_l.setLayoutY(300);
        temperature_l.setFont(new Font("Arial", 18)); // Установка шрифта и размера

        // Кнопка для добавления фото
        Button photo = new Button("Добавить фото растения");
        photo.setLayoutX(250);
        photo.setLayoutY(350);
        photo.setOnAction(s_e -> {
            try {
                picture(root); // Вызов функции для загрузки фото
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Кнопка сохранения изменений
        Button save_b = new Button("Сохранить");
        save_b.setTranslateX(510);
        save_b.setTranslateY(490);

        save_b.setOnAction(to_the_inf_e -> {

            int check = 0;
            check+=check_new_val(height_f.getText(), "Необходимо ввести цифрами высоту растения\nЭто число не может быть отрицательным\nПопробуйте ещё раз", 360, 160);
            check+=check_new_val(volume_f.getText(), "Необходимо ввести цифрами объём цветочного горшка\nЭто число не может быть отрицательным\nПопробуйте ещё раз", 370, 160);
            check+=check_new_val(temperature_f.getText(), "Необходимо ввести цифрами температуру в помещении\nПри отрицательной температуре растение погибнет\nПопробуйте ещё раз", 390, 160);

            if(check == 0) {
                the_plant.set_height(Double.parseDouble(height_f.getText()));
                the_plant.set_vol(Double.parseDouble(volume_f.getText()));
                the_plant.set_temperature(Double.parseDouble(temperature_f.getText()));


                //Запись изменений в Firestore

                Map<String, Object> updates = new HashMap<>();
                updates.put("height", Double.parseDouble(height_f.getText()));
                updates.put("vol", Double.parseDouble(volume_f.getText()));
                updates.put("room_temperature", Double.parseDouble(temperature_f.getText()));

                ApiFuture<WriteResult> future = db.collection(the_user).document(the_plant.get_name()).update(updates);

                try {
                    WriteResult result = future.get(); // ждем подтверждения записи
                    System.out.println("Данные успешно обновлены в: " + result.getUpdateTime());
                    root.getChildren().removeAll(height_l, height_f,
                            volume_l, volume_f,
                            temperature_l, temperature_f,
                            photo, save_b);
                    the_fl_data(root, back, stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    View.warning("Ошибка при сохранении данных. Попробуйте позже.", 310, 80);
                    return; // прерываем дальнейшее выполнение, чтобы не скрыть ошибки
                }

            }
        });

        // Добавляем все элементы на панель
        root.getChildren().addAll(
                height_l, height_f,
                volume_l, volume_f,
                temperature_l, temperature_f,
                photo, save_b);

        back.setOnAction(back_to_the_inf_event -> {
            root.getChildren().removeAll(height_l, height_f,
                    volume_l, volume_f,
                    temperature_l, temperature_f,
                    photo, save_b);
            try {
                the_fl_data(root, back, stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void new_mentions(Pane root, Button back, Stage stage) {
        // Поля для ввода
        TextField day_f = new TextField();
        day_f.setLayoutX(330);
        day_f.setLayoutY(40);

        TextField time_f = new TextField();
        time_f.setLayoutX(330);
        time_f.setLayoutY(80);

        TextField title_f = new TextField();
        title_f.setLayoutX(330);
        title_f.setLayoutY(120);

        TextField place_f = new TextField();
        place_f.setLayoutX(330);
        place_f.setLayoutY(160);

        TextField details_f = new TextField();
        details_f.setLayoutX(330);
        details_f.setLayoutY(200);

        TextField interval_f = new TextField();
        interval_f.setLayoutX(330);
        interval_f.setLayoutY(240);

        TextField number_rep_f = new TextField();
        number_rep_f.setLayoutX(330);
        number_rep_f.setLayoutY(280);

        // Метки для полей ввода

        Label day_l = new Label("Дата начала(число.месяц):");
        day_l.setLayoutX(100);
        day_l.setLayoutY(40);
        day_l.setFont(new Font("Arial", 18));

        Label time_l = new Label("Время начала:");
        time_l.setLayoutX(100);
        time_l.setLayoutY(80);
        time_l.setFont(new Font("Arial", 18));

        Label title_l = new Label("Название события:");
        title_l.setLayoutX(100);
        title_l.setLayoutY(120);
        title_l.setFont(new Font("Arial", 18));

        Label place_l = new Label("Место проведения:");
        place_l.setLayoutX(100);
        place_l.setLayoutY(160);
        place_l.setFont(new Font("Arial", 18));

        Label details_l = new Label("Детали события:");
        details_l.setLayoutX(100);
        details_l.setLayoutY(200);
        details_l.setFont(new Font("Arial", 18));

        Label interval_l = new Label("Интервал:");
        interval_l.setLayoutX(100);
        interval_l.setLayoutY(240);
        interval_l.setFont(new Font("Arial", 18));

        Label number_rep_l = new Label("Количество повторений:");
        number_rep_l.setLayoutX(100);
        number_rep_l.setLayoutY(280);
        number_rep_l.setFont(new Font("Arial", 18));

        // Создаем текстовое поле для ввода
        TextField zone_input = new TextField(); // Устанавливаем ширину текстового поля
        zone_input.setPrefWidth(378); // Устанавливаем ширину текстового поля
        zone_input.setLayoutX(100); // Устанавливаем позицию по X
        zone_input.setLayoutY(320); // Устанавливаем позицию по Y

        // Создаем ListView для отображения результатов поиска
        ListView<String> time_list = new ListView<>();
        time_list.setPrefHeight(100); // Устанавливаем высоту ListView
        time_list.setPrefWidth(zone_input.getPrefWidth()); // Устанавливаем ширину ListView
        time_list.setLayoutX(zone_input.getLayoutX()); // Устанавливаем позицию по X
        time_list.setLayoutY(zone_input.getLayoutY() + 30); // Устанавливаем позицию по Y (под текстовым полем)

        // Обработчик изменения текста в текстовом поле для фильтрации списка
        zone_input.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> filteredList = FXCollections.observableArrayList();
            for (String zone : time_zones) {
                if (zone.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredList.add(zone); // Добавляем совпадения в новый список
                }
            }
            time_list.setItems(filteredList); // Обновляем time_list с отфильтрованными данными

            // Убираем очистку списка здесь
        });

        // Обработчик выбора элемента из time_list
        time_list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                zone_input.setText(newValue); // Обновляем текст в текстовом поле при выборе варианта
                // Вместо очистки списка можно просто скрыть его или оставить заполненным
                // time_list.getItems().clear(); // Очищаем список после выбора
            }
        });

        Button save_b = new Button("Сохранить");
        save_b.setTranslateX(510);
        save_b.setTranslateY(490);
        root.getChildren().add(save_b);

        save_b.setOnAction(e -> {
            int check = 0;
            String t_zone = zone_input.getText();

            check+=check_new_val(interval_f.getText(), "Интервал - количество дней между напоминаниями\nЭто число не может быть отрицательным или дробным\nПопробуйте ещё раз", 360, 200);
            check+=check_new_val(number_rep_f.getText(), "Количество дней не должно быть отрицательным или дробным\nПопробуйте ещё раз", 400, 150);
            check+=check_time(time_f.getText());
            check+=check_date(day_f.getText());


            if(check == 0) {
                if (t_zone.isEmpty()){
                    if(View.warning ("Если вы не выберете часовой пояс, будет установлено время мск\nЕсли согласны - нажмите 'ок'\nЕсли нет - крестик в правом верхнем углу",
                            400, 160)==1){
                        t_zone = "Europe/Moscow";
                        String title = title_f.getText() + " " + the_plant.get_name();
                        String start_dt = convert_ISO8601(day_f.getText(), time_f.getText(), t_zone);

                        calendar2(title, place_f.getText() + "", details_f.getText() + "", Integer.parseInt(interval_f.getText()),
                                Integer.parseInt(number_rep_f.getText()), t_zone, start_dt);

                        /*calendar(title, place_f.getText() + "", details_f.getText() + "", Integer.parseInt(interval_f.getText()),
                                Integer.parseInt(number_rep_f.getText()), t_zone);*/


                        root.getChildren().removeAll(day_f, day_l, time_f, time_l, title_l, title_f,
                                place_l, place_f,
                                details_l, details_f,
                                interval_l, interval_f,
                                number_rep_l, number_rep_f,
                                zone_input, time_list, save_b);
                        //удалить просмотр и изменение
                        mentions(root, back, stage);
                    }
                }
                else{
                    String title = title_f.getText() + " " + the_plant.get_name();
                    String start_dt = convert_ISO8601(day_f.getText(), time_f.getText(), t_zone);

                    calendar2(title, place_f.getText() + "", details_f.getText() + "", Integer.parseInt(interval_f.getText()),
                            Integer.parseInt(number_rep_f.getText()), t_zone, start_dt);

                    /*calendar(title, place_f.getText() + "", details_f.getText() + "", Integer.parseInt(interval_f.getText())+1,
                            Integer.parseInt(number_rep_f.getText()), t_zone);*/


                    root.getChildren().removeAll(day_f, day_l, time_f, time_l, title_l, title_f,
                            place_l, place_f,
                            details_l, details_f,
                            interval_l, interval_f,
                            number_rep_l, number_rep_f,
                            zone_input, time_list, save_b);
                    //удалить просмотр и изменение
                    mentions(root, back, stage);
                }
            }


        });

        back.setOnAction(event -> {
            root.getChildren().removeAll(day_f, day_l, time_f, time_l, title_l, title_f,
                    place_l, place_f,
                    details_l, details_f,
                    interval_l, interval_f,
                    number_rep_l, number_rep_f,
                    zone_input, time_list, save_b);
            mentions( root, back, stage);
        });

        // Добавляем все элементы на панель
        root.getChildren().addAll(day_f, day_l, time_f, time_l,
                title_l, title_f,
                place_l, place_f,
                details_l, details_f,
                interval_l, interval_f,
                number_rep_l, number_rep_f,
                zone_input, time_list); // Добавляем текстовое поле и ListView
    }

    private void mentions(Pane root, Button back, Stage stage)
    {
        Button new_m_b = new Button("Создать напоминание");
        new_m_b.setTranslateX(255);
        new_m_b.setTranslateY(150);
        root.getChildren().add(new_m_b);

        new_m_b.setOnAction(e -> {
            root.getChildren().remove(new_m_b);
            //удалить просмотр и изменение
            new_mentions(root, back, stage);


        });
        back.setOnAction(event -> {
            root.getChildren().remove(new_m_b);
            try {
                the_fl_data( root, back, stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void the_fl_data(Pane root, Button back, Stage stage) throws IOException {

        View.text_output("             " + the_plant.get_name() +
                "\n\nВысота цветка = " + the_plant.get_height() + " см" +
                "\n\nОбъём горшка = " + the_plant.get_vol() + " л" +
                "\n\nТемпература в комнате = " + the_plant.get_temperature() + " °C" +
                "\n\nИнформация: " + the_plant.get_info(), 70, 50);


        // Попробуем загрузить изображение
        File plantImageFile = the_plant.get_pic(); // Предполагается, что метод get_pic() возвращает File
        ImageView imageView;
        if (plantImageFile != null && plantImageFile.exists()) {
            Image plantImage = new Image(plantImageFile.toURI().toString());
            imageView = new ImageView(plantImage);
            imageView.setFitWidth(200); // Установите нужную ширину
            imageView.setFitHeight(240); // Установите нужную высоту
            imageView.setTranslateX(400); // Позиция по оси X
            imageView.setTranslateY(20); // Позиция по оси Y
            root.getChildren().add(imageView); // Добавляем изображение в корень
        } else {
            imageView = null;
        }

        Button change_b = new Button("Изменить информацию");
        change_b.setTranslateX(450);
        change_b.setTranslateY(490);
        root.getChildren().addAll(change_b);

        Button mentions_b = new Button("Напоминания");
        mentions_b.setTranslateX(480);
        mentions_b.setTranslateY(310);
        root.getChildren().addAll(mentions_b);

        Button delete_b = new Button("Удалить растение");
        delete_b.setTranslateX(230);
        delete_b.setTranslateY(490);
        root.getChildren().addAll(delete_b);

        change_b.setOnAction(e -> {
            if (imageView != null) {
                root.getChildren().remove(imageView);
            }
            root.getChildren().removeAll(change_b, mentions_b, delete_b);
            change_inf (root, back, stage);
        });

        mentions_b.setOnAction(e -> {
            if (imageView != null) {
                root.getChildren().remove(imageView);
            }
            root.getChildren().removeAll(change_b, mentions_b, delete_b);
            View.text_output("", 70, 100);
            mentions( root, back, stage);
        });

        back.setOnAction(event -> {
            if (imageView != null) {
                root.getChildren().remove(imageView);
            }
            root.getChildren().removeAll(change_b, mentions_b, delete_b);
            plant_list(root, stage);
        });

        delete_b.setOnAction(e -> {
            if (imageView != null) {
                root.getChildren().remove(imageView);
            }
            root.getChildren().removeAll(change_b, mentions_b, delete_b);
            delete_plant( root, stage, back);
        });
    }

    public void delete_plant(Pane root, Stage stage, Button back)
    {
        View.text_output("Напишите имя растения, которое хотите УДАЛИТЬ.\n         Имя выбранного растения " + the_plant.get_name(), 100, 263);

        input = new TextField();
        input.setLayoutX(247);
        input.setLayoutY(350);

        Button delete = new Button("Удалить");
        delete.setTranslateX(475);
        delete.setTranslateY(490);
        delete.setPrefSize(100, 30);

        root.getChildren().addAll(input, delete);

        back.setOnAction(event -> {
            root.getChildren().removeAll(delete, input);
            try {
                the_fl_data( root, back, stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


        delete.setOnAction(event -> {
            String plant_name = input.getText();
            if (Objects.equals(plant_name, the_plant.get_name())) {
                if(delete_plant_db(db, the_user, plant_name)){
                    root.getChildren().removeAll(delete, input);
                    plant_list( root, stage);
                }
            } else {
                warning("Если хотите удалить растение,\nвведите его имя", 310, 160);
            }
        });

    }

    private void add_plant(Pane root, Stage stage, Button back) {

        View.text_output("Придумайте имя для растения:", 172, 263);

        input = new TextField();
        input.setLayoutX(247);
        input.setLayoutY(350);

        Button next = new Button("Продолжить");
        next.setTranslateX(475);
        next.setTranslateY(490);
        next.setPrefSize(100, 30);

        root.getChildren().addAll(input, next);

        back.setOnAction(event -> {
            root.getChildren().removeAll(next, input);
            plant_list(root, stage);
        });

        next.setOnAction(event -> {
            String flower_name = input.getText();
            if (!flower_name.isEmpty()) {
                the_plant = new Plant(flower_name); // Создаем объект Plant с именем

                // Формируем Map для Firestore:
                Map<String, Object> docData = new HashMap<>();
                docData.put("vol", 0.0);                       // по умолчанию 0
                docData.put("room_temperature", 0.0);          // по умолчанию 0
                docData.put("height", 0.0);                     // по умолчанию 0
                docData.put("pic", "");                         // пустая строка для ссылки
                docData.put("inf", "");                         // пустая информация
                docData.put("name", flower_name);               // имя цветка
                docData.put("calendar", new ArrayList<String>()); // пустой календарь

                // Записываем в Firestore:
                Firestore db = FirestoreClient.getFirestore();

                // В коллекцию user создаем документ с именем flower_name
                ApiFuture<WriteResult> future = db.collection(the_user).document(flower_name).set(docData);

                try {
                    WriteResult result = future.get(); // ждем завершения записи

                    root.getChildren().removeAll(next, input);
                    View.text_output("", 172, 263);
                    the_fl_data(root, back, stage);

                } catch (Exception e) {
                    warning("Ошибка при сохранении растения", 360, 80);
                }
            } else {
                warning("Имя растения не должно быть пустым", 360, 80);
            }
        });
    }

    public void plant_list(Pane root, Stage stage){
        View.text_output ("", 200, 320);

        Button delete = new Button("Удалить аккаунт");
        delete.setTranslateX(450);
        delete.setTranslateY(490);
        delete.setPrefSize(135, 30);
        root.getChildren().add(delete);

        //делает кнопку "+ растения"
        Button new_fl = new Button();
        new_fl.setText("Добавить растение");
        new_fl.setTranslateX(235);
        new_fl.setTranslateY(80);
        new_fl.setPrefSize(150, 30);
        root.getChildren().add(new_fl);

        Button back = new Button("Назад");
        back.setTranslateX(20);
        back.setTranslateY(490);
        back.setPrefSize(50, 30);
        root.getChildren().add(back);

        int y = 130; //50

        plants = inform(db, the_user);

        for (Plant plant : plants) {
            Button button = new Button(plant.get_name());
            button.setTranslateX(235);
            button.setTranslateY(y);
            button.setPrefSize(150, 30);

            // Помечаем кнопку, что она связана с растением
            button.setUserData("plant_button");

            button.setOnAction(e -> {
                // Удаляем все кнопки с пометкой "plant_button"
                root.getChildren().removeIf(node -> {
                    if (node instanceof Button) {
                        Object ud = node.getUserData();
                        return "plant_button".equals(ud);
                    }
                    return false;
                });
                root.getChildren().removeAll(new_fl, delete);
                the_plant = plant;
                try {
                    the_fl_data(root, back, stage);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            root.getChildren().add(button);
            y += 50;
        }


        new_fl.setOnAction(event -> {
            // Удаляем все кнопки растений
            root.getChildren().removeIf(node -> {
                if (node instanceof Button) {
                    Object ud = node.getUserData();
                    return "plant_button".equals(ud);
                }
                return false;
            });
            root.getChildren().removeAll(new_fl, delete);
            // вызываем вашу функцию добавления
            add_plant(root, stage, back);
        });


        back.setOnAction(event -> {
            // Удаляем все кнопки растений
            root.getChildren().removeIf(node -> {
                if (node instanceof Button) {
                    Object ud = node.getUserData();
                    return "plant_button".equals(ud);
                }
                return false;
            });
            root.getChildren().removeAll(new_fl, delete);
            stop_firebase();
            start(stage);
        });

        delete.setOnAction(event -> {
            // Удаляем все кнопки растений
            root.getChildren().removeIf(node -> {
                if (node instanceof Button) {
                    Object ud = node.getUserData();
                    return "plant_button".equals(ud);
                }
                return false;
            });
            root.getChildren().remove(new_fl);
            delete_acc(root, delete, stage, back);
        });

    }

    public void delete_acc(Pane root, Button delete, Stage stage, Button back){

        View.text_output("Удаление аккаунта", 220, 100);

        TextField login_f = new TextField();
        login_f.setLayoutX(330);
        login_f.setLayoutY(200);

        TextField password_f = new TextField();
        password_f.setLayoutX(330);
        password_f.setLayoutY(240);

        // Метки для полей ввода

        Label login_l = new Label("Имя пользователя:");
        login_l.setLayoutX(120);
        login_l.setLayoutY(200);
        login_l.setFont(new Font("Arial", 18));

        Label password_l = new Label("Пароль:\n");
        password_l.setLayoutX(120);
        password_l.setLayoutY(240);
        password_l.setFont(new Font("Arial", 18));

        root.getChildren().addAll(password_l, password_f, login_l, login_f);

        back.setOnAction(event -> {
            root.getChildren().removeAll(delete, password_l, password_f, login_l, login_f, back);
            plant_list(root, stage);
        });

        delete.setOnAction(event -> {
            String password = password_f.getText();
            String login = login_f.getText();
            String collection_name = login + " " + password;
            if(collection_name.equals(the_user)){
                try {
                    delete_user( db, the_user, 20);
                    message("Пользователь '" + login + "' удалён", 360, 150);
                    root.getChildren().removeAll(delete, password_l, password_f, login_l, login_f, back);
                    stop_firebase();
                    start(stage);
                } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    //throw new RuntimeException(e);
                    warning("Ошибка при удалении пользователя", 360, 150);
                }
            }
            else{
                warning("Для удаления аккаунта необходимо ввести\nимя пользователя и пароль от аккаунта", 360, 150);
            }
        });

    }

    public void register(Pane root, Stage stage){

        View.text_output("Создание нового аккаунта", 200, 100);

        TextField login_f = new TextField();
        login_f.setLayoutX(330);
        login_f.setLayoutY(200);

        TextField password_f = new TextField();
        password_f.setLayoutX(330);
        password_f.setLayoutY(240);

        // Метки для полей ввода

        Label login_l = new Label("Имя пользователя:");
        login_l.setLayoutX(120);
        login_l.setLayoutY(200);
        login_l.setFont(new Font("Arial", 18));

        Label password_l = new Label("Придумайте пароль:\n(от 5 знаков)");
        password_l.setLayoutX(120);
        password_l.setLayoutY(240);
        password_l.setFont(new Font("Arial", 18));

        root.getChildren().addAll(password_l, password_f, login_l, login_f);

        //делает кнопку "Продолжить"
        Button next = new Button();
        next.setText("Продолжить");
        next.setTranslateX(460);
        next.setTranslateY(490);
        next.setPrefSize(100, 30);
        root.getChildren().add(next);

        Button back = new Button("Назад");
        back.setTranslateX(20);
        back.setTranslateY(490);
        back.setPrefSize(50, 30);
        root.getChildren().add(back);

        back.setOnAction(event -> {
            root.getChildren().removeAll(next, password_l, password_f, login_l, login_f, back);
            stop_firebase();
            start(stage);
        });

        next.setOnAction(event -> //нажали "Продолжить"
        {
            String password = password_f.getText();
            String login = login_f.getText();

            int check = 0;
            if (password == null || password.length() < 5){
                check+=1;
                warning("В пароле должно быть минимум 5 знаков", 340, 80);
            }
            // Регулярное выражение для проверки адреса электронной почты
            String regex = "^[a-zA-Zа-яА-ЯёЁ0-9._\\-@#]{1,20}$";
            Pattern pattern = Pattern.compile(regex);
            if (login == null || !pattern.matcher(login).matches()){
                check+=1;
                warning("Поле для имени не должно быть пустым\n или содержать больше 20 знаков.\nМожно использовать английские, русские буквы, цифры и спец. символы ( ._-@# )", 360, 150);
            }
            if (check == 0) {
                String collection_name = login+" "+password;
                boolean answer = new_user(collection_name, db, login);
                if (answer){
                    the_user = collection_name;
                    the_login = login;
                    root.getChildren().removeAll(next, password_l, password_f, login_l, login_f, back, next);
                    plant_list(root, stage);
                }

            }
        });
    }

    public void login (Pane root, Stage stage){

        View.text_output("Авторизация", 250, 100);

        TextField login_f = new TextField();
        login_f.setLayoutX(330);
        login_f.setLayoutY(200);

        TextField password_f = new TextField();
        password_f.setLayoutX(330);
        password_f.setLayoutY(240);

        // Метки для полей ввода

        Label login_l = new Label("Имя пользователя:");
        login_l.setLayoutX(120);
        login_l.setLayoutY(200);
        login_l.setFont(new Font("Arial", 18));

        Label password_l = new Label("Пароль:\n");
        password_l.setLayoutX(120);
        password_l.setLayoutY(240);
        password_l.setFont(new Font("Arial", 18));

        root.getChildren().addAll(password_l, password_f, login_l, login_f);

        //делает кнопку "Продолжить"
        Button next = new Button();
        next.setText("Продолжить");
        next.setTranslateX(460);
        next.setTranslateY(490);
        next.setPrefSize(100, 30);
        root.getChildren().add(next);

        Button back = new Button("Назад");
        back.setTranslateX(20);
        back.setTranslateY(490);
        back.setPrefSize(50, 30);
        root.getChildren().add(back);

        back.setOnAction(event -> {
            root.getChildren().removeAll(next, password_l, password_f, login_l, login_f, back);
            stop_firebase();
            start(stage);
        });

        next.setOnAction(event -> //нажали "Продолжить"
        {
            String password = password_f.getText();
            String login = login_f.getText();

            int check = 0;
            if (password == null){
                check+=1;
                warning("В пароле должно быть минимум 5 знаков", 340, 80);
            }
            if (login == null){
                check+=1;
                warning("Поле для имени не должно быть пустым", 360, 100);
            }
            if (check == 0) {
                String collection_name = login+" "+password;
                boolean result = check_the_user (collection_name, db);

                if (result){
                    the_user = collection_name;
                    root.getChildren().removeAll(next, password_l, password_f, login_l, login_f, back, next);
                    plant_list(root, stage);
                }
                else{
                    warning("Пользователь не найден.\nПроверьте правильность имени и пароля", 360, 160);
                }
            }
        });
    }


    @Override
    public void start(Stage stage)
    {
        Pane root = new Pane();
        View.create_text(root);
        View.text_output("                     Здравствуйте!\n" +
                "       Для дальнейшей работы в приложении\nнеобходимо войти в аккаунт или создать новый", 110, 120);
        View.set_the_background (root);
        db = start_firebase();

        Button register_b= new Button();
        register_b.setText("Создать аккаунт");
        register_b.setTranslateX(240);
        register_b.setTranslateY(260);
        register_b.setPrefSize(130, 30);
        root.getChildren().add(register_b);

        Button login_b = new Button();
        login_b.setText("Войти");
        login_b.setTranslateX(260);
        login_b.setTranslateY(200);
        login_b.setPrefSize(90, 30);
        root.getChildren().add(login_b);

        register_b.setOnAction(event -> //нажали "Создать аккаунт"
        {
            root.getChildren().removeAll(login_b, register_b);
            View.text_output("", 110, 120);
            register( root, stage);
        });

        login_b.setOnAction(event -> //нажали "Создать аккаунт"
        {
            root.getChildren().removeAll(login_b, register_b);
            View.text_output("", 110, 120);
            login ( root, stage);
        });

        View.create_scene(root, stage);// Создание сцены с одним корневым элементом
    }
}
