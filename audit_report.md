# 🛫 Аудит проекта cw_spring26 — Аэропорт

> **Последнее обновление:** 2026-05-04  
> **Общая готовность:** `█████████████░░░░░░░` ~65%

---

## Реальная структура пакетов (МНОГОМОДУЛЬНЫЙ ПРОЕКТ)

```
airport-system/ (root)
├── Shared/
│   └── src/main/java/com/common/
│       ├── dto/ (UserDto, FlightDto, TicketDto)
│       └── entity/ (User, Role, Airplane, Flight, Passenger, Ticket, City)
├── Server/
│   ├── src/main/java/com/server/
│   │   ├── dao/ (BaseDao, UserDao, RoleDao, CityDao, etc.)
│   │   ├── service/ (AuthService, AdminService, etc.)
│   │   └── utils/ (HibernateUtil, HashUtil)
│   └── src/main/resources/ (hibernate.cfg.xml, init_db.sql)
└── Client/
    ├── src/main/java/com/client/ (ClientApp, ServerConnection)
    └── src/main/resources/ (views/, css/)
```

---

## Этап 1: Domain Model & Persistence (Hibernate)

| Пункт | Статус | Комментарий |
|---|---|---|
| Настройка Hibernate | ✅ Реализовано | Перенесено в модуль Server |
| Entity-классы | ✅ Реализовано | Перенесено в модуль Shared |
| Связь Flight — @OneToMany — Ticket | ✅ Реализовано | |
| Связь Role — @OneToMany — User | ✅ Реализовано | |
| Поле `is_blocked` в User | ✅ Реализовано | Для функционала блокировки |
| SQL-скрипт инициализации БД | ✅ Реализовано | `init_db.sql` обновлен |

**Прогресс: `██████████` 100%** ✅

---

## Этап 2: Общий сетевой слой (Shared Library)

| Пункт | Статус | Комментарий |
|---|---|---|
| Унификация пакетов | ✅ Реализовано | Создан полноценный модуль Shared |
| Request / Response классы | ✅ Реализовано | |
| CommandType Enum | ✅ Реализовано | |
| Serializable для всех entity | ✅ Реализовано | |

**Прогресс: `██████████` 100%** ✅

---

## Этап 3: Серверная архитектура

| Пункт | Статус | Комментарий |
|---|---|---|
| HibernateUtil (Singleton) | ✅ Реализовано | |
| Generic DAO | ✅ Реализовано | Внедрен `BaseDao<T>`, устранено дублирование |
| MultiThreadedServer + ClientHandler | ✅ Реализовано | |
| Command Dispatcher | ✅ Реализовано | |
| Хеширование паролей | ✅ Реализовано | |

**Прогресс: `██████████` 100%** ✅

---

## Этап 4: Бизнес-логика (Сервисы)

| Пункт | Статус | Комментарий |
|---|---|---|
| AuthService (вход + регистрация) | ✅ Реализовано | Добавлена проверка на блокировку |
| FlightService (поиск) | ✅ Реализовано | |
| BookingService (атомарная покупка) | ✅ Реализовано | |
| AdminService (блокировка) | ✅ Реализовано | Метод `toggleUserBlock` добавлен |

**Прогресс: `██████████` 100%** ✅

---

## Этап 5: Базовый клиент

| Пункт | Статус | Комментарий |
|---|---|---|
| Структура клиента | ✅ Реализовано | Выделен в модуль Client |
| ServerConnection (Singleton) | ✅ Реализовано | |
| Login → Переход между окнами | ✅ Реализовано | Логика в LoginController реализована |
| Base Controller (абстрактный) | ❌ НЕ реализовано | |

**Прогресс: `████████░░` 80%**

---

## Этап 6: Пользовательский интерфейс

| Пункт | Статус | Комментарий |
|---|---|---|
| LoginView.fxml | ✅ Реализовано | |
| Admin/Dispatcher/Client Main | 🟡 Частично | Созданы плейсхолдеры |
| Data Binding | ❌ НЕ реализовано | |
| Async UI | ❌ НЕ реализовано | |

**Прогресс: `████░░░░░░` 40%**

---

## Итоговая таблица по этапам

| Этап | До правок | После правок (04.05) |
|---|---|---|
| 1. Domain Model & Hibernate | 🟢 100% | 🟢 100% |
| 2. Сетевой слой | 🟡 70% | 🟢 100% ↑ |
| 3. Серверная архитектура | 🟢 83% | 🟢 100% ↑ |
| 4. Бизнес-логика | 🟡 80% | 🟢 100% ↑ |
| 5. Базовый клиент | 🟡 75% | 🟢 80% ↑ |
| 6. Интерфейс UI | 🔴 25% | 🟡 40% ↑ |
| **Итого** | **~52%** | **~65%** ↑ |

---

## Что исправлено по запросу:
1.  **Generic DAO**: Создан `BaseDao`, все DAO отрефакторены.
2.  **Login → Transition**: В `LoginController` добавлена логика `switchToMainView` на основе ролей.
3.  **AdminService (Блокировка)**: Добавлено поле `isBlocked` в `User` и метод управления в `AdminService`. `AuthService` проверяет статус при входе.
4.  **Модульность**: Проект полностью переведен на многомодульную структуру (Maven modules: Shared, Server, Client).
