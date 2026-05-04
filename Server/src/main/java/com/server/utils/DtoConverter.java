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
        return new UserDto(user.getId(), user.getLogin(), user.getRole().getRoleName());
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
        if (users == null) return null;
        return users.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }

    public static List<FlightDto> toFlightDtoList(List<Flight> flights) {
        if (flights == null) return null;
        // Note: this simple version doesn't handle bookedTickets count for each flight.
        // If needed, we can pass a map or use a more complex logic.
        // For now, let's assume 0 booked tickets or update later.
        return flights.stream().map(f -> toDto(f, 0)).collect(Collectors.toList());
    }

    public static List<TicketDto> toTicketDtoList(List<Ticket> tickets) {
        if (tickets == null) return null;
        return tickets.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }
}
