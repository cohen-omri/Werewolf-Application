package com.example.werewolfclient.ClientObjects;

import java.io.Serializable;

public class LoginObj extends Message implements Serializable {

    private String username;
    private String password;

    //default constructor
    public LoginObj() {
        super();
        this.username = "";
        this.password = "";
        this.msg = 0;
    }

    public LoginObj(String name, String pswd, int action) {

        super(action, 0, null);
        this.username = name;
        this.password = pswd;
    }

    @Override
    public String toString() {
        return "{Action: " + action + ", Name: " + username + ", Password: " + password + "}";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}