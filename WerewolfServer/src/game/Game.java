package game;

import client_objects.ChatMessage;
import objects.Card;
import objects.CardType;
import objects.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class Game {

    private CardType currentDopple = CardType.DOPPLEGANGER;
    private String code;
    private int maxPlayers;
    private ArrayList<Player> players;
    private ArrayList<Player> ghosts;
    private Player creator;
    private GameState state;
    // action time, discussion time, voting time
    private int actionTime; // seconds
    private int discussionTime; // seconds
    private int votingTime; // seconds

    private ArrayList<ChatMessage> chat;
    private HashMap<Integer, Boolean> activeRoles;

    private CardType currentTurn;

    private HashMap<UUID, Card> currentRoles;
    private HashMap<UUID, Card> finalRoles;
    private HashMap<UUID, Integer> votes;
    private HashMap<UUID, UUID> votesByUser;

    private List<CardType> middleCards;

    private ScheduledFuture<?> discussionFuture;
    private ScheduledFuture<?> votingFuture;
    private ScheduledFuture<?> actionFuture;
    private ScheduledFuture<?> endingFuture;

    private Player chosen;

    public Game(GameState state, String code,
                int targetedNumPlayers, ArrayList<Player> players,
                Player creator, int actionTime, int discussionTime, int votingTime) {
        this.code = code;
        this.maxPlayers = targetedNumPlayers;
        this.players = players;
        this.ghosts = new ArrayList<>();
        this.creator = creator;
        this.state = state;
        this.votingTime = votingTime;
        this.discussionTime = discussionTime;
        this.actionTime = actionTime;

        this.chat = new ArrayList<>();
        this.activeRoles = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            activeRoles.put(i, true);
        }

        this.currentRoles = new HashMap<>();
        this.finalRoles = new HashMap<>();
        this.votes = new HashMap<>();
        this.middleCards = new ArrayList<>();
        this.discussionFuture = null;
        this.votingFuture = null;
        this.actionFuture = null;
        this.endingFuture = null;
        this.chosen = null;
        this.votesByUser = new HashMap<>();
    }

    public boolean start() {
        if (hasStarted()) return false;
        //if(players.size() != maxPlayers) return false;

        this.state = GameState.STARTING;
        return true;
    }

    public boolean hasStarted() {
        return this.state != GameState.WAITING && this.state != GameState.ENDING;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if (players.contains(player)) return false;
        if (players.size() < maxPlayers) {
            this.players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        players.remove(player);
        if (players.isEmpty()) return;

        if (this.creator == player && !players.isEmpty()) creator =
                (players.getFirst() == null ? null : players.getFirst()) ;

        if(hasStarted()) {
            addGhost(player);
        }
    }

    public boolean hasPlayer(Player p) {
        return players.contains(p);
    }

    public String getCode() {
        return code;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public void addGhost(Player p) {
        p.setConnected(false);
        players.remove(p);
        ghosts.add(p);
    }

    public void removeGhost(Player p) {
        p.setConnected(true);
        //players.add(p);
        ghosts.remove(p);
    }

    public boolean hasGhost(Player p) {
        return this.ghosts.contains(p);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getActionTime() {
        return actionTime;
    }

    public void setActionTime(int actionTime) {
        this.actionTime = actionTime;
    }

    public int getDiscussionTime() {
        return discussionTime;
    }

    public void setDiscussionTime(int discussionTime) {
        this.discussionTime = discussionTime;
    }

    public int getVotingTime() {
        return votingTime;
    }

    public void setVotingTime(int votingTime) {
        this.votingTime = votingTime;
    }

    public ArrayList<ChatMessage> getChat() {
        return chat;
    }

    public void setChat(ArrayList<ChatMessage> chat) {
        this.chat = chat;
    }

    public HashMap<Integer, Boolean> getActiveRoles() {
        return activeRoles;
    }

    public void setActiveRoles(HashMap<Integer, Boolean> activeRoles) {
        this.activeRoles = activeRoles;
    }

    public HashMap<UUID, Card> getCurrentRoles() {
        return currentRoles;
    }

    public void setCurrentRoles(HashMap<UUID, Card> currentRoles) {
        this.currentRoles = currentRoles;
    }

    public HashMap<UUID, Card> getFinalRoles() {
        return finalRoles;
    }

    public void setFinalRoles(HashMap<UUID, Card> finalRoles) {
        this.finalRoles = finalRoles;
    }

    public CardType getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(CardType currentTurn) {
        this.currentTurn = currentTurn;
    }

    public List<CardType> getMiddleCards() {
        return middleCards;
    }

    public void setMiddleCards(List<CardType> middleCards) {
        this.middleCards = middleCards;
    }

    public CardType getCurrentDopple() {
        return currentDopple;
    }

    public void setCurrentDopple(CardType currentDopple) {
        this.currentDopple = currentDopple;
    }

    public HashMap<UUID, Integer> getVotes() {
        return votes;
    }

    public void setVotes(HashMap<UUID, Integer> votes) {
        this.votes = votes;
    }

    public ScheduledFuture<?> getDiscussionFuture() {
        return discussionFuture;
    }

    public void setDiscussionFuture(ScheduledFuture<?> discussionFuture) {
        this.discussionFuture = discussionFuture;
    }

    public ScheduledFuture<?> getVotingFuture() {
        return votingFuture;
    }

    public void setVotingFuture(ScheduledFuture<?> votingFuture) {
        this.votingFuture = votingFuture;
    }

    public ScheduledFuture<?> getActionFuture() {
        return actionFuture;
    }

    public void setActionFuture(ScheduledFuture<?> actionFuture) {
        this.actionFuture = actionFuture;
    }

    public ScheduledFuture<?> getEndingFuture() {
        return endingFuture;
    }

    public void setEndingFuture(ScheduledFuture<?> endingFuture) {
        this.endingFuture = endingFuture;
    }

    public Player getChosen() {
        return chosen;
    }

    public void setChosen(Player chosen) {
        this.chosen = chosen;
    }

    public ArrayList<Player> getGhosts() {
        return ghosts;
    }

    public void setGhosts(ArrayList<Player> ghosts) {
        this.ghosts = ghosts;
    }

    public HashMap<UUID, UUID> getVotesByUser() {
        return votesByUser;
    }

    public void setVotesByUser(HashMap<UUID, UUID> votesByUser) {
        this.votesByUser = votesByUser;
    }
}
