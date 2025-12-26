package com.example.werewolfclient.ClientObjects;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    protected int action;
    protected int msg;
    protected UUID uuid;

    public Message(int action, int msg, UUID uuid) {
        this.msg = msg;
        this.action = action;
        this.uuid = uuid;
    }

    public Message(){
        this.uuid = null;
        this.msg = 0;
        this.action = 0;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getMsg() {
        return msg;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }

    public UUID getUuid() { return uuid; }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /** Action
     *  0 - no data was given
     *  1 - signup
     *  2 - login
     *  3 - update user
     *  4 - logout
     *  5 - create game
     *  6 - join game
      */

    /** Msg:
     * -1 - unknown error
     * 0 - no problems / client object
     * signup errors:
     * 1 - usShort - shorter than x chars
     * 2 - usLong - longer than x chars
     * 3 - pwdShort - shorter than x chars
     * 4 - pwdLong - longer than x chars
     * 5 - userExists - user already exists
     * login errors:
     * 1 - notFound - user was not found in the database
     * 2 - pwdIncorrect - wrong password
     * 3 - usShort - shorter than x chars
     * 4 - usLong - longer than x chars
     * 5 - pwdShort - shorter than x chars
     * 6 - pwdLong - longer than x chars
     * 7 - userExists - user already exists
     */



}
