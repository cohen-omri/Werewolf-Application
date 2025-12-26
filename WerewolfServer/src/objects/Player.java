package objects;

import game.ObjectHandler;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import static database.Connection.getData;

public class Player {

    private String name;
    private UUID uuid;
    private Socket socket = null;
    private boolean connected;
    private Map<String, Object> data;
    public ObjectHandler handler;

    public Player(String name, Socket socket, UUID uuid, ObjectHandler handle, Map<String, Object> data) throws IOException {
        this.name = name;
        this.socket = socket;
        this.connected = false;
        this.data = data;
        this.uuid = uuid;
        this.handler = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean b) {
        connected = b;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}
