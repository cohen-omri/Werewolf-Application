package client_objects;

import server_objects.Message;

import java.io.Serializable;

public class LoginObj extends Message implements Serializable {

    //private static final long serialVersionUID = 100000L;

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

    /** Msg:
     * -1 - unkown error
     * 0 - no data was given
     * signup errors:
     * 1 - usShortErr - shorter than x chars
     * 2 - usLongErr - longer than x chars
     * 3 - pwdShortErr - shorter than x chars
     * 4 - pwdLongErr - longer than x chars
     * login errors:
     * 1 - notFoundErr - user was not found in the database
     */
}