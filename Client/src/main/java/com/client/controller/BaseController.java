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
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Базовый контроллер для JavaFX приложения.
 * Содержит общие методы для работы с сетью и переключения окон.
 */
public abstract class BaseController {

    /**
     * Отправляет запрос на сервер и получает ответ.
     */
    protected Response sendRequest(Request request) {
        return ServerConnection.getInstance().sendRequest(request);
    }

    /**
     * Выполняет запрос к серверу асинхронно с индикацией загрузки.
     */
    protected void executeTask(Request request, Consumer<Response> onSuccess) {
        // Мы не добавляем ProgressIndicator в дерево здесь, так как не знаем корень,
        // но в реальном приложении здесь можно было бы показать оверлей.
        // Пока просто выполняем задачу в Task для отзывчивости UI.
        
        Task<Response> task = new Task<>() {
            @Override
            protected Response call() throws Exception {
                return sendRequest(request);
            }
        };

        task.setOnSucceeded(event -> {
            Response response = task.getValue();
            if (response.isSuccess()) {
                onSuccess.accept(response);
            } else {
                showError("Ошибка сервера", response.getMessage());
            }
        });

        task.setOnFailed(event -> {
            showError("Сетевая ошибка", task.getException() != null ? task.getException().getMessage() : "Неизвестная ошибка");
        });

        new Thread(task).start();
    }

    /**
     * Показывает диалог подтверждения.
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
     * Переключает текущую сцену на другую по FXML файлу.
     */
    protected void switchScene(String fxmlPath, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Airport System - " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка загрузки окна: " + fxmlPath, e.getMessage());
        }
    }

    /**
     * Показывает окно с ошибкой.
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
     * Показывает информационное сообщение.
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
