package client_objects;

import server_objects.Message;

import java.io.Serializable;
import java.util.UUID;

public class StringMessage extends Message implements Serializable {

    private String string;
    public StringMessage(int action, int msg, UUID uuid, String string) {
        super(action, msg, uuid);
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

}
