package com.example.werewolfclient.ClientObjects;

import java.util.HashMap;
import java.util.UUID;

public class User {

    private HashMap<String, Object> data;
    private UUID uuid;

    /**
     * DATA:
     * name - string
     * password - string
     * type - boolean (admin)
     * games - int
     * wins - int
     * wins-town - int
     * wins-werewolf - int
     * wins-tanner - int
     * join-date - java.util.Date
     * characters - List<int>
     */
    public User() {
        this.uuid = null;
        this.data = new HashMap<>();
    }

    public User(UUID uuid, HashMap<String, Object> data) {
        this.uuid = uuid;
        this.data = data;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public UUID getUuid() { return uuid; }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
