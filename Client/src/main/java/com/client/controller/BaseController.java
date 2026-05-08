package com.client.controller;

import com.client.ServerConnection;
import com.common.Request;
import com.common.Response;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * БАЗОВЫЙ КОНТРОЛЛЕР - все другие контроллеры наследуются от него
 *
 * ЧТО ОН ДЕЛАЕТ:
 * 1. Даёт доступ к отправке запросов на сервер
 * 2. Показывает ошибки и информационные окна
 * 3. Переключает между окнами (сценами)
 * 4. Выполняет асинхронные запросы без зависания UI
 */
public abstract class BaseController {

    /**
     * МЕТОД 1: sendRequest(Request request)
     *
     * Что делает: Отправляет запрос на сервер и синхронно ждёт ответ
     * Когда использовать: НЕ ИСПОЛЬЗОВАТЬ напрямую! Используйте executeTask()
     * Проблема: Блокирует поток UI на время ожидания
     */
    protected Response sendRequest(Request request) {
        return ServerConnection.getInstance().sendRequest(request);
    }

    /**
     * МЕТОД 2: executeTask(Request request, Consumer<Response> onSuccess)
     *
     * Что делает:
     * - Запускает запрос в отдельном потоке (чтобы UI не зависал)
     * - При успехе вызывает onSuccess (ваш код обработки ответа)
     * - При ошибке показывает окно с ошибкой
     *
     * КАК РАБОТАЕТ:
     * 1. Создаёт Task (фоновое задание)
     * 2. В фоне вызывает sendRequest()
     * 3. Когда ответ получен, вызывает onSuccess в JavaFX потоке
     *
     * ПОЧЕМУ ТАК НУЖНО? JavaFX не любит, когда UI меняют из других потоков
     */
    protected void executeTask(Request request, Consumer<Response> onSuccess) {
        // Task - это фоновое задание
        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                // Этот код выполняется в отдельном потоке
                return sendRequest(request);
            }
        };

        // Что делать, когда задание успешно завершилось
        task.setOnSucceeded(event -> {
            Response response = task.getValue(); // Получаем результат
            if (response.isSuccess()) {
                onSuccess.accept(response); // Вызываем ваш код
            } else {
                showError("Ошибка сервера", response.getMessage());
            }
        });

        // Что делать, если задание упало с ошибкой
        task.setOnFailed(event -> {
            showError("Сетевая ошибка",
                    task.getException() != null ? task.getException().getMessage() : "Неизвестная ошибка");
        });

        // Запускаем задание в новом потоке
        new Thread(task).start();
    }

    /**
     * МЕТОД 3: showConfirmation(String header, String content)
     *
     * Что делает: Показывает диалог с вопросом (Да/Нет)
     * Возвращает: true если пользователь нажал OK, false если Cancel
     *
     * Пример использования:
     * if (showConfirmation("Удалить?", "Вы уверены?")) {
     *     // удаляем
     * }
     */
    protected boolean showConfirmation(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * МЕТОД 4: switchScene(String fxmlPath, String title, Stage stage)
     *
     * Что делает: Полностью заменяет текущее окно на новое
     *
     * Параметры:
     * - fxmlPath: путь к FXML файлу (например "/views/Login.fxml")
     * - title: заголовок окна
     * - stage: текущее окно, которое нужно заменить
     *
     * КОГДА ИСПОЛЬЗОВАТЬ: При переходе между главными экранами
     * Например: из Login в ClientMain
     *
     * КОГДА НЕ ИСПОЛЬЗОВАТЬ: Для открытия дополнительных окон (используйте новый Stage)
     */
    protected void switchScene(String fxmlPath, String title, Stage stage) {
        try {
            // Загружаем FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Меняем сцену в текущем окне
            stage.setScene(new Scene(root));
            stage.setTitle("Airport System - " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки окна: " + fxmlPath + " - " + e.getMessage());
            showError("Ошибка загрузки окна: " + fxmlPath, e.getMessage());
        }
    }

    /**
     * МЕТОД 5: showError(String header, String content)
     *
     * Что делает: Показывает красное окно с ошибкой
     * Важно: Использует Platform.runLater() - это безопасно из любого потока
     */
    protected void showError(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * МЕТОД 6: showInfo(String header, String content)
     *
     * Что делает: Показывает синее информационное окно
     * Важно: Тоже использует Platform.runLater() для безопасности
     */
    protected void showInfo(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}