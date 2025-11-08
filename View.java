package org.example;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Objects;

public class View{
    private static Text warning;
    private static Text text; //основной текст

    public static void create_scene (Pane root, Stage stage)
    {
        Scene scene = new Scene(root, 610, 550); //ширина и высота
        stage.setScene(scene);
        stage.setTitle("Забота о растениях");
        stage.show(); //показывает окно
    }

    public static void create_text(Pane root)
    {
        text = new Text(" ");
        root.getChildren().add(text);
    }

    public static void set_the_background (Pane root)
    {
        Image image = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/images/image.jpg"))); // Замените на URL вашего изображения
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(550);
        imageView.setFitWidth(610);

        imageView.setOpacity(0.2); // Установка прозрачности
        root.getChildren().add(imageView);
    }

    public static int warning (String message, int width, int height) //это для предупреждения
    {
        Alert alert = new Alert(Alert.AlertType.WARNING); // Создаем предупреждение
        alert.setTitle("Предупреждение"); // Заголовок окна
        alert.setHeaderText(null); // Убираем заголовок
        alert.setContentText("\n"+message); // Устанавливаем текст сообщения
        alert.getDialogPane().setPrefSize(width, height); // Ширина и высота окна
        // Добавляем кнопку "ОК"
        alert.getButtonTypes().setAll(ButtonType.OK);

        // Показываем окно и ждем результата
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        // Проверяем, была ли нажата кнопка "ОК"
        if (result == ButtonType.OK) {
            return 1; // Возвращаем 1, если нажали "ОК"
        } else {
            return 0; // Возвращаем 0, если закрыли окно крестиком
        }
    }

    public static void message (String message, int width, int height) //это для предупреждения
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Создаем предупреждение
        alert.setTitle("Информация"); // Заголовок окна
        alert.setHeaderText(null); // Убираем заголовок
        alert.setContentText("\n"+message); // Устанавливаем текст сообщения
        alert.getDialogPane().setPrefSize(width, height); // Ширина и высота окна
        alert.showAndWait();
    }

    public static void text_output (String t,double x, double y)
    {
        text.setText(t);
        text.setLayoutX(x);
        text.setLayoutY(y);
        Font font = new Font("Arial", 20);
        text.setFont(font);
    }

    public static void remove_warnings (Pane root)
    {
        root.getChildren().remove(warning);
    }
}
