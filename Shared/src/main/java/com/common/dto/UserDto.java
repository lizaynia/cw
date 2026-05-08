package com.common.dto;

import java.io.Serializable;

public class UserDto implements Serializable {
    private Integer id;
    private String login;
    private String roleName;
    private boolean blocked;
    private String fullName;      // ✅ НОВОЕ ПОЛЕ
    private String passportNumber; // ✅ НОВОЕ ПОЛЕ

    public UserDto() {}

    public UserDto(Integer id, String login, String roleName) {
        this.id = id;
        this.login = login;
        this.roleName = roleName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
}