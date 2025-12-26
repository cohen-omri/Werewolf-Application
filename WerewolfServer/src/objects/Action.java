package objects;

public enum Action {
    ERROR(0),
    SIGNUP(1),
    LOGIN(2),
    UPDATE_USER(3),
    LOGOUT(4),
    CREATE(5),
    JOIN(6),
    ADD_PLAYER(7),
    REMOVE_PLAYER(8),
    ADD_MESSAGE(9),
    TOGGLE_ROLE(10),
    START_GAME(11), // FROM ADMIN
    SET_TURN(12),
    SHOW_CARD_NAME(13),
    SWITCH_CARDS(14),
    START_DISCUSSION(15),
    START_VOTING(16),
    VOTE(17),
    START_ENDING(18),
    ADMIN_GAMES(19),
    ADMIN_CODES(20);

    public final int action;
    Action(int a) {
        this.action = a;
    }

    Action() {
        this(0);
    }

    public static Action fromInt(int n) {
        for (Action action : Action.values()) {
            if (action.action == n) {
                return action;
            }
        }
        throw new IllegalArgumentException("No Action with value " + n + " found.");
    }
}
