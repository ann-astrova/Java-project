import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class UD {
    private static String DB_URL = "jdbc:postgresql://localhost:5432/";
    private static final String USER = "postgres"; // Замените на ваше имя пользователя
    private static final String PASSWORD = "Uni25corn)";
    private static Connection connection;
    private static int ad_or_g;

    private static String make_creator = """ 
            CREATE OR REPLACE PROCEDURE create_book()
            AS $$
            BEGIN
            CREATE TABLE book (
            id INT primary key check (id > 0),
            Имя text not null,
            Фамилия text not null,
            Телефон text not null,
            Звонки int not null check(Звонки >= 0),
            Заметки text not null);
            END;
            $$ LANGUAGE plpgsql;""";

    private static String make_all_deleter = """ 
            CREATE OR REPLACE PROCEDURE del_book()
            AS $$
            BEGIN
            DROP TABLE book;
            END;
            $$ LANGUAGE plpgsql;""";

    private static String make_clearer = """ 
            CREATE OR REPLACE PROCEDURE clear_book()
            AS $$
            BEGIN
            TRUNCATE TABLE book;
            END;
            $$ LANGUAGE plpgsql;""";

    private static String make_adder = """ 
            CREATE OR REPLACE PROCEDURE add_data(id INT, name text, last_name text, phone text, note text)
            AS $$
            BEGIN
            INSERT INTO book (id, Имя, Фамилия, Телефон, Звонки, Заметки) VALUES
            (id, name, last_name, phone, 0, note);
            END;
            $$ LANGUAGE plpgsql;""";

    private static String make_opener = """ 
            CREATE OR REPLACE FUNCTION get_data()
                RETURNS TABLE(id INT, имя TEXT, фамилия TEXT, телефон TEXT, звонки INT, заметки TEXT) AS $$
            BEGIN
                RETURN QUERY SELECT * FROM book;
            END;
            $$ LANGUAGE plpgsql;""";

    private static String make_finder = """ 
            CREATE OR REPLACE FUNCTION find_data(in place text, in neww text)
                        RETURNS TABLE(id INT, имя TEXT, фамилия TEXT, телефон TEXT, звонки INT, заметки TEXT) AS $$
                        BEGIN
                        	IF (place = 'ID') then
                        		RETURN QUERY SELECT * FROM book WHERE book.Id = CAST(neww AS int);
                        	END IF;
                        	IF (place = 'Имя') then
                        		RETURN QUERY SELECT * FROM book WHERE Имя = neww;
                        	END IF;
                        	IF (place = 'Фамилия') then
                        		RETURN QUERY SELECT * FROM book WHERE Фамилия = neww;
                        	END IF;
                        	IF (place = 'Телефон') then
                        		RETURN QUERY SELECT * FROM book WHERE Телефон = neww;
                        	END IF;
                        	IF (place = 'Звонки') then
                        		RETURN QUERY SELECT * FROM book WHERE Звонки = CAST(neww AS int);
                        	END IF;
                        	IF (place = 'Заметки') then
                        		RETURN QUERY SELECT * FROM book WHERE Заметки = neww;
                        	END IF;
                        END;
                        $$ LANGUAGE plpgsql;""";

    private static String make_deleter = """ 
            CREATE OR REPLACE PROCEDURE delete_data(in place text, in neww text)
                        AS $$
                        BEGIN
                        	IF (place = 'ID') then
                        		DELETE FROM book WHERE book.Id = CAST(neww AS int);
                        	END IF;
                        	IF (place = 'Имя') then
                        		DELETE FROM book WHERE Имя = neww;
                        	END IF;
                        	IF (place = 'Фамилия') then
                        		DELETE FROM book WHERE Фамилия = neww;
                        	END IF;
                        	IF (place = 'Телефон') then
                        		DELETE FROM book WHERE Телефон = neww;
                        	END IF;
                        	IF (place = 'Звонки') then
                        		DELETE FROM book WHERE Звонки = CAST(neww AS int);
                        	END IF;
                        	IF (place = 'Заметки') then
                        		DELETE FROM book WHERE Заметки = neww;
                        	END IF;
                        END;
                        $$ LANGUAGE plpgsql;""";

    private static String make_updater = """ 
            CREATE OR REPLACE PROCEDURE update_data(in place text, in i text, in neww text)
                        AS $$
                        BEGIN
                        	IF (place = 'ID') then
                        		UPDATE book SET book.Id = CAST(neww AS int) WHERE book.Id = CAST(i AS int);
                        	END IF;
                        	IF (place = 'Имя') then
                        		UPDATE book SET Имя = neww WHERE book.Id = CAST(i AS int);
                        	END IF;
                        	IF (place = 'Фамилия') then
                        		UPDATE book SET Фамилия = neww WHERE book.Id = CAST(i AS int);
                        	END IF;
                        	IF (place = 'Телефон') then
                        	    UPDATE book SET Телефон = neww WHERE book.Id = CAST(i AS int);
                        	END IF;
                        	IF (place = 'Звонки') then
                        	    UPDATE book SET Звонки = CAST(neww AS int) WHERE book.Id = CAST(i AS int);
                        	END IF;
                        	IF (place = 'Заметки') then
                        		UPDATE book SET Заметки = neww WHERE book.Id = CAST(i AS int);
                        	END IF;
                        END;
                        $$ LANGUAGE plpgsql;""";







    public void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Database");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 300);
                frame.setLayout(new FlowLayout());

                JButton createButton = new JButton("Создать базу данных");
                createButton.addActionListener(e -> createDatabase());
                frame.add(createButton);

                JButton openButton = new JButton("Открыть базу данных");
                openButton.addActionListener(_ -> openDatabase());
                frame.add(openButton);

                JButton clearButton = new JButton("Очистить базу данных");
                clearButton.addActionListener(_ -> clearDatadase());
                frame.add(clearButton );

                JButton deletebaseButton = new JButton("Удалить данные");
                deletebaseButton.addActionListener(_ -> delete_win());
                frame.add(deletebaseButton);

                JButton findButton = new JButton("Найти данные");
                findButton.addActionListener(_ -> find_win());
                frame.add(findButton);

                JButton deleteButton = new JButton("Удалить базу данных");
                deleteButton.addActionListener(_ -> deleteDatabase());
                frame.add(deleteButton);

                JButton addButton = new JButton("Добавить данные");
                addButton.addActionListener(_ -> add_data_win());
                frame.add(addButton);

                JButton updateButton = new JButton("Обновить данные");
                updateButton.addActionListener(e -> update_win());
                frame.add(updateButton);

                frame.setVisible(true);
            });


        UD base = new UD();
        try {
            base.connect();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }





    private static void createDatabase() {
        try  {
            String sql = "CREATE DATABASE ph";
            DB_URL = "jdbc:postgresql://localhost:5432/ph";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            sql = make_creator;
            stmt.executeUpdate(sql);
            sql = make_all_deleter;
            stmt.executeUpdate(sql);
            sql = make_clearer;
            stmt.executeUpdate(sql);
            sql = make_adder;
            stmt.executeUpdate(sql);
            sql = "CALL create_book()";
            stmt.executeUpdate(sql);

            JOptionPane.showMessageDialog(null, "База данных успешно создана");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "База данных уже была создана");
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void openDatabase() {
        try{
            Statement st = connection.createStatement();
            String sqlo = make_opener;
            st.executeUpdate(sqlo);
            String sql = "SELECT * FROM get_data()";
            Statement stmt = connection.createStatement();
            ResultSet ans = stmt.executeQuery(sql);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Data");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(550, 700);
                frame.setLayout(new FlowLayout());

                int flag = 0;

                while (true){
                    try {
                        if (!ans.next()) break;
                        flag += 1;
                        JLabel label = new JLabel("ID: " + ans.getString("Id"));
                        frame.add(label);
                        label = new JLabel("Имя: " +ans.getString("Имя"));
                        frame.add(label);
                        label = new JLabel("Фамилия: " +ans.getString("Фамилия"));
                        frame.add(label);
                        label = new JLabel("Телефон: " + ans.getString("Телефон"));
                        frame.add(label);
                        label = new JLabel("Звонки: " + ans.getString("Звонки"));
                        frame.add(label);
                        label = new JLabel("Заметки: " +ans.getString("Заметки"));
                        frame.add(label);

                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
                    }
                }
                if (flag == 0){
                    JLabel label = new JLabel("В базе данных еще нет записей");
                    frame.add(label);
                }

                frame.setVisible(true);
            });
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "База данных не была создана");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void update_win() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Update");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("Информации из какого столбца вы хотите обновить?");
            frame.add(label);

            String[] items = {
                    "ID",
                    "Имя",
                    "Фамилия",
                    "Телефон",
                    "Звонки",
                    "Заметки",
            };
            JComboBox comboBox = new JComboBox(items);
            frame.add(comboBox);

            label = new JLabel("Введите какая информация должна находиться в этом столбце теперь");
            frame.add(label);

            JTextField txt = new JTextField(30);
            frame.add(txt);

            label = new JLabel("Введите id строчки, которую вы хотите изменить");
            frame.add(label);

            JTextField id = new JTextField(30);
            frame.add(id);

            JButton updateButton = new JButton("Обновить");
            updateButton.addActionListener(e -> update_data(txt.getText(), id.getText(), String.valueOf(comboBox.getSelectedItem())));
            frame.add(updateButton);

            frame.setVisible(true);
        });
    }

    private static void update_data(String neww, String id, String place) {
        try{
            Statement st = connection.createStatement();
            String sqlo = make_updater;
            st.executeUpdate(sqlo);
            String sql = "CALL update_data('" + place + "', '" + id + "', '" + neww+ "')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            JOptionPane.showMessageDialog(null, "Нужная строка успешно обновлены");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Либо база данных не была создана, либо вы пытаетесь написать текст в числовом столбце");
            e.printStackTrace();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void add_data_win() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Add");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("Введите информацию, которую хотите добавить в телефонную книгу");
            frame.add(label);

            label = new JLabel("Идентификатор:            ");
            frame.add(label);

            JTextField id = new JTextField(30);
            frame.add(id);

            label = new JLabel("Имя:                           ");
            frame.add(label);

            JTextField name = new JTextField(30);
            frame.add(name);


            label = new JLabel("Фамилия:                 ");
            frame.add(label);

            JTextField last_name = new JTextField(30);
            frame.add(last_name);

            label = new JLabel("Телефон:                ");
            frame.add(label);

            JTextField phone = new JTextField(30);
            frame.add(phone);

            label = new JLabel("Заметки:               ");
            frame.add(label);

            JTextField note = new JTextField(30);
            frame.add(note);

            JButton updateButton = new JButton("Добавить");
            updateButton.addActionListener(e -> add_data(id.getText(), name.getText(), last_name.getText(), phone.getText(), note.getText()));
            frame.add(updateButton);

            frame.setVisible(true);
        });
    }

    private static void add_data(String id, String name, String last_name, String phone, String note) {
        try  {
            Integer.parseInt(id);
            String sql = "CALL add_data(" + id + ", '" + name + "' , '" + last_name + "' , '"+ phone + "' , '"+ note + "');";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "Запись успешно добавлена");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Это идентификатор занят");

        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Идентификатор - целое число больше 0");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void clearDatadase() {
        try  {
            String sql = "CALL clear_book()";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "База данных успешно очищена");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "База данных не существует");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void deleteDatabase() {
        try  {
            String sql = "CALL del_book()";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            sql = "DROP DATABASE ph";
            stmt.executeUpdate(sql);
            JOptionPane.showMessageDialog(null, "База данных успешно удалена");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "База данных не существует");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void find_win() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Find");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("По информации из какого столбца вы хотите осуществить поиск?");
            frame.add(label);

            String[] items = {
                    "ID",
                    "Имя",
                    "Фамилия",
                    "Телефон",
                    "Звонки",
                    "Заметки",
            };
            JComboBox comboBox = new JComboBox(items);
            frame.add(comboBox);

            label = new JLabel("Введите какая информация должна находиться в этом столбце");
            frame.add(label);

            JTextField txt = new JTextField(30);
            frame.add(txt);

            JButton updateButton = new JButton("Найти");
            updateButton.addActionListener(e -> find_data(txt.getText(), String.valueOf(comboBox.getSelectedItem())));
            frame.add(updateButton);

            frame.setVisible(true);
        });
    }

    private static void delete_win() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Delete");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("По информации из какого столбца вы хотите осуществить удаление?");
            frame.add(label);

            String[] items = {
                    "ID",
                    "Имя",
                    "Фамилия",
                    "Телефон",
                    "Звонки",
                    "Заметки",
            };
            JComboBox comboBox = new JComboBox(items);
            frame.add(comboBox);

            label = new JLabel("Введите какая информация должна находиться в этом столбце");
            frame.add(label);

            JTextField txt = new JTextField(30);
            frame.add(txt);

            JButton updateButton = new JButton("Удалить");
            updateButton.addActionListener(e -> delete_data(txt.getText(), String.valueOf(comboBox.getSelectedItem())));
            frame.add(updateButton);

            frame.setVisible(true);
        });
    }

    private static void delete_data(String txt, String place) {
        try{
            Statement st = connection.createStatement();
            String sqlo = make_deleter;
            st.executeUpdate(sqlo);
            String sql = "CALL delete_data('" + place + "', '" + txt + "')";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            JOptionPane.showMessageDialog(null, "Нужные строки успешно удалены");
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Либо база данных не была создана, либо вы пытаетесь найти текст в числовом столбце");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

    private static void find_data(String txt, String place) {
        try{
            Statement st = connection.createStatement();
            String sqlo = make_finder;
            st.executeUpdate(sqlo);
            String sql = "SELECT * FROM find_data('" + place + "', '" + txt + "')";
            Statement stmt = connection.createStatement();
            ResultSet ans = stmt.executeQuery(sql);

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Find_Data");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(550, 700);
                frame.setLayout(new FlowLayout());

                int flag = 0;

                while (true){
                    try {
                        if (!ans.next()) break;
                        flag += 1;
                        JLabel label = new JLabel("ID: " + ans.getString("Id"));
                        frame.add(label);
                        label = new JLabel("Имя: " +ans.getString("Имя"));
                        frame.add(label);
                        label = new JLabel("Фамилия: " +ans.getString("Фамилия"));
                        frame.add(label);
                        label = new JLabel("Телефон: " + ans.getString("Телефон"));
                        frame.add(label);
                        label = new JLabel("Звонки: " + ans.getString("Звонки"));
                        frame.add(label);
                        label = new JLabel("Заметки: " +ans.getString("Заметки"));
                        frame.add(label);

                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
                    }
                }
                if (flag == 0){
                    JLabel label = new JLabel("Записей не найдено");
                    frame.add(label);
                }

                frame.setVisible(true);
            });
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Либо база данных не была создана, либо вы пытаетесь найти текст в числовом столбце");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не предвиденная ошибка");
        }
    }

}
