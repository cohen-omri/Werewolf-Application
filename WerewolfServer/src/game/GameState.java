package game;

public enum GameState {
    WAITING, // waiting for players - lobby
    STARTING, //giving card n stuff ig
    PLAYING, // turn of each role
    DISCUSSION, //chatting with other players
    VOTING, // voting for a player
    ENDING, // announcing winners n back to lobby
}
