package com.example.werewolfclient.ServerObjects;

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.Objects.ChatMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LobbyObject extends Message implements Serializable {

    private int maxPlayers;
    private int actionTime;
    private int discussionTime;
    private int votingTime;
    private ArrayList<ChatMessage> chatMessageList;
    private HashMap<Integer, Boolean> activeRoles;
    private String code;
    private ArrayList<String> players;
    private boolean admin;

    public LobbyObject() {
        super(5,0, UUID.randomUUID());
        this.maxPlayers = 0;
        this.actionTime = 0;
        this.discussionTime = 0;
        this.votingTime = 0;
        this.chatMessageList = new ArrayList<>();
        this.activeRoles = new HashMap<>();
        this.code = "";
        this.players = new ArrayList<>();
        this.admin = false;
    }

    public LobbyObject(int msg, UUID uuid, int maxPlayers,
                       int actionTime, int discussionTime, int votingTime,
                       ArrayList<ChatMessage> chatMessageList,
                       HashMap<Integer, Boolean> activeRoles, String code,
                       ArrayList<String> players, boolean admin) {
        super(5, msg, uuid);
        this.maxPlayers = maxPlayers;
        this.actionTime = actionTime;
        this.discussionTime = discussionTime;
        this.votingTime = votingTime;
        this.chatMessageList = chatMessageList;
        this.activeRoles = activeRoles;
        this.code = code;
        this.players = players;
        this.admin = admin;
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

    public ArrayList<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(ArrayList<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public HashMap<Integer, Boolean> getActiveRoles() {
        return activeRoles;
    }

    public void setActiveRoles(HashMap<Integer, Boolean> activeRoles) {
        this.activeRoles = activeRoles;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}