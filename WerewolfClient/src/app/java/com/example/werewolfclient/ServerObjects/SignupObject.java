package com.example.werewolfclient.ServerObjects;

import com.example.werewolfclient.ClientObjects.Message;

import java.io.Serializable;

public class SignupObject extends Message implements Serializable {

    private boolean ack;

    public SignupObject() {
        super(0,0,null);
        this.ack = false;
    }

    public SignupObject(int action, int msg, boolean ack) {
        super(action, msg, null);
        this.ack = ack;
    }

    @Override
    public String toString() {
        return "{Action: " + action + ", Message: "+ msg + ", ACK: "+ack+"}";
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
}
