package server_objects;

import game.GameState;

import java.io.Serializable;
import java.util.UUID;

public class AdminObject extends Message implements Serializable {

    private LobbyObject lobby;
    private GameState state;

    public AdminObject() {
        this.lobby = null;
        this.state = null;
    }

    public AdminObject(int action, int msg, UUID uuid, LobbyObject lobby, GameState state) {
        super(action, msg, uuid);
        this.lobby = lobby;
        this.state = state;
    }

    public LobbyObject getLobby() {
        return lobby;
    }

    public void setLobby(LobbyObject lobby) {
        this.lobby = lobby;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}