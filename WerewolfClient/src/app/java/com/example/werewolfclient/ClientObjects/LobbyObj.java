package com.example.werewolfclient.ClientObjects;


import java.io.Serializable;
import java.util.UUID;

public class LobbyObj extends Message implements Serializable {

    private int maxPlayers;
    private int actionTime;
    private int discussionTime;
    private int votingTime;

    public LobbyObj() {
        super(0, 0, null);
        this.maxPlayers = 0;
        this.actionTime = 0;
        this.discussionTime = 0;
        this.votingTime = 0;
    }

    public LobbyObj (UUID uuid, int maxPlayers, int actionTime, int discussionTime, int votingTime) {

        super(5, 0, uuid);
        this.maxPlayers = maxPlayers;
        this.actionTime = actionTime;
        this.discussionTime = discussionTime;
        this.votingTime = votingTime;
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
}
