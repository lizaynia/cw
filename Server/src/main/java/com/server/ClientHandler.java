package com.server;

import com.common.Request;
import com.common.Response;
import com.common.CommandType;
import com.common.dto.*;
import com.common.entity.Flight;
import com.common.entity.Passenger;
import com.common.entity.Ticket;
import com.common.entity.User;
import com.server.dao.PassengerDao;
import com.server.service.*;
import com.server.utils.DtoConverter;
import com.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User currentUser;  // ✅ ДОБАВЛЕНО

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole().getRoleName());
    }

    private boolean isDispatcher() {
        return currentUser != null && "DISPATCHER".equals(currentUser.getRole().getRoleName());
    }

    private boolean isClient() {
        return currentUser != null && "CLIENT".equals(currentUser.getRole().getRoleName());
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            logger.info("Клиент подключен: {}", socket.getInetAddress().getHostAddress());

            BookingService bookingService = new BookingService();
            AuthService authService = new AuthService();
            DispatcherService dispatcherService = new DispatcherService();
            AdminService adminService = new AdminService();
            ClientService clientService = new ClientService();
            PassengerDao passengerDao = new PassengerDao();

            while (!socket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (!(obj instanceof Request request)) {
                        logger.warn("Получен объект неверного типа");
                        continue;
                    }

                    logger.info("Запрос: {} от пользователя: {}", request.getCommand(),
                            currentUser != null ? currentUser.getLogin() : "неавторизован");

                    Response response = new Response();

                    try {
                        CommandType command;
                        try {
                            command = CommandType.valueOf(request.getCommand());
                        } catch (IllegalArgumentException e) {
                            response.setSuccess(false);
                            response.setMessage("Неизвестная команда: " + request.getCommand());
                            out.writeObject(response);
                            out.flush();
                            continue;
                        }

                        switch (command) {
                            case REGISTER: {
                                String login = (String) request.getArgs()[0];
                                String password = (String) request.getArgs()[1];
                                String firstName = (String) request.getArgs()[2];
                                String lastName = (String) request.getArgs()[3];
                                String passport = (String) request.getArgs()[4];
                                String msg = authService.register(login, password, firstName, lastName, passport);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }

                            case LOGIN: {
                                String login = (String) request.getArgs()[0];
                                String password = (String) request.getArgs()[1];
                                User user = authService.login(login, password);
                                if (user != null) {
                                    this.currentUser = user;  // ✅ СОХРАНЯЕМ ПОЛЬЗОВАТЕЛЯ
                                    response.setSuccess(true);
                                    response.setMessage("Успешный вход");
                                    response.setData(DtoConverter.toDto(user));
                                } else {
                                    response.setSuccess(false);
                                    response.setMessage("Неверный логин или пароль");
                                }
                                break;
                            }

                            case GET_SCHEDULE: {
                                // ✅ Доступно всем (даже неавторизованным)
                                List<Flight> flights = dispatcherService.getSchedule();
                                java.util.Map<Integer, Long> bookedCounts = new java.util.HashMap<>();
                                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                                    for (Flight flight : flights) {
                                        Long count = session.createQuery(
                                                        "select count(t) from Ticket t where t.flight.id = :flightId", Long.class)
                                                .setParameter("flightId", flight.getId())
                                                .uniqueResult();
                                        bookedCounts.put(flight.getId(), count != null ? count : 0L);
                                    }
                                }
                                response.setSuccess(true);
                                response.setData(DtoConverter.toFlightDtoList(flights, bookedCounts));
                                break;
                            }

                            case UPDATE_PROFILE: {
                                // ✅ Только авторизованный пользователь
                                if (!isAuthenticated()) {
                                    response.setSuccess(false);
                                    response.setMessage("Требуется авторизация");
                                    break;
                                }
                                Integer userId = (Integer) request.getArgs()[0];
                                String newPassword = (String) request.getArgs()[1];
                                // Проверяем, что пользователь меняет свой пароль
                                if (!currentUser.getId().equals(userId)) {
                                    response.setSuccess(false);
                                    response.setMessage("Нельзя изменить пароль другого пользователя");
                                    break;
                                }
                                String msg = authService.updatePassword(userId, newPassword);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }

                            case BOOK_TICKET: {
                                // ✅ Только авторизованный пользователь
                                if (!isAuthenticated()) {
                                    response.setSuccess(false);
                                    response.setMessage("Требуется авторизация");
                                    break;
                                }
                                Integer userId = (Integer) request.getArgs()[0];
                                Integer flightId = (Integer) request.getArgs()[1];
                                String seatNumber = (String) request.getArgs()[2];

                                // Проверяем, что пользователь покупает билет для себя
                                if (!currentUser.getId().equals(userId)) {
                                    response.setSuccess(false);
                                    response.setMessage("Нельзя купить билет для другого пользователя");
                                    break;
                                }

                                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                                    Passenger passenger = passengerDao.findByUserId(session, userId);
                                    if (passenger == null) {
                                        response.setSuccess(false);
                                        response.setMessage("Ошибка: Профиль пассажира не найден.");
                                    } else {
                                        String msg = bookingService.bookTicket(passenger.getId(), flightId, seatNumber);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                    }
                                } catch (Exception e) {
                                    response.setSuccess(false);
                                    response.setMessage("Ошибка: " + e.getMessage());
                                }
                                break;
                            }

                            case GET_OCCUPIED_SEATS: {
                                // ✅ Доступно всем
                                Integer flightId = (Integer) request.getArgs()[0];
                                List<String> occupiedSeats = bookingService.getOccupiedSeats(flightId);
                                response.setSuccess(true);
                                response.setData(occupiedSeats);
                                break;
                            }

                            case SEARCH_FLIGHTS: {
                                // ✅ Доступно всем
                                String dep = (String) request.getArgs()[0];
                                String arr = (String) request.getArgs()[1];
                                java.time.LocalDate date = (java.time.LocalDate) request.getArgs()[2];
                                List<Flight> flights = clientService.searchFlights(dep, arr, date);

                                java.util.Map<Integer, Long> bookedCounts = new java.util.HashMap<>();
                                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                                    for (Flight flight : flights) {
                                        Long count = session.createQuery(
                                                        "select count(t) from Ticket t where t.flight.id = :flightId", Long.class)
                                                .setParameter("flightId", flight.getId())
                                                .uniqueResult();
                                        bookedCounts.put(flight.getId(), count != null ? count : 0L);
                                    }
                                }
                                response.setSuccess(true);
                                response.setData(DtoConverter.toFlightDtoList(flights, bookedCounts));
                                break;
                            }

                            case GET_TICKET_HISTORY: {
                                // ✅ Только авторизованный пользователь
                                if (!isAuthenticated()) {
                                    response.setSuccess(false);
                                    response.setMessage("Требуется авторизация");
                                    break;
                                }
                                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                                    Integer userId = (Integer) request.getArgs()[0];
                                    // Проверяем, что пользователь смотрит свою историю
                                    if (!currentUser.getId().equals(userId)) {
                                        response.setSuccess(false);
                                        response.setMessage("Нельзя просматривать историю другого пользователя");
                                        break;
                                    }
                                    Passenger p = passengerDao.findByUserId(session, userId);
                                    if (p == null) {
                                        response.setSuccess(false);
                                        response.setMessage("Ошибка: Профиль пассажира не найден.");
                                    } else {
                                        List<Ticket> history = clientService.getTicketHistory(p.getId());
                                        response.setSuccess(true);
                                        response.setData(DtoConverter.toTicketDtoList(history));
                                    }
                                }
                                break;
                            }

                            // === ДИСПЕТЧЕРСКИЕ КОМАНДЫ (только для DISPATCHER и ADMIN) ===
                            case GET_AIRPLANES:
                            case ADD_FLIGHT:
                            case UPDATE_FLIGHT:
                            case DELETE_FLIGHT:
                            case ADD_AIRPLANE: {
                                if (!isDispatcher() && !isAdmin()) {
                                    response.setSuccess(false);
                                    response.setMessage("Доступ запрещен. Требуется роль DISPATCHER или ADMIN");
                                    break;
                                }

                                switch (command) {
                                    case GET_AIRPLANES: {
                                        response.setSuccess(true);
                                        response.setData(dispatcherService.getAirplanes());
                                        break;
                                    }
                                    case ADD_FLIGHT: {
                                        String flightNum = (String) request.getArgs()[0];
                                        String depCity = (String) request.getArgs()[1];
                                        String arrCity = (String) request.getArgs()[2];
                                        java.time.LocalDateTime time = (java.time.LocalDateTime) request.getArgs()[3];
                                        Integer airplaneId = (Integer) request.getArgs()[4];
                                        java.math.BigDecimal price = null;
                                        if (request.getArgs().length > 5 && request.getArgs()[5] != null) {
                                            if (request.getArgs()[5] instanceof Double) {
                                                price = java.math.BigDecimal.valueOf((Double) request.getArgs()[5]);
                                            } else if (request.getArgs()[5] instanceof java.math.BigDecimal) {
                                                price = (java.math.BigDecimal) request.getArgs()[5];
                                            }
                                        }
                                        String msg = dispatcherService.addFlight(flightNum, depCity, arrCity, time, airplaneId, price);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case UPDATE_FLIGHT: {
                                        Integer flightId = (Integer) request.getArgs()[0];
                                        String flightNum = (String) request.getArgs()[1];
                                        String depCity = (String) request.getArgs()[2];
                                        String arrCity = (String) request.getArgs()[3];
                                        java.time.LocalDateTime time = (java.time.LocalDateTime) request.getArgs()[4];
                                        Integer airplaneId = (Integer) request.getArgs()[5];
                                        java.math.BigDecimal price = null;
                                        if (request.getArgs().length > 6 && request.getArgs()[6] != null) {
                                            if (request.getArgs()[6] instanceof Double) {
                                                price = java.math.BigDecimal.valueOf((Double) request.getArgs()[6]);
                                            } else if (request.getArgs()[6] instanceof java.math.BigDecimal) {
                                                price = (java.math.BigDecimal) request.getArgs()[6];
                                            }
                                        }
                                        String msg = dispatcherService.updateFlight(flightId, flightNum, depCity, arrCity, time, airplaneId, price);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case DELETE_FLIGHT: {
                                        Integer flightId = (Integer) request.getArgs()[0];
                                        String msg = dispatcherService.deleteFlight(flightId);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case ADD_AIRPLANE: {
                                        String model = (String) request.getArgs()[0];
                                        Integer capacity = (Integer) request.getArgs()[1];
                                        String msg = dispatcherService.addAirplane(model, capacity);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                }
                                break;
                            }

                            // === АДМИНИСТРАТОРСКИЕ КОМАНДЫ (только для ADMIN) ===
                            case GET_USERS:
                            case CHANGE_ROLE:
                            case BLOCK_USER:
                            case UPDATE_AIRPLANE_STATUS:
                            case GET_STATISTICS: {
                                if (!isAdmin()) {
                                    response.setSuccess(false);
                                    response.setMessage("Доступ запрещен. Требуется роль ADMIN");
                                    break;
                                }

                                switch (command) {
                                    case GET_USERS: {
                                        response.setSuccess(true);
                                        response.setData(DtoConverter.toUserDtoList(adminService.getAllUsers()));
                                        break;
                                    }
                                    case CHANGE_ROLE: {
                                        Integer userId = (Integer) request.getArgs()[0];
                                        String roleName = (String) request.getArgs()[1];
                                        String msg = adminService.changeUserRole(userId, roleName);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case BLOCK_USER: {
                                        Integer userId = (Integer) request.getArgs()[0];
                                        boolean block = (Boolean) request.getArgs()[1];
                                        String msg = adminService.toggleUserBlock(userId, block);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case UPDATE_AIRPLANE_STATUS: {
                                        Integer id = (Integer) request.getArgs()[0];
                                        com.common.entity.Airplane.AirplaneStatus status = (com.common.entity.Airplane.AirplaneStatus) request.getArgs()[1];
                                        String msg = adminService.updateAirplaneStatus(id, status);
                                        response.setSuccess(msg.startsWith("Успех"));
                                        response.setMessage(msg);
                                        break;
                                    }
                                    case GET_STATISTICS: {
                                        response.setSuccess(true);
                                        response.setData(adminService.getStatistics());
                                        break;
                                    }
                                }
                                break;
                            }

                            default:
                                response.setSuccess(false);
                                response.setMessage("Команда " + command + " еще не реализована");
                                break;
                        }
                    } catch (Exception e) {
                        logger.error("Ошибка бизнес-логики: ", e);
                        response.setSuccess(false);
                        response.setMessage("Ошибка на сервере: " + e.getMessage());
                    }

                    out.writeObject(response);
                    out.flush();
                } catch (IOException | ClassNotFoundException e) {
                    logger.info("Клиент отключился.");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка ClientHandler: ", e);
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
            logger.error("Ошибка при закрытии сокетов: ", e);
        }
    }
}