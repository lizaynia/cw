package com.common.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "cities")
public class City implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_city")
    private Integer id;

    @Column(name = "city_name", nullable = false, unique = true)
    private String cityName;

    @OneToMany(mappedBy = "departureCity", fetch = FetchType.LAZY)
    private List<Flight> departingFlights;

    @OneToMany(mappedBy = "arrivalCity", fetch = FetchType.LAZY)
    private List<Flight> arrivingFlights;

    public City() {}

    public City(String cityName) {
        this.cityName = cityName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public List<Flight> getDepartingFlights() { return departingFlights; }
    public void setDepartingFlights(List<Flight> departingFlights) { this.departingFlights = departingFlights; }

    public List<Flight> getArrivingFlights() { return arrivingFlights; }
    public void setArrivingFlights(List<Flight> arrivingFlights) { this.arrivingFlights = arrivingFlights; }
}
