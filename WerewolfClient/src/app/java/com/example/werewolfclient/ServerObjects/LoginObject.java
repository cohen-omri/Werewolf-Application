package com.example.werewolfclient.ServerObjects;

import com.example.werewolfclient.ClientObjects.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class LoginObject extends Message implements Serializable {

    private boolean ack;
    private HashMap<String, Object> data;

    public LoginObject() {
        super(0, 0, null);
        this.ack = false;
        this.data = new HashMap<>();
    }

    public LoginObject(int action, int msg, boolean ack, UUID uuid, HashMap<String, Object> data) {
        super(action, msg, uuid);
        this.ack = ack;
        this.data = data;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}
