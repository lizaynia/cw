package com.common;

public enum CommandType {
    // --- АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ ---
    REGISTER,
    LOGIN,

    // --- КЛИЕНТ (И НЕАВТОРИЗОВАННЫЙ) ---
    GET_SCHEDULE,
    BOOK_TICKET,
    GET_TICKET_HISTORY,
    SEARCH_FLIGHTS,
    ADVANCED_SEARCH_FLIGHTS,
    GET_CITIES_FROM_FLIGHTS,

    // --- ДИСПЕТЧЕР ---
    ADD_AIRPLANE,
    GET_AIRPLANES,
    ADD_FLIGHT,
    UPDATE_FLIGHT,
    DELETE_FLIGHT,


    // --- АДМИНИСТРАТОР ---
    GET_USERS,
    CHANGE_ROLE,
    BLOCK_USER,
    UPDATE_AIRPLANE_STATUS,
    GET_STATISTICS,
    
    // --- НОВЫЕ КОМАНДЫ ДЛЯ UI ---
    GET_OCCUPIED_SEATS,
    UPDATE_PROFILE,
    UPDATE_PROFILE_INFO,



}
