package client_objects;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private String username;
    private String text;

    public ChatMessage(String username, String text) {
        this.username = username;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

}