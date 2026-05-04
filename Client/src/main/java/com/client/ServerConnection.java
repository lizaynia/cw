package com.client;

import com.common.Request;
import com.common.Response;
import com.common.dto.UserDto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private static ServerConnection instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private UserDto currentUser;

    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    private ServerConnection() {
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Подключено к серверу.");
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public Response sendRequest(Request request) {
        int attempts = 3;
        while (attempts > 0) {
            try {
                if (socket == null || socket.isClosed()) {
                    connect();
                }
                out.writeObject(request);
                out.flush();
                return (Response) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                attempts--;
                System.err.println("Ошибка сетевого обмена, попытка переподключения... (осталось " + attempts + ")");
                connect();
            }
        }
        return new Response(false, "Не удалось связаться с сервером после нескольких попыток.");
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserDto currentUser) {
        this.currentUser = currentUser;
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Соединение с сервером закрыто.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
