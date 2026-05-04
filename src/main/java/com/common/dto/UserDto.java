package com.common.dto;

import java.io.Serializable;

public class UserDto implements Serializable {
    private Integer id;
    private String login;
    private String roleName;

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
}
