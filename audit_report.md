# 🛫 Аудит проекта cw_spring26 — Аэропорт

> **Последнее обновление:** 2026-05-04  
> **Общая готовность:** `████████░░░░░░░░░░░░` ~47%

---

## Реальная структура пакетов (что есть в коде)

```
src/main/java/
├── com/                          ← АКТИВНЫЙ рабочий код
│   ├── common/
│   │   ├── CommandType.java
│   │   ├── Request.java
│   │   ├── Response.java
│   │   ├── dto/  (FlightDto, TicketDto, UserDto)
│   │   └── entity/ (User, Role, Airplane, Flight, Passenger, Ticket)
│   ├── server/
│   │   ├── Server.java
│   │   ├── ClientHandler.java
│   │   ├── dao/ (UserDao, RoleDao, AirplaneDao, FlightDao, PassengerDao, TicketDao)
│   │   ├── service/ (AuthService, BookingService, AdminService, DispatcherService, ClientService)
│   │   └── utils/ (HibernateUtil, HashUtil, DtoConverter)
│   └── client/
│       ├── ClientApp.java
│       ├── ServerConnection.java
│       └── controller/LoginController.java
│
└── example/                      ← ✅ УДАЛЁН (04.05.2026)
```

---

## Этап 1: Domain Model & Persistence (Hibernate)

| Пункт | Статус | Комментарий |
|---|---|---|
| Настройка Hibernate (hibernate.cfg.xml) | ✅ Реализовано | БД `airport_db`, MySQL, порт 3306 |
| Entity-классы User, Role, Airplane, Flight, Passenger, Ticket | ✅ Реализовано | В пакете `com.common.entity` |
| Mapping & Validation (`@Table`, `@Column`, `@Id`) | ✅ Реализовано | Аннотации присутствуют |
| Связь Flight — @OneToMany — Ticket | ❌ НЕ реализовано | В `Flight.java` нет поля `List<Ticket>` и `@OneToMany` |
| Связь Role — @OneToMany — User | ❌ НЕ реализовано | В `Role.java` нет поля `List<User>` |
| SQL-скрипт инициализации БД | ❌ НЕ реализовано | Файл `.sql` отсутствует |

**Прогресс: `████████░░` 60%**

---

## Этап 2: Общий сетевой слой (Shared Library)

| Пункт | Статус | Комментарий |
|---|---|---|
| Унификация пакетов | ⚠️ Частично | Пакет `com.common`, нет отдельного Maven-модуля Shared |
| Request / Response классы | ✅ Реализовано | `implements Serializable`, `serialVersionUID` есть |
| CommandType Enum | ✅ Реализовано | Все основные команды: LOGIN, REGISTER, BOOK_TICKET, GET_SCHEDULE и др. |
| Serializable для всех entity | ✅ Реализовано | Все entity реализуют `Serializable` |

**Прогресс: `███████░░░` 70%**

---

## Этап 3: Серверная архитектура

| Пункт | Статус | Комментарий |
|---|---|---|
| HibernateUtil (Singleton) | ✅ Реализовано | В пакете `com.server.utils` |
| Импорт HibernateUtil в BookingService | ✅ Исправлено 04.05 | Был `org.example.*`, стал `com.server.utils.*` |
| Generic DAO | ❌ НЕ реализовано | Нет общего интерфейса — каждый DAO дублирует `save/update/findById/findAll` |
| MultiThreadedServer + ClientHandler | ✅ Реализовано | `ExecutorService` пул 10 потоков |
| Command Dispatcher | ✅ Реализовано | `switch(CommandType.valueOf(...))` в `ClientHandler` |
| Хеширование паролей | ✅ Реализовано | `HashUtil` (SHA-256) |
| Дублирующий пакет `example.*` | ✅ Удалён 04.05 | Старые копии классов удалены |

**Прогресс: `████████░░` 83%**

---

## Этап 4: Бизнес-логика (Сервисы)

| Пункт | Статус | Комментарий |
|---|---|---|
| AuthService (вход + регистрация) | ✅ Реализовано | Хэширование пароля, проверка роли |
| FlightService (поиск по дате и городу) | ✅ Реализовано | `ClientService.searchFlights()` |
| BookingService (атомарная покупка) | ✅ Реализовано | Транзакция, проверка мест, откат |
| AdminService (блокировка + логи) | ⚠️ Частично | Смена роли есть, **блокировки пользователей НЕТ**, логов просмотра НЕТ |

**Прогресс: `████████░░` 80%**

---

## Этап 5: Базовый клиент

| Пункт | Статус | Комментарий |
|---|---|---|
| Структура клиента | ✅ Реализовано | `com.client.*`, `ClientApp` запускает JavaFX |
| ServerConnection (Singleton) | ✅ Реализовано | `synchronized getInstance()`, `sendRequest()` |
| currentUser тип UserDto | ✅ Исправлено 04.05 | Был `User`, стал `UserDto` — ClassCastException устранён |
| Base Controller (абстрактный) | ❌ НЕ реализовано | Нет абстрактного контроллера |

