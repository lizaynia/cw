-- ============================================================
--  airport_db — скрипт инициализации базы данных
--  Запускать в MySQL от пользователя root
--  Порядок выполнения: сверху вниз (зависимости учтены)
-- ============================================================

CREATE DATABASE IF NOT EXISTS airport_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE airport_db;

-- ------------------------------------------------------------
-- 1. Справочник городов
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cities (
    id_city   INT          NOT NULL AUTO_INCREMENT,
    city_name VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (id_city)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. Роли пользователей
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id_role   INT         NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,   -- ADMIN | DISPATCHER | CLIENT
    PRIMARY KEY (id_role)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. Пользователи (аккаунты)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id_user  INT          NOT NULL AUTO_INCREMENT,
    login    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,          -- SHA-256 хеш
    role_id  INT          NOT NULL,
    PRIMARY KEY (id_user),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id_role)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. Самолёты
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS airplanes (
    id_airplane INT         NOT NULL AUTO_INCREMENT,
    model       VARCHAR(100) NOT NULL,
    capacity    INT          NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | MAINTENANCE | DECOMMISSIONED
    PRIMARY KEY (id_airplane)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. Рейсы (связан с cities дважды и с airplanes)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS flights (
    id_flight      INT          NOT NULL AUTO_INCREMENT,
    flight_number  VARCHAR(20)  NOT NULL UNIQUE,
    departure_city_id INT       NOT NULL,
    arrival_city_id   INT       NOT NULL,
    departure_time DATETIME     NOT NULL,
    airplane_id    INT          NOT NULL,
    PRIMARY KEY (id_flight),
    CONSTRAINT fk_flights_dep_city  FOREIGN KEY (departure_city_id) REFERENCES cities (id_city),
    CONSTRAINT fk_flights_arr_city  FOREIGN KEY (arrival_city_id)   REFERENCES cities (id_city),
    CONSTRAINT fk_flights_airplane  FOREIGN KEY (airplane_id)        REFERENCES airplanes (id_airplane)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. Пассажиры (привязаны к аккаунту users)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS passengers (
    id_passenger    INT          NOT NULL AUTO_INCREMENT,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    passport_number VARCHAR(20)  NOT NULL UNIQUE,
    user_id         INT              NULL,            -- NULL = гость без аккаунта
    PRIMARY KEY (id_passenger),
    CONSTRAINT fk_passengers_user FOREIGN KEY (user_id) REFERENCES users (id_user)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. Билеты
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id_ticket    INT            NOT NULL AUTO_INCREMENT,
    flight_id    INT            NOT NULL,
    passenger_id INT            NOT NULL,
    seat_number  VARCHAR(10)        NULL,
    price        DECIMAL(10, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'BOOKED',  -- BOOKED | PAID | CANCELLED
    PRIMARY KEY (id_ticket),
    CONSTRAINT fk_tickets_flight    FOREIGN KEY (flight_id)    REFERENCES flights    (id_flight),
    CONSTRAINT fk_tickets_passenger FOREIGN KEY (passenger_id) REFERENCES passengers (id_passenger)
) ENGINE=InnoDB;

-- ============================================================
--  Начальные данные (seed data)
-- ============================================================

-- Роли (обязательны для регистрации через AuthService)
INSERT IGNORE INTO roles (role_name) VALUES ('ADMIN'), ('DISPATCHER'), ('CLIENT');

-- Города
INSERT IGNORE INTO cities (city_name) VALUES
    ('Минск'), ('Москва'), ('Варшава'), ('Берлин'), ('Лондон'),
    ('Париж'), ('Рим'), ('Прага'), ('Вена'), ('Барселона');

-- Администратор по умолчанию
-- Пароль: admin123  →  SHA-256 хеш (совпадает с HashUtil.hashPassword("admin123"))
INSERT IGNORE INTO users (login, password, role_id)
SELECT 'admin',
       '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
       id_role
FROM roles WHERE role_name = 'ADMIN'
LIMIT 1;

-- Самолёт для тестовых рейсов
INSERT IGNORE INTO airplanes (model, capacity, status)
VALUES ('Boeing 737', 180, 'ACTIVE'),
       ('Airbus A320', 150, 'ACTIVE');
