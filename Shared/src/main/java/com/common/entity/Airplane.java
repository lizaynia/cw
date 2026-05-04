package com.common.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "airplanes")
public class Airplane implements Serializable {
    
    public enum AirplaneStatus {
        READY, REPAIR, IN_SERVICE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_airplane")
    private Integer id;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AirplaneStatus status = AirplaneStatus.READY;

    public Airplane() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public AirplaneStatus getStatus() { return status; }
    public void setStatus(AirplaneStatus status) { this.status = status; }
}