**Прогресс: `███████░░░` 75%**

---

## Этап 6: Пользовательский интерфейс

| Пункт | Статус | Комментарий |
|---|---|---|
| LoginView.fxml | ✅ Реализовано | `Login.fxml` с полями логина/пароля |
| LoginController — getRoleName() | ✅ Исправлено 04.05 | Был `getName()`, стал `getRoleName()` |
| AdminDashboard.fxml | ❌ НЕ реализовано | Файл отсутствует |
| FlightCatalog.fxml | ❌ НЕ реализовано | Файл отсутствует |
| Data Binding (ObservableList + TableView) | ❌ НЕ реализовано | |
| Async UI (Task/Service) | ❌ НЕ реализовано | Запросы в LoginController — синхронные |
| Переход между окнами после логина | ❌ НЕ реализовано | Только `System.out.println` |

**Прогресс: `███░░░░░░░` 25%**

---

## Этап 7: Тестирование

| Пункт | Статус |
|---|---|
| Use Case Testing (12 сценариев) | ❌ НЕ реализовано |
| Validation форм | ⚠️ Частично (только «пустые поля» в Login) |
| Exception Handling (сервер выключен) | ❌ НЕ реализовано — приложение упадёт с NPE |

**Прогресс: `█░░░░░░░░░` 10%**

---

## Этап 8: Отчётность и логирование

| Пункт | Статус | Комментарий |
|---|---|---|
| Логирование | ⚠️ Частично | `logback` подключён, `Logger` в Server и ClientHandler |
| PDF/TXT Export | ❌ НЕ реализовано | |
| README | ❌ НЕ реализовано | |

**Прогресс: `██░░░░░░░░` 20%**

---

## 🔴 КРИТИЧЕСКИЕ ОШИБКИ — Статус исправлений

| # | Проблема | Статус |
|---|---|---|
| 1 | Неверный импорт `HibernateUtil` в `BookingService` | ✅ Исправлено 04.05 |
| 2 | `getRole().getName()` → `getRole().getRoleName()` в `DtoConverter` и `LoginController` | ✅ Исправлено 04.05 |
| 3 | `ClassCastException`: `ServerConnection.currentUser` теперь `UserDto` | ✅ Исправлено 04.05 |
| 4 | Дублирующий пакет `example.*` | ✅ Удалён 04.05 |

---

## 🟡 Важные несоответствия требованиям (остаются)

| Проблема | Детали |
|---|---|
| **Нет SQL-скрипта** | Hibernate с `hbm2ddl.auto` не настроен — БД нужно создавать вручную |
| **Нет Generic DAO** | `UserDao`, `FlightDao` и др. повторяют одинаковые методы без общего интерфейса |
| **Login → нет перехода** | После успешного логина только `System.out.println` — нет открытия нового окна |
| **AdminService** | Нет метода блокировки пользователя (`blockUser`), только смена роли |
| **Нет модульности** | Gemini.md описывает многомодульный Maven-проект (Shared/Server/Client) |

---

## Итоговая таблица по этапам

| Этап | До правок | После правок (04.05) |
|---|---|---|
| 1. Domain Model & Hibernate | 🟡 60% | 🟡 60% |
| 2. Сетевой слой | 🟡 70% | 🟡 70% |
| 3. Серверная архитектура | 🟡 75% | 🟢 83% ↑ |
| 4. Бизнес-логика | 🟡 80% | 🟡 80% |
| 5. Базовый клиент | 🟡 65% | 🟡 75% ↑ |
| 6. Интерфейс UI | 🔴 20% | 🔴 25% ↑ |
| 7. Тестирование | 🔴 10% | 🔴 10% |
| 8. Логирование/Отчёт | 🔴 20% | 🔴 20% |
| **Итого** | **~50%** | **~53%** |

---

## Приоритет оставшихся задач

### Срочно (для запуска и демонстрации):
- [ ] 1. Создать SQL-скрипт инициализации БД
- [ ] 2. Реализовать переход между окнами после логина (по роли из `UserDto.getRoleName()`)
- [ ] 3. Добавить `AdminDashboard.fxml` и `FlightCatalog.fxml`

### Важно для зачёта:
- [ ] 4. Добавить Generic DAO интерфейс (`BaseDao<T>`)
- [ ] 5. Добавить абстрактный `BaseController` для JavaFX
- [ ] 6. Добавить блокировку пользователей в `AdminService`
- [ ] 7. Сделать сетевые вызовы асинхронными через `Task<>`
- [ ] 8. Реализовать обработку «Сервер выключен»

### Финиш:
- [ ] 9. PDF/TXT экспорт списка пассажиров
- [ ] 10. Заполнить README
- [ ] 11. Прогнать 12 use-case сценариев
