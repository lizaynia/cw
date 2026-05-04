package com.server;

import com.common.Request;
import com.common.Response;
import com.common.CommandType;
import com.common.dto.*;
import com.common.entity.Flight;
import com.common.entity.Ticket;
import com.common.entity.User;
import com.server.service.*;
import com.server.utils.DtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
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
            
            while (!socket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (!(obj instanceof Request)) {
                        logger.warn("Получен объект неверного типа: {}", obj != null ? obj.getClass().getName() : "null");
                        continue;
                    }
                    
                    Request request = (Request) obj;
                    logger.info("Запрос: {}", request.getCommand());
                    
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
                                String msg = authService.register(login, password);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                            case LOGIN: {
                                String login = (String) request.getArgs()[0];
                                String password = (String) request.getArgs()[1];
                                User user = authService.login(login, password);
                                if (user != null) {
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
                                java.util.List<Flight> flights = dispatcherService.getSchedule();
                                response.setSuccess(true);
                                response.setData(flights); 
                                break;
                            }
                            case BOOK_TICKET: {
                                Integer passengerId = (Integer) request.getArgs()[0];
                                Integer flightId = (Integer) request.getArgs()[1];
                                java.math.BigDecimal price = (java.math.BigDecimal) request.getArgs()[2];
                                String msg = bookingService.bookTicket(passengerId, flightId, price);
                                response.setSuccess(msg.startsWith("Успех"));
                                response.setMessage(msg);
                                break;
                            }
                            case SEARCH_FLIGHTS: {
                                String dep = (String) request.getArgs()[0];
                                String arr = (String) request.getArgs()[1];
                                java.time.LocalDate date = (java.time.LocalDate) request.getArgs()[2];
                                java.util.List<Flight> flights = clientService.searchFlights(dep, arr, date);
                                response.setSuccess(true);
                                response.setData(flights);
                                break;
                            }
                            case GET_TICKET_HISTORY: {
                                Integer passengerId = (Integer) request.getArgs()[0];
                                java.util.List<Ticket> history = clientService.getTicketHistory(passengerId);
                                response.setSuccess(true);
                                response.setData(history);
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
                                String msg = dispatcherService.addFlight(flightNum, depCity, arrCity, time, airplaneId);
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
                            case GET_USERS: {
                                response.setSuccess(true);
                                response.setData(adminService.getAllUsers());
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
