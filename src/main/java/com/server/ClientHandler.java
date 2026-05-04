package com.server;

import com.common.Request;
import com.common.Response;
import com.common.entity.Flight;
import com.common.entity.Ticket;
import com.common.entity.User;

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
            
            org.example.server.service.BookingService bookingService = new org.example.server.service.BookingService();
            org.example.server.service.AuthService authService = new org.example.server.service.AuthService();
            org.example.server.service.DispatcherService dispatcherService = new org.example.server.service.DispatcherService();
            org.example.server.service.AdminService adminService = new org.example.server.service.AdminService();
            org.example.server.service.ClientService clientService = new org.example.server.service.ClientService();
            
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Request) {
                    Request request = (Request) obj;
                    System.out.println("Получен запрос от клиента: " + request.getCommand());
                    
                    Response response = new Response();
                    
                    try {
                        switch (CommandType.valueOf(request.getCommand())) {
                            // --- АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ ---
                            case "REGISTER": {
                                String login = (String) request.getArgs()[0];
                                String password = (String) request.getArgs()[1];
                                String msg = authService.register(login, password);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                            case "LOGIN": {
                                String login = (String) request.getArgs()[0];
                                String password = (String) request.getArgs()[1];
                                User user = authService.login(login, password);
                                if (user != null) {
                                    response.setSuccess(true);
                                    response.setMessage("Успешный вход");
                                    response.setData(user);
                                } else {
                                    response.setSuccess(false);
                                    response.setMessage("Неверный логин или пароль");
                                }
                                break;
                            }

                            // --- КЛИЕНТ (И НЕАВТОРИЗОВАННЫЙ) ---
                            case "GET_SCHEDULE": {
                                java.util.List<Flight> flights = dispatcherService.getSchedule();
                                response.setSuccess(true);
                                response.setData(flights);
                                break;
                            }
                            case "BOOK_TICKET": {
                                Integer passengerId = (Integer) request.getArgs()[0];
                                Integer flightId = (Integer) request.getArgs()[1];
                                java.math.BigDecimal price = (java.math.BigDecimal) request.getArgs()[2];
                                
                                String msg = bookingService.bookTicket(passengerId, flightId, price);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                            case "GET_TICKET_HISTORY": {
                                Integer passengerId = (Integer) request.getArgs()[0];
                                java.util.List<Ticket> history = clientService.getTicketHistory(passengerId);
                                response.setSuccess(true);
                                response.setData(history);
                                break;
                            }

                            // --- ДИСПЕТЧЕР ---
                            case "ADD_AIRPLANE": {
                                String model = (String) request.getArgs()[0];
                                Integer capacity = (Integer) request.getArgs()[1];
                                String msg = dispatcherService.addAirplane(model, capacity);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                            case "ADD_FLIGHT": {
                                String flightNum = (String) request.getArgs()[0];
                                String depCity = (String) request.getArgs()[1];
                                String arrCity = (String) request.getArgs()[2];
                                java.time.LocalDateTime time = (java.time.LocalDateTime) request.getArgs()[3];
                                Integer airplaneId = (Integer) request.getArgs()[4];
                                
                                String msg = dispatcherService.addFlight(flightNum, depCity, arrCity, time, airplaneId);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }

                            // --- АДМИНИСТРАТОР ---
                            case "GET_USERS": {
                                java.util.List<User> users = adminService.getAllUsers();
                                response.setSuccess(true);
                                response.setData(users);
                                break;
                            }
                            case "CHANGE_ROLE": {
                                Integer userId = (Integer) request.getArgs()[0];
                                String roleName = (String) request.getArgs()[1];
                                String msg = adminService.changeUserRole(userId, roleName);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                                
                            default:
                                response.setSuccess(false);
                                response.setMessage("Неизвестная команда.");
                                break;
                        }
                    } catch (Exception e) {
                        response.setSuccess(false);
                        response.setMessage("Ошибка обработки запроса: " + e.getMessage());
                    }
                    
                    out.writeObject(response);
                    out.flush();
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
