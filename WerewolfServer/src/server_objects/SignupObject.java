package server_objects;

import java.io.Serializable;
import java.util.UUID;

public class SignupObject extends Message implements Serializable {
    //private static final long serialVersionUID = 100000L;

    private boolean ack;

    /**
     * Creates an empty login object - default constructor
     */
    public SignupObject() {
        super(0,0,null);
        this.ack = false;
    }

    /**
     * Creates a login object to pass between the server and client
     *
     * @Author Omri
     * @param msg an action/message to do
     * @param ack true - success, false - failure (see message for details)
     */
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
