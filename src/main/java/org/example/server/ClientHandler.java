package org.example.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Важно: сначала создаем ObjectOutputStream, затем ObjectInputStream,
            // чтобы избежать взаимной блокировки потоков (deadlock)
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Поток для клиента запущен.");
            
            // В будущем здесь будет цикл чтения Request от клиента 
            // и отправки Response обратно
            while (true) {
                // Заглушка: читаем любой объект и выводим сообщение
                Object request = in.readObject();
                if (request != null) {
                    System.out.println("Получен запрос от клиента: " + request);
                }
            }
        } catch (Exception e) {
            System.out.println("Клиент отключился: " + socket.getInetAddress().getHostAddress());
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии сокетов: " + e.getMessage());
        }
    }
}
