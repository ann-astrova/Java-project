package org.example;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import java.io.*;

import static org.example.View.message;
import static org.example.View.warning;

public class Database {
    public static Firestore start_firebase() {
        try {
            // Загружаем JSON-файл с учетными данными из ресурсов
            InputStream serviceAccount = Database.class.getClassLoader().getResourceAsStream("lab-63efa-firebase-adminsdk-fbsvc-08450a634e.json");

            if (serviceAccount == null) {
                throw new FileNotFoundException("Файл учетных данных не найден в ресурсах.");
            }

            // Используем builder() для настроек Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://<your-database-name>.firebaseio.com/")
                    .build();

            //FirebaseApp.initializeApp(options);
            FirebaseApp app = FirebaseApp.initializeApp(options);
            // Возвращаем экземпляр Firestore
            return FirestoreClient.getFirestore(app);
        } catch (IOException e) {
            System.out.println("Error initializing Firebase: " + e.getMessage());
            return null;
        }
    }

    public static void stop_firebase() {
        try {
            FirebaseApp.getInstance().delete(); // Завершение работы
            System.out.println("Firebase успешно отключен.");
        } catch (Exception e) {
            System.out.println("Ошибка при отключении Firebase: " + e.getMessage());
        }
    }

    public static boolean new_user(String login_password, Firestore db, String login) {
        if (check_new_username(db, login)) {
            // Уже есть коллекция, начинающаяся с логина
            warning("Пользователь с таким логином уже существует", 360, 100);
            return false;
        }
        // Создаем новую коллекцию
        CollectionReference collectionRef = db.collection(login_password);
        try {
            ApiFuture<WriteResult> future = collectionRef.document().set(new java.util.HashMap<>());
            future.get();
            message("Успешно создан новый пользователь:\n" + login, 360, 150);
            return true;
        } catch (Exception e) {
            warning("Ошибка при создании пользователя:\n" + e.getMessage(), 360, 250);
            return false;
        }
    }

