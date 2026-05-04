## Инфраструктура проекта
- **БД:** MySQL (airport_db)
- **Порт сервера:** 8888 (TCP)
- - **Java Version:** 25 (LTS)
- **Maven Compiler:** source/target 25
- **Технологии:** JavaFX, Hibernate ORM, Sockets

Этап 1: Domain Model & Persistence (Hibernate)
[x] Настройка Hibernate: Базовая конфигурация готова (используем hibernate.cfg.xml из примера как шаблон).

[x] Entity-классы: User, Role, Airplane, Flight, Passenger, Ticket созданы в модуле Shared.

[ ] Mapping & Validation:

Проверить аннотации @Table, @Column и @Id.

Настроить связи: Flight — @OneToMany — Ticket, Role — @OneToMany — User.

[ ] DB Sync: Создать SQL-скрипт инициализации БД (таблицы users, flights и т.д.) для синхронизации с сущностями.

Этап 2: Общий сетевой слой (Shared Library)
[x] Унификация пакетов: Все общие классы перенесены в модуль Shared (пакет by.bsuir.airport.shared).

[x] Base Response/Request: Созданы классы для обмена данными.

[ ] CommandType Enum: Добавить специфичные для аэропорта команды: BUY_TICKET, GET_SCHEDULE, UPDATE_FLIGHT_STATUS, REPAIR_PLANE.

[ ] Serialization: Убедиться, что все классы в Shared реализуют Serializable (важно для ObjectStreams).

Этап 3: Серверная архитектура (Multi-threading & DAO)
[x] HibernateUtil: Реализован паттерн Singleton для SessionFactory.

[ ] Generic DAO: Внедрить базовый интерфейс для CRUD, чтобы не дублировать код в UserDao и FlightDao.

[ ] Server Socket: Реализовать MultiThreadedServer (как в примере Condorcet_Server), где каждый клиент обрабатывается в отдельном ClientHandler.

[ ] Command Dispatcher: Реализовать логику выбора сервиса на основе CommandType (паттерн Strategy или простая фабрика).

[ ] Security: Реализовать метод хеширования паролей в UserService.

Этап 4: Бизнес-логика (Сервисы)
[ ] AuthService: Логика входа и регистрации.

[ ] FlightService: Метод поиска рейсов с фильтрацией по дате и городу.

[ ] BookingService: Критически важно: Атомарная покупка билета (проверка наличия места + создание билета + уменьшение счетчика мест в одной транзакции).

[ ] AdminService: Логика блокировки пользователей и просмотр логов.

Этап 5: Базовый клиент (JavaFX & Network)
[x] Структура проекта: Создан модуль Client с зависимостью от Shared.

[ ] Client Connection: Реализовать класс-синглтон для управления Socket-соединением, чтобы не открывать его заново на каждом окне.

[ ] Base Controller: Создать абстрактный контроллер для JavaFX с методами быстрой отправки запросов на сервер.

Этап 6: Пользовательский интерфейс (UI)
[ ] Scene Builder: Создать FXML-файлы для:

LoginView.fxml (Авторизация).

AdminDashboard.fxml (Таблица пользователей, управление ролями).

FlightCatalog.fxml (Поиск рейсов и кнопка «Купить»).

[ ] Data Binding: Привязка списков ObservableList<Flight> к TableView.

[ ] Async UI: Выполнение сетевых запросов в Service<T> или Task, чтобы интерфейс не зависал при ожидании ответа сервера.

Этап 7: Тестирование и Техусловия
[ ] Use Case Testing: Прогнать 12 сценариев (например: «Попытка купить билет, если места закончились»).

[ ] Validation: Проверка на пустые поля в формах и корректность email/паспорта.

[ ] Exception Handling: Обработка ситуации «Сервер выключен» на стороне клиента.

Этап 8: Отчетность и Логирование (Финиш)
[ ] Server Logging: Добавить библиотеку log4j или простой вывод в файл server.log.

[ ] PDF/TXT Export: Реализовать генерацию ведомости пассажиров на конкретный рейс (использовать FileWriter или библиотеку iText).

[ ] README: Заполнить файл согласно контекстному плану.