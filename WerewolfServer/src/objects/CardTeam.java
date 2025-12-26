package objects;

import java.io.Serializable;

public enum CardTeam implements Serializable {

    VILLAGE(0),
    WEREWOLF(1),
    TANNER(2);

    CardTeam(int team) {
        this.team = team;
    }

    public int team;
    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }
}
