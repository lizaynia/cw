package com.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FlightDto implements Serializable {
    private Integer id;
    private String flightNumber;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private String airplaneModel;
    private Integer availableSeats;
    private Double basePrice;

    public FlightDto() {}

    public String getRoute() {
        return departureCity + " -> " + arrivalCity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }
    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public String getAirplaneModel() { return airplaneModel; }
    public void setAirplaneModel(String airplaneModel) { this.airplaneModel = airplaneModel; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
}
