package com.common.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flights")
public class Flight implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_flight")
    private Integer id;

    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_city_id", nullable = false)
    private City departureCity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_city_id", nullable = false)
    private City arrivalCity;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airplane_id", nullable = false)
    private Airplane airplane;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @Column(name = "base_price", nullable = false)
    private java.math.BigDecimal basePrice = java.math.BigDecimal.valueOf(100.0);

    public java.math.BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(java.math.BigDecimal basePrice) { this.basePrice = basePrice; }

    public Flight() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    
    public City getDepartureCity() { return departureCity; }
    public void setDepartureCity(City departureCity) { this.departureCity = departureCity; }
    
    public City getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(City arrivalCity) { this.arrivalCity = arrivalCity; }
    
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    
    public Airplane getAirplane() { return airplane; }
    public void setAirplane(Airplane airplane) { this.airplane = airplane; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}
