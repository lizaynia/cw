package com.server;

import com.server.utils.HibernateUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;
    // Пул на 10 потоков для одновременной обработки клиентов
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Инициализация базы данных...");
        try {
            HibernateUtil.getSessionFactory(); // Проверка подключения к БД
            System.out.println("База данных успешно подключена.");
        } catch (Exception e) {
            System.err.println("Ошибка при подключении к БД. Проверьте hibernate.cfg.xml!");
            e.printStackTrace();
            return; // Останавливаем запуск сервера, если БД не доступна
        }

        System.out.println("Запуск сервера на порту " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен и ожидает подключений.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress().getHostAddress());

                // Передаем клиента в отдельный поток (ClientHandler)
                ClientHandler clientThread = new ClientHandler(clientSocket);
                pool.execute(clientThread);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        } finally {
            pool.shutdown();
            HibernateUtil.shutdown();
        }
    }
}
