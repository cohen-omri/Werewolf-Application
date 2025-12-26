package objects;

import java.io.Serializable;

public enum CardType implements Serializable {

    DOPPLEGANGER(0),
    WEREWOLF(1),
    MINION(2),
    MASON(3),
    SEER(4),
    ROBBER(5),
    TROUBLEMAKER(6),
    DRUNK(7),
    INSOMNIAC(8),
    HUNTER(9),
    TANNER(10),
    VILLAGER(11);

    CardType(int type) {
        this.type = type;
    }

    public final int type;

    public int getType() {
        return type;
    }

    //get 0 to 15 (lobby)
    public static CardType cardFromInt(int n) {

        if (n == 0) return DOPPLEGANGER;
        if (n == 2 || n == 1) return WEREWOLF;
        if (n == 3) return MINION;
        if (n == 4 || n == 5) return MASON;
        if (n == 6) return SEER;
        if (n == 7) return ROBBER;
        if (n == 8) return TROUBLEMAKER;
        if (n == 9) return DRUNK;
        if (n == 10) return INSOMNIAC;
        if (n == 11) return HUNTER;
        if (n == 12) return TANNER;
        if (n == 13 || n == 14 || n == 15) return VILLAGER;

        throw new IllegalArgumentException("No CardType with value " + n + " found.");
    }

    //get 0 to 11 (mid game)
    public static CardType roleFromInt(int n) {
        if (n == 0) return DOPPLEGANGER;
        if (n == 1) return WEREWOLF;
        if (n == 2) return MINION;
        if (n == 3) return MASON;
        if (n == 4) return SEER;
        if (n == 5) return ROBBER;
        if (n == 6) return TROUBLEMAKER;
        if (n == 7) return DRUNK;
        if (n == 8) return INSOMNIAC;
        if (n == 9) return HUNTER;
        if (n == 10) return TANNER;
        if (n == 11) return VILLAGER;

        throw new IllegalArgumentException("No CardType with value " + n + " found.");
    }

}