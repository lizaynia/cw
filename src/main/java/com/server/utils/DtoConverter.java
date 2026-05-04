package com.server.utils;

import com.common.dto.FlightDto;
import com.common.dto.TicketDto;
import com.common.dto.UserDto;
import com.common.entity.Flight;
import com.common.entity.Ticket;
import com.common.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class DtoConverter {

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getLogin(), user.getRole().getName());
    }

    public static FlightDto toDto(Flight flight, long bookedTickets) {
        if (flight == null) return null;
        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setDepartureCity(flight.getDepartureCity());
        dto.setArrivalCity(flight.getArrivalCity());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setAirplaneModel(flight.getAirplane().getModel());
        dto.setAvailableSeats(flight.getAirplane().getCapacity() - (int)bookedTickets);
        return dto;
    }

    public static TicketDto toDto(Ticket ticket) {
        if (ticket == null) return null;
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setFlightNumber(ticket.getFlight().getFlightNumber());
        dto.setPassengerName(ticket.getPassenger().getFirstName() + " " + ticket.getPassenger().getLastName());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setPrice(ticket.getPrice());
        dto.setStatus(ticket.getStatus().name());
        return dto;
    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }
}