    public static ArrayList<Plant> inform(Firestore db, String user) {
        ArrayList<Plant> plants = new ArrayList<>();
        CollectionReference collection = db.collection(user);

        try {
            // Получаем все документы из коллекции
            ApiFuture<QuerySnapshot> future = collection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                // Проверяем, есть ли поле "name"
                if (doc.contains("name")) {
                    Map<String, Object> data = doc.getData();

                    // Безопасно достаём поля (с соответствующими приводами)
                    double vol = data.get("vol") != null ? ((Number) data.get("vol")).doubleValue() : 0.0;
                    double roomTemp = data.get("room_temperature") != null ? ((Number) data.get("room_temperature")).doubleValue() : 0.0;
                    double height = data.get("height") != null ? ((Number) data.get("height")).doubleValue() : 0.0;

                    // Для pic — предположим, что в документе хранится путь или URL в виде строки
                    File pic = null;
                    if (data.get("pic") != null) {
                        String picPath = data.get("pic").toString();
                        pic = new File(picPath); // В зависимости от задачи здесь можно сделать по-другому
                    }

                    String inf = data.get("inf") != null ? data.get("inf").toString() : "";
                    String name = data.get("name").toString();

                    // calendar — список строк
                    ArrayList<String> calendar = new ArrayList<>();
                    if (data.get("calendar") != null) {
                        List<Object> rawCalendar = (List<Object>) data.get("calendar");
                        for (Object o : rawCalendar) {
                            calendar.add(o.toString());
                        }
                    }
                    Plant plant = new Plant(vol, roomTemp, height, pic, inf, name, calendar);
                    plants.add(plant);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении растений: " + e.getMessage());
        }
        return plants;
    }

    public static boolean check_the_user(String the_user, Firestore db) {
        try {
            // Делается запрос к коллекции с лимитом 1, чтобы проверить наличие хотя бы одного документа
            ApiFuture<QuerySnapshot> future = db.collection(the_user).limit(1).get();
            QuerySnapshot querySnapshot = future.get();

            // Если есть хотя бы один документ, коллекция существует
            return !querySnapshot.isEmpty();

        } catch (Exception e) {
            e.printStackTrace();
            // В случае ошибки можно считать, что коллекция не существует или есть проблема доступа
            return false;
        }
    }

    public static boolean check_new_username(Firestore db, String login) {
        try {
            // Получаем список всех коллекций
            Iterable<CollectionReference> collections = db.listCollections();

            // Обходим коллекции
            for (CollectionReference collRef : collections) {
                String collName = collRef.getId();
                if (collName.startsWith(login)) {
                    // Коллекция с нужным префиксом найдена
                    return true;
                }
            }
            return false; // Ни одной подходящей не нашли
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void delete_user(Firestore db, String login_password, int number) throws InterruptedException, ExecutionException, URISyntaxException {
        CollectionReference collection = db.collection(login_password);
        // Задаем путь к директории с изображениями
        String directoryPath = "plant images";

        // Создаем объект File для директории
        File directory = new File(Objects.requireNonNull(Main.class.getClassLoader().getResource(directoryPath)).toURI());
        if (directory.isDirectory()) {
            // Получаем список файлов в директории
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Проверяем, является ли объект файлом
                    if (file.isFile()) {
                        // Проверяем, начинает ли имя файла с login_password и следует символ подчеркивания "_"
                        if (file.getName().startsWith(login_password + "_")) {
                            // Удаляем файл
                            boolean deleted = file.delete();
                            if (deleted) {
                                System.out.println("Файл успешно удален: " + file.getAbsolutePath());
                            } else {
                                System.err.println("Не удалось удалить файл: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
        delete_all_plants(db, collection, number);
    }

    private static void delete_all_plants(Firestore db, Query query, int number) throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = query.limit(number).get();
        int deleted = 0;
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        WriteBatch batch = db.batch();

        for (DocumentSnapshot document : documents) {
            batch.delete(document.getReference());
        }

        ApiFuture<List<WriteResult>> writeFuture = batch.commit();
        writeFuture.get();
        deleted = documents.size();

        if (deleted >= number) {
            // Рекурсивно вызываем, пока не удалим все документы
            delete_all_plants(db, query, number);
        }
    }

    public static boolean delete_plant_db(Firestore db, String the_user, String plant_name) {
        // Создаем ссылку на документ по его имени
        DocumentReference docRef = db.collection(the_user).document(plant_name);

        // Выполняем операцию удаления
        ApiFuture<WriteResult> future = docRef.delete();
        try {
            // Ждем результатов операции удаления
            WriteResult result = future.get();


            // Задаем путь к директории с изображениями
            String directoryPath = "plant images";
            File directory = new File(Main.class.getClassLoader().getResource(directoryPath).toURI());

            // Формируем имя файла
            String file_jpg = the_user + "_" + plant_name + ".jpg"; // Замените ".jpg" на ".png" или другое расширение, если нужно
            // Получаем путь к директории с изображениями
            File file_del_jpg = new File(directory, file_jpg);

            String file_png = the_user + "_" + plant_name + ".png"; // Замените ".jpg" на ".png" или другое расширение, если нужно
            File file_del_png = new File(directoryPath, file_jpg);

            // Проверяем, существует ли файл
            if (file_del_jpg.exists()) {
                // Удаляем файл
                boolean deleted = file_del_jpg.delete();
                if (deleted) {
                    System.out.println("Файл успешно удален: " + file_del_jpg.getAbsolutePath());
                }
            }
            else if(file_del_png.exists()){
                // Удаляем файл
                boolean deleted = file_del_png.delete();
                if (deleted) {
                    System.out.println("Файл успешно удален: " + file_del_png.getAbsolutePath());
                }
            }
            return true;
        } catch (ExecutionException | InterruptedException e) {
            return  false;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ad_picture(String plant_name, String imagePath, String info, Firestore db, String the_user) throws Exception {
        // Папка назначения в проекте
        // Папка назначения в проекте
        String resourcesDirPath = "plant images"; // Папка внутри src/main/resources

        // Получаем путь к директории с изображениями
        File resourcesDir;
        try {
            resourcesDir = new File(Objects.requireNonNull(Main.class.getClassLoader().getResource(resourcesDirPath)).toURI());
        } catch (NullPointerException e) {
            throw new IOException("Не удалось загрузить ресурс: " + resourcesDirPath);
        } catch (URISyntaxException e) {
            throw new IOException("Ошибка в синтаксисе URI: " + e.getMessage());
        }

        // Проверяем, существует ли папка; если нет, создаем её
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }

        // Определяем новое имя файла
        String newFileName = the_user + "_" + plant_name + ".jpg"; // Или используйте .png в зависимости от расширения
        File destFile = new File(resourcesDir, newFileName);

        // Копируем файл
        File sourceFile = new File(imagePath);
        if (!sourceFile.exists()) {
            throw new IOException("Исходный файл не найден: " + imagePath);
        }

        // Копируем изображение, заменяя существующий файл, если он есть
        try (FileInputStream inStream = new FileInputStream(sourceFile);
             FileOutputStream outStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            System.out.println("Файл скопирован: " + destFile.getAbsolutePath());
        }

        // Обновляем Firestore с новой информацией
        update_doc(db, plant_name, the_user, destFile.getAbsolutePath(), info);
    }


    private static void update_doc(Firestore db, String plant_name, String the_user, String imagePath, String
            info) throws Exception {
        // Поиск документа по имени
        ApiFuture<QuerySnapshot> query = db.collection(the_user)
                .whereEqualTo("name", plant_name)
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        if (documents.size() > 0) {
            Map<String, Object> updates = new HashMap<>();

            // Обновляем поле pic
            if (documents.get(0).getData().containsKey("pic") && documents.get(0).getString("pic") != null) {
                // Если поле pic существует и не пустое, очищаем его и заменяем новым значением
                updates.put("pic", imagePath);
            } else {
                updates.put("pic", imagePath); // Если поля pic не было, создаем его
            }

            // Обновляем поле info
            if (documents.get(0).getData().containsKey("inf") && documents.get(0).getString("info") != null) {
                // Если поле info существует и не пустое, очищаем его и заменяем новым значением
                updates.put("inf", info); // Добавляем только новую информацию
            } else {
                updates.put("inf", info); // Если поля info не было, создаем его
            }

            // Обновляем поля pic и info в документе
            ApiFuture<WriteResult> future = db.collection(the_user).document(documents.get(0).getId()).update(updates);
            future.get(); // Дожидаемся завершения
            System.out.println("Поля pic и info обновлены в Firestore.");
        } else {
            System.err.println("Документ с именем " + plant_name + " не найден.");
        }
    }
}



