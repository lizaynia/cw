package com.common.dto;

import java.io.Serializable;

public class PassengerDto implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String passportNumber;

    public PassengerDto() {}

    public PassengerDto(Long id, String firstName, String lastName, String passportNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportNumber = passportNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
}
