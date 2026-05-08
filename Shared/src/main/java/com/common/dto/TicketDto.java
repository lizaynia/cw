package com.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class TicketDto implements Serializable {
    private Integer id;
    private String flightNumber;
    private String passengerName;
    private String seatNumber;
    private BigDecimal price;
    private String status;

    private String route;

    private String flightDate;

    public String getFlightDate() { return flightDate; }
    public void setFlightDate(String flightDate) { this.flightDate = flightDate; }
    public TicketDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
