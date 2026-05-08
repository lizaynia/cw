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



    public static TicketDto toDto(Ticket ticket) {
        if (ticket == null) return null;
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setFlightNumber(ticket.getFlight().getFlightNumber());
        dto.setRoute(ticket.getFlight().getDepartureCity().getCityName() + " → " +
                ticket.getFlight().getArrivalCity().getCityName());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setPrice(ticket.getPrice());
        dto.setStatus(ticket.getStatus().toString());
        return dto;
    }

    public static List<TicketDto> toTicketDtoList(List<Ticket> tickets) {
        if (tickets == null) return null;
        return tickets.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }

    public static FlightDto toDto(Flight flight, long bookedTickets) {
        if (flight == null) return null;
        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());

        dto.setFlightNumber(flight.getFlightNumber());
        dto.setDepartureCity(flight.getDepartureCity().getCityName());
        dto.setArrivalCity(flight.getArrivalCity().getCityName());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setAirplaneModel(flight.getAirplane().getModel());
        dto.setAvailableSeats(flight.getAirplane().getCapacity() - (int)bookedTickets);
        dto.setBasePrice(flight.getBasePrice() != null ? flight.getBasePrice().doubleValue() : 0.0);
        return dto;
    }

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getLogin(), user.getRole().getRoleName());

    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        if (users == null) return null;
        return users.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }

    public static List<FlightDto> toFlightDtoList(List<Flight> flights, java.util.Map<Integer, Long> bookedCounts) {
        if (flights == null) return null;
        return flights.stream()
                .map(f -> toDto(f, bookedCounts.getOrDefault(f.getId(), 0L)))
                .collect(java.util.stream.Collectors.toList());
    }


}
