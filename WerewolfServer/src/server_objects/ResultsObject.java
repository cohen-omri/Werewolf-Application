package server_objects;

import objects.CardType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ResultsObject extends Message implements Serializable {

    private HashMap<String, CardType> userRoles;
    private ArrayList<String> players;
    private String votedUsername;
    private boolean isWin;
    private String code;

    public ResultsObject() {
        this.userRoles = new HashMap<>();
        this.players = new ArrayList<>();
        this.votedUsername = "";
        this.isWin = false;
        this.code = "";
    }

    public ResultsObject(int action, int msg, UUID uuid, HashMap<String, CardType> userRoles,
                         ArrayList<String> players, String votedUsername, boolean isWin, String code) {
        super(action, msg, uuid);
        this.userRoles = userRoles;
        this.players = players;
        this.votedUsername = votedUsername;
        this.isWin = isWin;
        this.code = code;
    }

    public HashMap<String, CardType> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(HashMap<String, CardType> userRoles) {
        this.userRoles = userRoles;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public String getVotedUsername() {
        return votedUsername;
    }

    public void setVotedUsername(String votedUsername) {
        this.votedUsername = votedUsername;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
