package com.example.werewolfclient.ServerObjects;

import com.example.werewolfclient.ClientObjects.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import objects.CardType;

public class GameObject extends Message implements Serializable {

    private ArrayList<String> players;
    private CardType card;
    private boolean isAdmin;
    private int actionTime;
    private int discussionTime;
    private int votingTime;

    public GameObject() {
        super(11,-1,null);
        this.players = null;
        this.card = null;
        this.actionTime = 0;
        this.discussionTime = 0;
        this.votingTime = 0;
        this.isAdmin = false;
    }

    public GameObject(int msg, UUID uuid, ArrayList<String> players, CardType card,
                      int actionTime, int discussionTime, int votingTime, boolean isAdmin) {
        super(11, msg, uuid);
        this.players = players;
        this.card = card;
        this.votingTime = votingTime;
        this.discussionTime = discussionTime;
        this.actionTime = actionTime;
        this.isAdmin = isAdmin;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public objects.CardType getCard() {
        return card;
    }

    public void setCard(objects.CardType card) {
        this.card = card;
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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}