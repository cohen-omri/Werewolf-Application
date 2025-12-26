package objects;

import java.io.Serializable;

public class Card implements Serializable {

    private CardType role;
    private CardTeam team;

    public Card(CardType role) {
        this.role = role;
        if(role.type == 10) this.team = CardTeam.TANNER;
        else if (role.type == 1 || role.type == 2) this.team = CardTeam.WEREWOLF;
        else this.team = CardTeam.VILLAGE;
    }

    public CardType getRole() { return role; }
    public CardTeam getTeam() { return team; }

    public void setRole(CardType role) {
        this.role = role;
    }

    public void setTeam(CardTeam team) {
        this.team = team;
    }
}
