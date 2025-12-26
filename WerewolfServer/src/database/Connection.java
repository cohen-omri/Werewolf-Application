package database;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import game.ObjectHandler;
import objects.Player;
import server_objects.LoginObject;
import server_objects.SignupObject;

import static game.Server.players;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static game.GameManager.maxLength;
import static game.GameManager.minLength;

public class Connection {

    private static final Firestore db = FirestoreConfig.getFirestore();
    private static final String COLLECTION_NAME = "users";

    public static LoginObject login(String username, String password, int action) {
        try {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(username).get().get();

            if (document.exists()) {
                String storedPassword = document.getString("password");
                LoginObject lo;
                if (storedPassword != null && storedPassword.equals(password)) {
                    lo = new LoginObject(action, 0, true, null, getData(username));
                } else {
                    lo = new LoginObject(action, 2, false, null, new HashMap<>());
                }
                return lo;
                // Check if password matches
            }
            return new LoginObject(action, 1, false, null, new HashMap<>()); // Document not found
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new LoginObject(action, -1, false, null, new HashMap<>());
        }
    }

    // Sign-Up Method
    public static SignupObject signUp(String username, String password, int action, UUID uuid, ObjectHandler handle) {
        try {
            //if(action == 1) {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(username).get().get();
            String olds = username;
            if (uuid != null && players.containsKey(uuid))
                olds = players.get(uuid).getName();
            if (document.exists() && !olds.equals(username)) {
                //System.out.println("Username already exists!");
                return new SignupObject(1, 5, false);
                // Username already taken
            }
            //}
            if (password.length() < minLength) {
                return new SignupObject(action, 3, false);
            } else if (password.length() > maxLength) {
                return new SignupObject(action, 4, false);
            } else if (username.length() > maxLength) {
                return new SignupObject(action, 2, false);
            } else if (username.length() < minLength) {
                return new SignupObject(action, 1, false);
            }

            // Create a new document with the username as the document ID
            if (action == 1) {
                Map<String, Object> newUser = new HashMap<>();
                newUser.put("password", password); // Use hashed passwords in production
                newUser.put("games", 0);
                //newUser.put("loses", 0);
                newUser.put("name", username);
                newUser.put("type", false);
                newUser.put("wins", 0);
                newUser.put("wins-town", 0);
                newUser.put("wins-werewolf", 0);
                newUser.put("wins-tanner", 0);
                newUser.put("join-date", new Date().getTime());
                List<Integer> chars = new ArrayList<>();
                for (int i = 0; i < 12; i++)
                    chars.add(0);
                newUser.put("characters", chars);
                db.collection(COLLECTION_NAME).document(username).set(newUser).get();
                SignupObject lo = new SignupObject(action, 0, true);
                //System.out.println();
                return lo;
            } else if (action == 3) {
                String old = players.get(uuid).getName();
                Map<String, Object> use = players.get(uuid).getData();
                use.put("name", username);
                use.put("password", password);
                players.remove(uuid);
                players.put(uuid, new Player(username, null, uuid, handle, use));
                db.collection(COLLECTION_NAME).document(old).delete().get(); //delete player from data
                db.collection(COLLECTION_NAME).document(username).set(use).get();
                SignupObject lo = new SignupObject(action, 0, true);
                //System.out.println();
                return lo;
            }
            return new SignupObject(action, -1, false);
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            return new SignupObject(action, -1, false);
        }
    }

    public static @Nullable HashMap<String, Object> getData(String username) {
        try {
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(username).get().get();
            if (!document.exists()) {
                System.out.println("Username doesn't exists!");
                return null;
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("password", document.get("password")); // Use hashed passwords in production
            data.put("games", (int) document.get("games", Integer.class));
            //data.put("loses", document.get("loses", Integer.class));
            data.put("name", username);
            data.put("type", document.get("type", Boolean.class));
            data.put("wins", (int) document.get("wins", Integer.class));
            data.put("wins-town", (int) document.get("wins-town", Integer.class));
            data.put("wins-werewolf", (int) document.get("wins-werewolf", Integer.class));
            data.put("wins-tanner", (int) document.get("wins-tanner", Integer.class));
            data.put("join-date", document.get("join-date", Long.class));
            //int[] chars = new int[12];
            List<Integer> chars = (List<Integer>) document.get("characters");
            data.put("characters", chars);
            return data;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setData(String username, Map<String, Object> data) {
        try {
            db.collection(COLLECTION_NAME).document(username).delete().get(); //delete player from data
            db.collection(COLLECTION_NAME).document(username).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
