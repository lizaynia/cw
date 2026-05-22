package com.client;

import com.common.CommandType;
import com.common.Request;
import com.common.Response;
import com.common.dto.CityDto;
import com.common.dto.FlightDto;
import com.common.dto.TicketDto;
import com.common.dto.UserDto;
import com.common.entity.Airplane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   АВТОМАТИЗИРОВАННАЯ СИСТЕМА УПРАВЛЕНИЯ АЭРОПОРТОМ");
        System.out.println("         (КОНСОЛЬНЫЙ КЛИЕНТ ДЛЯ ЗАЩИТЫ КР)        ");
        System.out.println("=================================================");

        ServerConnection conn = ServerConnection.getInstance();

        boolean running = true;
        while (running) {
            UserDto currentUser = conn.getCurrentUser();
            if (currentUser == null) {
                running = showGuestMenu(conn);
            } else {
                switch (currentUser.getRoleName()) {
                    case "CLIENT":
                        running = showClientMenu(conn);
                        break;
                    case "DISPATCHER":
                        running = showDispatcherMenu(conn);
                        break;
                    case "ADMIN":
                        running = showAdminMenu(conn);
                        break;
                    default:
                        System.out.println("Неизвестная роль: " + currentUser.getRoleName());
                        conn.setCurrentUser(null);
                        break;
                }
            }
        }

        conn.close();
        System.out.println("Соединение закрыто. Выход из программы.");
    }

    private static boolean showGuestMenu(ServerConnection conn) {
        System.out.println("\n--- РЕЖИМ ГОСТЯ (НЕАВТОРИЗОВАННЫЙ ПОЛЬЗОВАТЕЛЬ) ---");
        System.out.println("1. Просмотреть расписание рейсов");
        System.out.println("2. Простой поиск рейсов");
        System.out.println("3. Расширенный поиск рейсов");
        System.out.println("4. Авторизация (Вход)");
        System.out.println("5. Регистрация");
        System.out.println("0. Выход");
        System.out.print("Выберите пункт меню: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                viewSchedule(conn);
                break;
            case "2":
                simpleSearch(conn);
                break;
            case "3":
                advancedSearch(conn);
                break;
            case "4":
                handleLogin(conn);
                break;
            case "5":
                handleRegister(conn);
                break;
            case "0":
                return false;
            default:
                System.out.println("Неверный ввод. Попробуйте еще раз.");
        }
        return true;
    }

    private static boolean showClientMenu(ServerConnection conn) {
        UserDto user = conn.getCurrentUser();
        System.out.println("\n--- МЕНЮ КЛИЕНТА (Логин: " + user.getLogin() + ") ---");
        System.out.println("1. Просмотреть расписание рейсов");
        System.out.println("2. Простой поиск рейсов");
        System.out.println("3. Расширенный поиск рейсов");
        System.out.println("4. Купить билет");
        System.out.println("5. История моих билетов");
        System.out.println("6. Личный кабинет (смена пароля)");
        System.out.println("7. Редактировать профиль (ФИО и паспорт)");
        System.out.println("8. Выход из аккаунта");
        System.out.println("0. Выйти из программы");
        System.out.print("Выберите пункт меню: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                viewSchedule(conn);
                break;
            case "2":
                simpleSearch(conn);
                break;
            case "3":
                advancedSearch(conn);
                break;
            case "4":
                bookTicket(conn);
                break;
            case "5":
                viewTicketHistory(conn);
                break;
            case "6":
                changePassword(conn);
                break;
            case "7":
                updateProfileInfo(conn);
                break;
            case "8":
                conn.setCurrentUser(null);
                System.out.println("Вы успешно вышли из аккаунта.");
                break;
            case "0":
                return false;
            default:
                System.out.println("Неверный ввод. Попробуйте еще раз.");
        }
        return true;
    }

    private static boolean showDispatcherMenu(ServerConnection conn) {
        UserDto user = conn.getCurrentUser();
        System.out.println("\n--- МЕНЮ ДИСПЕТЧЕРА (Логин: " + user.getLogin() + ") ---");
        System.out.println("1. Просмотреть расписание рейсов");
        System.out.println("2. Добавить новый рейс");
        System.out.println("3. Редактировать рейс");
        System.out.println("4. Удалить рейс");
        System.out.println("5. Просмотреть авиапарк");
        System.out.println("6. Добавить самолёт в авиапарк");
        System.out.println("7. Выйти из аккаунта");
        System.out.println("0. Выйти из программы");
        System.out.print("Выберите пункт меню: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                viewSchedule(conn);
                break;
            case "2":
                addFlight(conn);
                break;
            case "3":
                editFlight(conn);
                break;
            case "4":
                deleteFlight(conn);
                break;
            case "5":
                viewAirplanes(conn);
                break;
            case "6":
                addAirplane(conn);
                break;
            case "7":
                conn.setCurrentUser(null);
                System.out.println("Вы успешно вышли из аккаунта.");
                break;
            case "0":
                return false;
            default:
                System.out.println("Неверный ввод. Попробуйте еще раз.");
        }
        return true;
    }

    private static boolean showAdminMenu(ServerConnection conn) {
        UserDto user = conn.getCurrentUser();
        System.out.println("\n--- МЕНЮ АДМИНИСТРАТОРА (Логин: " + user.getLogin() + ") ---");
        System.out.println("1. Просмотреть расписание рейсов");
        System.out.println("2. Добавить новый рейс (CRUD)");
        System.out.println("3. Редактировать рейс (CRUD)");
        System.out.println("4. Удалить рейс (CRUD)");
        System.out.println("5. Просмотреть авиапарк");
        System.out.println("6. Добавить самолёт");
        System.out.println("7. Изменить статус обслуживания самолёта");
        System.out.println("8. Просмотреть список пользователей");
        System.out.println("9. Изменить роль пользователя");
        System.out.println("10. Блокировка / Разблокировка пользователя");
        System.out.println("11. Просмотреть статистику аэропорта");
        System.out.println("12. Выйти из аккаунта");
        System.out.println("0. Выйти из программы");
        System.out.print("Выберите пункт меню: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                viewSchedule(conn);
                break;
            case "2":
                addFlight(conn);
                break;
            case "3":
                editFlight(conn);
                break;
            case "4":
                deleteFlight(conn);
                break;
            case "5":
                viewAirplanes(conn);
                break;
            case "6":
                addAirplane(conn);
                break;
            case "7":
                updateAirplaneStatus(conn);
                break;
            case "8":
                viewUsers(conn);
                break;
            case "9":
                changeUserRole(conn);
                break;
            case "10":
                toggleUserBlock(conn);
                break;
            case "11":
                viewStatistics(conn);
                break;
            case "12":
                conn.setCurrentUser(null);
                System.out.println("Вы успешно вышли из аккаунта.");
                break;
            case "0":
                return false;
            default:
                System.out.println("Неверный ввод. Попробуйте еще раз.");
        }
        return true;
    }

    // === ОБЩИЕ МЕТОДЫ ===

    private static void viewSchedule(ServerConnection conn) {
        System.out.println("\n--- РАСПИСАНИЕ РЕЙСОВ ---");
        Request request = new Request(CommandType.GET_SCHEDULE.name());
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка при получении расписания: " + response.getMessage());
            return;
        }

        List<FlightDto> flights = (List<FlightDto>) response.getData();
        if (flights == null || flights.isEmpty()) {
            System.out.println("Рейсов не запланировано.");
            return;
        }

        printFlightsTable(flights);
    }

    private static void printFlightsTable(List<FlightDto> flights) {
        System.out.printf("%-5s | %-10s | %-30s | %-16s | %-20s | %-10s | %-10s\n",
                "ID", "Номер", "Маршрут", "Время вылета", "Самолёт", "Мест", "Цена ($)");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        for (FlightDto f : flights) {
            System.out.printf("%-5d | %-10s | %-30s | %-16s | %-20s | %-10d | %-10.2f\n",
                    f.getId(),
                    f.getFlightNumber(),
                    f.getRoute(),
                    f.getDepartureTime().format(dateTimeFormatter),
                    f.getAirplaneModel(),
                    f.getAvailableSeats(),
                    f.getBasePrice());
        }
    }

    private static void simpleSearch(ServerConnection conn) {
        System.out.println("\n--- ПРОСТОЙ ПОИСК РЕЙСОВ ---");
        System.out.print("Город отправления: ");
        String dep = scanner.nextLine().trim();
        System.out.print("Город прибытия: ");
        String arr = scanner.nextLine().trim();
        System.out.print("Дата вылета (ГГГГ-ММ-ДД, или пусто): ");
        String dateStr = scanner.nextLine().trim();

        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты! Поиск без даты.");
            }
        }

        Request request = new Request(CommandType.SEARCH_FLIGHTS.name(), dep, arr, date);
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка поиска: " + response.getMessage());
            return;
        }

        List<FlightDto> flights = (List<FlightDto>) response.getData();
        if (flights == null || flights.isEmpty()) {
            System.out.println("Рейсов не найдено.");
            return;
        }

        printFlightsTable(flights);
    }

    private static void advancedSearch(ServerConnection conn) {
        System.out.println("\n--- РАСШИРЕННЫЙ ПОИСК РЕЙСОВ ---");
        System.out.print("Номер рейса (или пусто): ");
        String flightNum = scanner.nextLine().trim();
        System.out.print("Город отправления (или пусто): ");
        String dep = scanner.nextLine().trim();
        System.out.print("Город прибытия (или пусто): ");
        String arr = scanner.nextLine().trim();
        System.out.print("Дата вылета (ГГГГ-ММ-ДД, или пусто): ");
        String dateStr = scanner.nextLine().trim();
        System.out.print("Максимальная цена (или пусто): ");
        String priceStr = scanner.nextLine().trim();

        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты.");
            }
        }

        Double maxPrice = null;
        if (!priceStr.isEmpty()) {
            try {
                maxPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат цены.");
            }
        }

        Request request = new Request(CommandType.ADVANCED_SEARCH_FLIGHTS.name(),
                flightNum, dep, arr, date, maxPrice != null ? maxPrice : 999999.0, false);
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка поиска: " + response.getMessage());
            return;
        }

        List<FlightDto> flights = (List<FlightDto>) response.getData();
        if (flights == null || flights.isEmpty()) {
            System.out.println("Рейсов не найдено.");
            return;
        }

        printFlightsTable(flights);
    }

    private static void handleLogin(ServerConnection conn) {
        System.out.println("\n--- АВТОРИЗАЦИЯ ---");
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        Request request = new Request(CommandType.LOGIN.name(), login, password);
        Response response = conn.sendRequest(request);

        System.out.println(response.getMessage());
        if (response.isSuccess()) {
            conn.setCurrentUser((UserDto) response.getData());
        }
    }

    private static void handleRegister(ServerConnection conn) {
        System.out.println("\n--- РЕГИСТРАЦИЯ ---");
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();
        System.out.print("Имя: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Фамилия: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Номер паспорта: ");
        String passport = scanner.nextLine().trim();

        if (login.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || passport.isEmpty()) {
            System.out.println("Все поля обязательны к заполнению!");
            return;
        }

        Request request = new Request(CommandType.REGISTER.name(), login, password, firstName, lastName, passport);
        Response response = conn.sendRequest(request);

        System.out.println(response.getMessage());
    }

    // === КЛИЕНТСКИЕ МЕТОДЫ ===

    private static void bookTicket(ServerConnection conn) {
        System.out.println("\n--- ПОКУПКА БИЛЕТА ---");
        System.out.print("Введите ID рейса: ");
        String flightIdStr = scanner.nextLine().trim();
        Integer flightId;
        try {
            flightId = Integer.parseInt(flightIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID рейса!");
            return;
        }

        // Получаем занятые места
        Request occupiedReq = new Request(CommandType.GET_OCCUPIED_SEATS.name(), flightId);
        Response occupiedResp = conn.sendRequest(occupiedReq);
        if (!occupiedResp.isSuccess()) {
            System.out.println("Не удалось загрузить схему мест: " + occupiedResp.getMessage());
            return;
        }
        List<String> occupied = (List<String>) occupiedResp.getData();
        System.out.println("Занятые места на этом рейсе: " + (occupied != null ? occupied : "нет"));
        System.out.print("Введите желаемое место (например, 12A, 1B): ");
        String seat = scanner.nextLine().trim();

        if (seat.isEmpty()) {
            System.out.println("Место не введено!");
            return;
        }

        Request bookReq = new Request(CommandType.BOOK_TICKET.name(),
                conn.getCurrentUser().getId(), flightId, seat);
        Response bookResp = conn.sendRequest(bookReq);
        System.out.println(bookResp.getMessage());
    }

    private static void viewTicketHistory(ServerConnection conn) {
        System.out.println("\n--- ИСТОРИЯ МОИХ БИЛЕТОВ ---");
        Request request = new Request(CommandType.GET_TICKET_HISTORY.name(), conn.getCurrentUser().getId());
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Не удалось получить историю: " + response.getMessage());
            return;
        }

        List<TicketDto> tickets = (List<TicketDto>) response.getData();
        if (tickets == null || tickets.isEmpty()) {
            System.out.println("Вы еще не покупали билетов.");
            return;
        }

        System.out.printf("%-5s | %-10s | %-25s | %-10s | %-10s | %-10s | %-12s\n",
                "ID", "Рейс", "Маршрут", "Дата", "Пассажир", "Место", "Статус");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (TicketDto t : tickets) {
            System.out.printf("%-5d | %-10s | %-25s | %-10s | %-10s | %-10s | %-12s\n",
                    t.getId(),
                    t.getFlightNumber(),
                    t.getRoute(),
                    t.getFlightDate() != null ? t.getFlightDate() : "N/A",
                    t.getPassengerName(),
                    t.getSeatNumber(),
                    t.getStatus());
        }
    }

    private static void changePassword(ServerConnection conn) {
        System.out.println("\n--- СМЕНА ПАРОЛЯ ---");
        System.out.print("Введите новый пароль: ");
        String newPassword = scanner.nextLine().trim();

        if (newPassword.isEmpty()) {
            System.out.println("Пароль не может быть пустым!");
            return;
        }

        Request request = new Request(CommandType.UPDATE_PROFILE.name(), conn.getCurrentUser().getId(), newPassword);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void updateProfileInfo(ServerConnection conn) {
        System.out.println("\n--- РЕДАКТИРОВАНИЕ ПРОФИЛЯ ---");
        System.out.print("Введите новые имя и фамилию (через пробел): ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Введите новый паспорт: ");
        String passport = scanner.nextLine().trim();

        if (fullName.isEmpty() || passport.isEmpty()) {
            System.out.println("Поля не могут быть пустыми!");
            return;
        }

        Request request = new Request(CommandType.UPDATE_PROFILE_INFO.name(),
                conn.getCurrentUser().getId(), fullName, passport);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
        if (response.isSuccess()) {
            conn.getCurrentUser().setFullName(fullName);
            conn.getCurrentUser().setPassportNumber(passport);
        }
    }

    // === ДИСПОТЧЕРСКИЕ МЕТОДЫ ===

    private static void viewAirplanes(ServerConnection conn) {
        System.out.println("\n--- АВИАПАРК ---");
        Request request = new Request(CommandType.GET_AIRPLANES.name());
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка при получении авиапарка: " + response.getMessage());
            return;
        }

        List<Airplane> airplanes = (List<Airplane>) response.getData();
        if (airplanes == null || airplanes.isEmpty()) {
            System.out.println("Авиапарк пуст.");
            return;
        }

        System.out.printf("%-5s | %-25s | %-10s | %-15s\n", "ID", "Модель", "Вместимость", "Статус");
        System.out.println("----------------------------------------------------------------");
        for (Airplane a : airplanes) {
            System.out.printf("%-5d | %-25s | %-10d | %-15s\n",
                    a.getId(), a.getModel(), a.getCapacity(), a.getStatus());
        }
    }

    private static void addAirplane(ServerConnection conn) {
        System.out.println("\n--- ДОБАВЛЕНИЕ САМОЛЁТА ---");
        System.out.print("Модель: ");
        String model = scanner.nextLine().trim();
        System.out.print("Вместимость: ");
        String capacityStr = scanner.nextLine().trim();
        Integer capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат вместимости!");
            return;
        }

        if (model.isEmpty() || capacity <= 0) {
            System.out.println("Неверные данные!");
            return;
        }

        Request request = new Request(CommandType.ADD_AIRPLANE.name(), model, capacity);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void addFlight(ServerConnection conn) {
        System.out.println("\n--- ДОБАВЛЕНИЕ РЕЙСА ---");
        System.out.print("Номер рейса (например, MSQ-123): ");
        String flightNum = scanner.nextLine().trim();
        System.out.print("Город отправления: ");
        String dep = scanner.nextLine().trim();
        System.out.print("Город прибытия: ");
        String arr = scanner.nextLine().trim();
        System.out.print("Время вылета (ГГГГ-ММ-ДД ЧЧ:ММ): ");
        String timeStr = scanner.nextLine().trim();

        LocalDateTime time;
        try {
            time = LocalDateTime.parse(timeStr, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат времени!");
            return;
        }

        System.out.print("ID самолёта: ");
        String planeIdStr = scanner.nextLine().trim();
        Integer planeId;
        try {
            planeId = Integer.parseInt(planeIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID самолёта!");
            return;
        }

        System.out.print("Базовая цена билета ($): ");
        String priceStr = scanner.nextLine().trim();
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат цены!");
            return;
        }

        Request request = new Request(CommandType.ADD_FLIGHT.name(), flightNum, dep, arr, time, planeId, price);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void editFlight(ServerConnection conn) {
        System.out.println("\n--- РЕДАКТИРОВАНИЕ РЕЙСА ---");
        System.out.print("Введите ID редактируемого рейса: ");
        String flightIdStr = scanner.nextLine().trim();
        Integer flightId;
        try {
            flightId = Integer.parseInt(flightIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID рейса!");
            return;
        }

        System.out.print("Новый номер рейса: ");
        String flightNum = scanner.nextLine().trim();
        System.out.print("Новый город отправления: ");
        String dep = scanner.nextLine().trim();
        System.out.print("Новый город прибытия: ");
        String arr = scanner.nextLine().trim();
        System.out.print("Новое время вылета (ГГГГ-ММ-ДД ЧЧ:ММ): ");
        String timeStr = scanner.nextLine().trim();

        LocalDateTime time;
        try {
            time = LocalDateTime.parse(timeStr, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат времени!");
            return;
        }

        System.out.print("Новый ID самолёта: ");
        String planeIdStr = scanner.nextLine().trim();
        Integer planeId;
        try {
            planeId = Integer.parseInt(planeIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID самолёта!");
            return;
        }

        System.out.print("Новая базовая цена билета ($): ");
        String priceStr = scanner.nextLine().trim();
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат цены!");
            return;
        }

        Request request = new Request(CommandType.UPDATE_FLIGHT.name(), flightId, flightNum, dep, arr, time, planeId, price);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void deleteFlight(ServerConnection conn) {
        System.out.println("\n--- УДАЛЕНИЕ РЕЙСА ---");
        System.out.print("Введите ID рейса для удаления: ");
        String idStr = scanner.nextLine().trim();
        Integer id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID рейса!");
            return;
        }

        Request request = new Request(CommandType.DELETE_FLIGHT.name(), id);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    // === АДМИНИСТРАТИВНЫЕ МЕТОДЫ ===

    private static void updateAirplaneStatus(ServerConnection conn) {
        System.out.println("\n--- ИЗМЕНЕНИЕ СТАТУСА САМОЛЁТА ---");
        System.out.print("Введите ID самолёта: ");
        String planeIdStr = scanner.nextLine().trim();
        Integer planeId;
        try {
            planeId = Integer.parseInt(planeIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID самолёта!");
            return;
        }

        System.out.println("Выберите новый статус обслуживания:");
        System.out.println("1 - ACTIVE (Готов к полетам)");
        System.out.println("2 - MAINTENANCE (Техническое обслуживание)");
        System.out.println("3 - DECOMMISSIONED (Выведен из эксплуатации)");
        System.out.print("Выбор: ");
        String statusChoice = scanner.nextLine().trim();
        Airplane.AirplaneStatus status;
        switch (statusChoice) {
            case "1":
                status = Airplane.AirplaneStatus.ACTIVE;
                break;
            case "2":
                status = Airplane.AirplaneStatus.MAINTENANCE;
                break;
            case "3":
                status = Airplane.AirplaneStatus.DECOMMISSIONED;
                break;
            default:
                System.out.println("Неверный выбор статуса.");
                return;
        }

        Request request = new Request(CommandType.UPDATE_AIRPLANE_STATUS.name(), planeId, status);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void viewUsers(ServerConnection conn) {
        System.out.println("\n--- СПИСОК ПОЛЬЗОВАТЕЛЕЙ ---");
        Request request = new Request(CommandType.GET_USERS.name());
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка получения списка пользователей: " + response.getMessage());
            return;
        }

        List<UserDto> users = (List<UserDto>) response.getData();
        if (users == null || users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
            return;
        }

        System.out.printf("%-5s | %-15s | %-15s | %-15s\n", "ID", "Логин", "Роль", "Статус");
        System.out.println("----------------------------------------------------------------");
        for (UserDto u : users) {
            System.out.printf("%-5d | %-15s | %-15s | %-15s\n",
                    u.getId(), u.getLogin(), u.getRoleName(), u.isBlocked() ? "ЗАБЛОКИРОВАН" : "АКТИВЕН");
        }
    }

    private static void changeUserRole(ServerConnection conn) {
        System.out.println("\n--- ИЗМЕНЕНИЕ РОЛИ ПОЛЬЗОВАТЕЛЯ ---");
        System.out.print("Введите ID пользователя: ");
        String userIdStr = scanner.nextLine().trim();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID!");
            return;
        }

        System.out.print("Введите новую роль (CLIENT, DISPATCHER, ADMIN): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (!role.equals("CLIENT") && !role.equals("DISPATCHER") && !role.equals("ADMIN")) {
            System.out.println("Такой роли не существует!");
            return;
        }

        Request request = new Request(CommandType.CHANGE_ROLE.name(), userId, role);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void toggleUserBlock(ServerConnection conn) {
        System.out.println("\n--- БЛОКИРОВКА / РАЗБЛОКИРОВКА ПОЛЬЗОВАТЕЛЯ ---");
        System.out.print("Введите ID пользователя: ");
        String userIdStr = scanner.nextLine().trim();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID!");
            return;
        }

        System.out.print("1 - Заблокировать, 2 - Разблокировать: ");
        String actChoice = scanner.nextLine().trim();
        boolean block;
        if (actChoice.equals("1")) {
            block = true;
        } else if (actChoice.equals("2")) {
            block = false;
        } else {
            System.out.println("Неверный выбор действия.");
            return;
        }

        Request request = new Request(CommandType.BLOCK_USER.name(), userId, block);
        Response response = conn.sendRequest(request);
        System.out.println(response.getMessage());
    }

    private static void viewStatistics(ServerConnection conn) {
        System.out.println("\n--- СТАТИСТИКА АЭРОПОРТА ---");
        Request request = new Request(CommandType.GET_STATISTICS.name());
        Response response = conn.sendRequest(request);

        if (!response.isSuccess()) {
            System.out.println("Ошибка при получении статистики: " + response.getMessage());
            return;
        }

        Map<String, Long> stats = (Map<String, Long>) response.getData();
        if (stats == null) {
            System.out.println("Статистика недоступна.");
            return;
        }

        System.out.println("Зарегистрированных пользователей: " + stats.getOrDefault("users", 0L));
        System.out.println("Всего самолётов в авиапарке: " + stats.getOrDefault("airplanes", 0L));
    }
}
