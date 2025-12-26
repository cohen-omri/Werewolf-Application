package game;

import client_objects.ChatMessage;
import client_objects.LobbyObj;
import client_objects.LoginObj;
import client_objects.StringMessage;
import database.Connection;
import objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server_objects.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static game.GameManager.activeGames;
import static game.GameManager.admins;
import static game.Server.*;

// Handles individual client requests in a separate thread
public class ObjectHandler implements Runnable {
    private final Socket socket;
    private UUID uuid;
    public ObjectOutputStream out;
    public ObjectInputStream in;

    public ObjectHandler(Socket socket) {
        this.socket = socket;
        this.uuid = null;
    }

    @Override
    public void run() {

        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            this.in = in;
            this.out = out;
            while (true) {
                Object receivedObject = in.readObject();
                System.out.println("Received object: " + receivedObject + " ,socket: " + socket);

                Message received = gson.fromJson(receivedObject.toString(), Message.class);

                Message send = null;
                if (received != null) {
                    int a = received.getAction();
                    Action act = Action.fromInt(a);
                    switch (act) {
                        case ERROR: {
                            //need to handle
                            break;
                        }
                        case SIGNUP: {

                            LoginObj obj = gson.fromJson
                                    (receivedObject.toString(), LoginObj.class);
                            send = Connection.signUp(obj.getUsername(), obj.getPassword(), 1, null, this);
                            break;
                        }
                        case LOGIN: {
                            LoginObj obj = gson.fromJson
                                    (receivedObject.toString(), LoginObj.class);
                            send = Connection.login(obj.getUsername(), obj.getPassword(), 2);
                            uuid = UUID.randomUUID();
                            send.setUuid(uuid);
                            for (Player p : players.values()) {
                                if (Objects.equals(p.getName(), obj.getUsername())) {
                                    //if (!p.isConnected()) break;
                                    send = new LoginObject(2, 8, false, uuid,
                                            new HashMap<>());
                                    break;
                                }
                            }
                            if (send.getMsg() != 0) break;

                            players.put(uuid, new Player(obj.getUsername(), socket, uuid, this,
                                    ((LoginObject) send).getData()));
                            break;
                        }
                        case UPDATE_USER: {
                            LoginObj obj = gson.fromJson
                                    (receivedObject.toString(), LoginObj.class);
                            send = Connection.signUp(obj.getUsername(), obj.getPassword(), 3, received.getUuid(), this);
                            if (((SignupObject) send).isAck())
                                send = Connection.login(obj.getUsername(), obj.getPassword(), 3);
                            send.setMsg(send.getMsg() + 2);
                            send.setUuid(received.getUuid());
                            System.out.println(send);
                            break;
                        }
                        case LOGOUT: {
                            players.remove(received.getUuid());
                            send = new Message(4, 0, received.getUuid());
                            break;
                        }
                        case CREATE: {
                            LobbyObj obj = gson.fromJson(receivedObject.toString()
                                    , LobbyObj.class);
                            Player p = players.get(obj.getUuid());
                            String code = manager.createGame(p, obj.getActionTime(),
                                    obj.getVotingTime(), obj.getDiscussionTime(), obj.getMaxPlayers());
                            Game g = activeGames.get(code);
                            boolean s = p == g.getCreator();
                            ArrayList<String> names = new ArrayList<>();
                            for (Player r : g.getPlayers()) names.add(r.getName());
                            send = new LobbyObject(0, obj.getUuid(), g.getMaxPlayers(),
                                    g.getActionTime(), g.getDiscussionTime(), g.getVotingTime(),
                                    g.getChat(), g.getActiveRoles(), g.getCode(), names, s);
                            System.out.println(send + " " + g.getCode());
                            break;
                        }
                        case JOIN: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = activeGames.get(obj.getString().toUpperCase());
                            if (g == null) {
                                send = new LobbyObject(1, obj.getUuid(), 0,
                                        0, 0, 0,
                                        null, null
                                        , "", null, false);
                                break;
                            }

                            boolean b = manager.addPlayer(g.getCode(), p);
                            if (b) {
                                boolean sr = p == g.getCreator();
                                if (g.getCreator() == null) {
                                    g.setCreator(p);
                                    sr = true;
                                }
                                ArrayList<String> names = new ArrayList<>();
                                for (Player r : g.getPlayers()) names.add(r.getName());
                                send = new LobbyObject(0, obj.getUuid(), g.getMaxPlayers()
                                        , g.getActionTime(), g.getDiscussionTime()
                                        , g.getVotingTime(), g.getChat(), g.getActiveRoles()
                                        , g.getCode(), names, sr);

                                Thread t = new Thread(() ->
                                        g.getPlayers().forEach(player -> {
                                            try {
                                                if (player != p) {
                                                    ObjectOutputStream s = player.handler.out;

                                                    StringMessage sm = new StringMessage(7, 0,
                                                            player.getUuid(), p.getName());
                                                    s.writeObject(gson.toJson(sm));
                                                    s.flush();
                                                }
                                            } catch (IOException e) {
                                                System.out.println("Couldn't get O STREAM update " +
                                                        "socket of player " + player.getName());
                                            }
                                        }));
                                t.start();
                                for(Player pp : admins.keySet()) {
                                    if(!Objects.equals(admins.get(pp), g.getCode())) continue;

                                    ArrayList<String> pls = new ArrayList<>();
                                    for (Player ps : g.getPlayers()) pls.add(ps.getName());
                                    ObjectOutputStream o = pp.handler.out;
                                    try {
                                        StringMessage sm = new StringMessage(7, 0,
                                                pp.getUuid(), p.getName());
                                        o.writeObject(gson.toJson(sm));
                                        o.flush();
                                    } catch (IOException e) {
                                        continue;
                                    }
                                }
                            } else
                                send = new LobbyObject(2, obj.getUuid(), g.getMaxPlayers()
                                        , g.getActionTime(), g.getDiscussionTime()
                                        , g.getVotingTime(), g.getChat(), g.getActiveRoles()
                                        , "", null, false);

                            if (obj.getMsg() == 1) {
                                System.out.println(send);
                                out.writeObject(gson.toJson(send));
                                out.flush();
                                send = null;
                                ScheduledFuture<?> f = scheduler.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoginObject lo = new LoginObject(2, 0, true, p.getUuid(),
                                                (HashMap<String, Object>) p.getData());
                                        try {
                                            //Message msg = send;
                                            out.writeObject(gson.toJson(lo));
                                            out.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, 2, TimeUnit.SECONDS);
                            }
                            break;
                        }
                        case ADD_PLAYER: {
                            // empty cuz I don't get it from the client, I send it.
                            break;
                        }
                        case REMOVE_PLAYER: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = activeGames.get(obj.getString());
                            if (g == null) break;

                            manager.removePlayer(g.getCode(), p);
                            Thread t = new Thread(() ->
                                    g.getPlayers().forEach(player -> {
                                        try {
                                            ObjectOutputStream s = player.handler.out;
                                            StringMessage sm = new StringMessage(8, 0,
                                                    player.getUuid(), p.getName());
                                            s.writeObject(gson.toJson(sm));
                                            s.flush();
                                        } catch (IOException e) {
                                            System.out.println("Couldn't get O STREAM update " +
                                                    "socket of player " + player.getName());
                                        }
                                    }));
                            t.start();
                            for(Player pp : admins.keySet()) {
                                if(!Objects.equals(admins.get(pp), g.getCode())) continue;

                                ArrayList<String> pls = new ArrayList<>();
                                for (Player ps : g.getPlayers()) pls.add(ps.getName());
                                ObjectOutputStream o = pp.handler.out;
                                try {
                                    StringMessage sm = new StringMessage(8, 0,
                                            pp.getUuid(), p.getName());
                                    o.writeObject(gson.toJson(sm));
                                    o.flush();
                                } catch (IOException e) {
                                    continue;
                                }
                            }
                            break;
                        }
                        case ADD_MESSAGE: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = manager.findGameByPlayer(p);
                            if (g == null) break;
                            ChatMessage cm = new ChatMessage(p.getName(), obj.getString());
                            g.getChat().add(cm);
                            //p.getSocket().sendUrgentData();

                            Thread t = new Thread(() ->
                                    g.getPlayers().forEach(player -> {
                                        try {
                                            ObjectOutputStream s = player.handler.out;
                                            String js = gson.toJson(cm);
                                            StringMessage sm = new StringMessage(9, 0,
                                                    player.getUuid(), js);
                                            s.writeObject(gson.toJson(sm));
                                            s.flush();
                                        } catch (IOException e) {
                                            System.out.println("Couldn't get O STREAM update " +
                                                    "socket of player " + player.getName());
                                        }
                                    }));
                            t.start();
                            break;
                        }
                        case TOGGLE_ROLE: {
                            // msg: id to change
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Game g = activeGames.get(obj.getString());
                            if (g == null) {
                                send = new Message(10, -1, obj.getUuid());
                                break;
                            }
                            if (g.getCreator().getUuid() != uuid) {
                                send = new Message(10, -1, obj.getUuid());
                                break;
                            }
                            g.getActiveRoles().put
                                    (obj.getMsg(), !g.getActiveRoles().get(obj.getMsg()));
                            send = null;

                            Thread t = new Thread(() ->
                                    g.getPlayers().forEach(player -> {
                                        try {
                                            ObjectOutputStream s = player.handler.out;

                                            Message sm = new Message(10, obj.getMsg(),
                                                    player.getUuid());
                                            s.writeObject(gson.toJson(sm));
                                            s.flush();
                                        } catch (IOException e) {
                                            System.out.println("Couldn't get O STREAM update " +
                                                    "socket of player " + player.getName());
                                        }
                                    }));
                            t.start();
                            for(Player pp : admins.keySet()) {
                                if(!Objects.equals(admins.get(pp), g.getCode())) continue;

                                ArrayList<String> pls = new ArrayList<>();
                                for (Player ps : g.getPlayers()) pls.add(ps.getName());
                                ObjectOutputStream o = pp.handler.out;
                                try {
                                    Message sm = new Message(10, obj.getMsg(),
                                            pp.getUuid());
                                    o.writeObject(gson.toJson(sm));
                                    o.flush();
                                } catch (IOException e) {
                                    continue;
                                }
                            }
                            break;
                        }
                        case START_GAME: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Game g = activeGames.get(obj.getString());
                            if (g == null) {
                                send = new Message(11,-1, obj.getUuid());
                                break;
                            }
                            if (g.getCreator().getUuid() != uuid) {
                                send = new Message(11,-1,obj.getUuid());
                                break;
                            }
                            int size = 0;
                            /*if(g.getPlayers().size() < 3) { // any game requires at least 3 players.
                                send = new Message(11,-3,obj.getUuid());
                                break;
                            }*/
                            // todo RETURNNNNNNNNNNNNNNNNN
                            for(int i : g.getActiveRoles().keySet()) if(g.getActiveRoles().get(i)) size++;
                            if(size < g.getPlayers().size() + 3) {
                                send = new Message(11,-2, obj.getUuid());
                                break;
                            }
                            send = new Message(11,1, obj.getUuid());
                            String json = gson.toJson(send);
                            out.writeObject(json);
                            out.flush();
                            send = null;

                            manager.changeState(g.getCode());
                            break;
                        }
                        case SET_TURN: {
                            //im sending not receiving
                            break;
                        }
                        case SHOW_CARD_NAME: {
                            //todo
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = manager.findGameByPlayer(p);
                            if (g == null) break;

                            String name = obj.getString();
                            if (name.equals("0") || name.equals("1") || name.equals("2")) {
                                CardType t = g.getMiddleCards().get(Integer.parseInt(name));
                                if (obj.getMsg() == 1) {
                                    g.setCurrentDopple(t);
                                }
                                StringMessage msg = new StringMessage(13, t.getType(),
                                        p.getUuid(), name);
                                ObjectOutputStream o = p.handler.out;
                                o.writeObject(gson.toJson(msg));
                                o.flush();
                                break;
                            }
                            UUID u = getUUIDByName(name);
                            CardType t = g.getFinalRoles().get(u).getRole();
                            if (obj.getMsg() == 1) {
                                g.setCurrentDopple(t);
                            }
                            StringMessage msg = new StringMessage(13, t.getType(), p.getUuid()
                                    , name);
                            ObjectOutputStream o = p.handler.out;
                            o.writeObject(gson.toJson(msg));
                            o.flush();
                            break;
                        }
                        case SWITCH_CARDS: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = manager.findGameByPlayer(p);
                            if (g == null) break;

                            String name = obj.getString();
                            String[] names = name.split(";");
                            if (names.length == 0) break;
                            if (names.length == 1) break;
                            UUID u1 = getUUIDByName(names[0]);
                            UUID u2 = getUUIDByName(names[1]);
                            Card t1 = g.getFinalRoles().get(u1);
                            Card t2 = g.getFinalRoles().get(u2);
                            if (u1 == null || t1 == null) {
                                if (names[0].equals("0") || names[0].equals("1")
                                        || names[0].equals("2")) {
                                    t1 = new Card(g.getMiddleCards().get
                                            (Integer.parseInt(names[0])));
                                } else break;
                            }
                            if (u2 == null || t2 == null) {
                                if (names[1].equals("0") || names[1].equals("1")
                                        || names[1].equals("2")) {
                                    t2 = new Card(g.getMiddleCards().get
                                            (Integer.parseInt(names[1])));
                                } else break;
                            }

                            g.getFinalRoles().put(u2, new Card(t1.getRole()));
                            g.getFinalRoles().put(u1, new Card(t2.getRole()));
                            break;
                        }
                        case START_DISCUSSION: {
                            //empty for now
                            break;
                        }
                        case START_VOTING: {
                            Player p = players.get(received.getUuid());
                            Game g = manager.findGameByPlayer(p);
                            if (g == null) break;

                            if (Objects.equals(g.getCreator().getName(), p.getName())) {
                                if (g.getDiscussionFuture() != null) {
                                    g.getDiscussionFuture().cancel(true);
                                    manager.changeState(g.getCode());
                                }
                            }
                            send = new Message(16,-1,p.getUuid());
                            break;
                        }
                        case VOTE: {
                            StringMessage obj = gson.fromJson(receivedObject.toString(),
                                    StringMessage.class);
                            Player p = players.get(obj.getUuid());
                            Game g = manager.findGameByPlayer(p);
                            if (g == null) break;

                            String name = obj.getString();
                            Player vote = null;
                            try {
                                vote = players.get(getUUIDByName(name));
                            } catch (NullPointerException e) {
                                System.err.println(e.getMessage());
                            }
                            if (vote == null) break;

                            g.getVotes().put(vote.getUuid(), (g.getVotes().get(vote.getUuid()) == null
                                    ? 0 : g.getVotes().get(vote.getUuid()) ) + 1);
                            g.getVotesByUser().put(p.getUuid(),vote.getUuid());
                            int votes = g.getVotes().values().stream().mapToInt(i -> i).sum();
                            if (votes == g.getPlayers().size()) {
                                g.getVotingFuture().cancel(true);
                                if (g.getEndingFuture() != null)
                                    g.getEndingFuture().cancel(true);
                                Game game = g;
                                HashMap<String, CardType> roles = new HashMap<>();
                                int maxVotes = 0;
                                UUID outt = null;
                                for (UUID uuid : game.getVotes().keySet()) {
                                    if (maxVotes <= game.getVotes().get(uuid)) {
                                        maxVotes = game.getVotes().get(uuid);
                                        outt = uuid;
                                    }
                                    roles.put(players.get(uuid).getName(), game.getFinalRoles().get(uuid).getRole());
                                }
                                for (int i = 0; i < game.getMiddleCards().size(); i++) {
                                    CardType type = game.getMiddleCards().get(i);
                                    roles.put(i + "", type);
                                }

                                ArrayList<String> pls = new ArrayList<>();
                                for (Player ps : game.getPlayers()) pls.add(ps.getName());
                                for (Player ps : game.getGhosts()) pls.add(ps.getName());

                                Player pOut = players.get(outt);
                                if(pOut == null) { pOut = game.getPlayers().getFirst(); outt = pOut.getUuid(); }
                                Card car = game.getFinalRoles().get(outt);

                                if(car.getRole() == CardType.HUNTER) {
                                    UUID uui = game.getVotesByUser().get(pOut.getUuid());
                                    if(uui == null) uui = pOut.getUuid();

                                    pOut = players.get(uui);
                                    outt = uui;
                                    car = game.getFinalRoles().get(outt);
                                }
                                if (car.getRole() == CardType.DOPPLEGANGER) {
                                    if(game.getCurrentDopple() == CardType.WEREWOLF || game.getCurrentDopple() == CardType.MINION) car.setTeam(CardTeam.WEREWOLF);
                                    else if (game.getCurrentDopple() == CardType.TANNER) {
                                        car.setTeam(CardTeam.TANNER);
                                    } else car.setTeam(CardTeam.VILLAGE);
                                }


                                game.setChosen(pOut);
                                Player finalPOut = pOut;
                                Card finalCar = car;
                                game.getPlayers().forEach(pl -> {
                                    try {
                                        ObjectOutputStream s = pl.handler.out;
                                        boolean iswin = false;
                                        Card cur = game.getFinalRoles().get(pl.getUuid());
                                        if (cur.getRole() == CardType.DOPPLEGANGER) {
                                            if(game.getCurrentDopple() == CardType.WEREWOLF || game.getCurrentDopple() == CardType.MINION) cur.setTeam(CardTeam.WEREWOLF);
                                            else if (game.getCurrentDopple() == CardType.TANNER) {
                                                cur.setTeam(CardTeam.TANNER);
                                            } else cur.setTeam(CardTeam.VILLAGE);
                                        }
                                        if (cur.getTeam() == CardTeam.VILLAGE && finalCar.getRole() == CardType.WEREWOLF) {
                                            iswin = true;
                                        } else if (cur.getTeam() == CardTeam.WEREWOLF && finalCar.getTeam() == CardTeam.VILLAGE ||
                                                cur.getTeam() == CardTeam.WEREWOLF && finalCar.getRole() == CardType.MINION) {
                                            iswin = true;
                                        } else if (finalCar.getTeam() == CardTeam.TANNER && pl.getUuid() == finalPOut.getUuid()) {
                                            iswin = true;
                                        }
                                        ResultsObject sm = new ResultsObject(18, 0,
                                                pl.getUuid(), roles, pls, finalPOut.getName(), iswin, game.getCode());

                                        s.writeObject(gson.toJson(sm));
                                        s.flush();
                                    } catch (IOException e) {
                                        System.out.println("Couldn't get O STREAM update socket of player " + pl.getName());
                                    }
                                });

                                game.setState(GameState.ENDING);
                                manager.changeState(g.getCode());
                            } else {
                                Thread t = new Thread(() ->
                                        g.getPlayers().forEach(player -> {
                                            try {
                                                ObjectOutputStream s = player.handler.out;
                                                Message sm = new Message(17, votes,
                                                        player.getUuid());
                                                s.writeObject(gson.toJson(sm));
                                                s.flush();
                                            } catch (IOException e) {
                                                System.out.println("Couldn't get O STREAM update " +
                                                        "socket of player " + player.getName());
                                            }
                                        }));
                                t.start();
                            }
                            break;
                        }
                        case START_ENDING: {
                            //empty for now
                            break;
                        }
                        case ADMIN_GAMES: {
                            if(received.getMsg() == 0) {
                                StringMessage sm = gson.fromJson(receivedObject.toString(), StringMessage.class);
                                Game g = activeGames.get(sm.getString());
                                Player p = players.get(uuid);
                                if (p == null || !((boolean) p.getData().get("type")) || g == null) {
                                    send = null;
                                    break;
                                } else {
                                    ArrayList<String> pls = new ArrayList<>();
                                    for (Player ps : g.getPlayers()) pls.add(ps.getName());
                                    AdminObject ad = new AdminObject(19, 0, uuid, new LobbyObject(0, uuid, g.getMaxPlayers(),
                                            g.getActionTime(), g.getDiscussionTime(), g.getVotingTime(), g.getChat(), g.getActiveRoles(), g.getCode(), pls, false), g.getState());
                                    send = ad;
                                    admins.put(p,sm.getString());
                                }
                            } else if (received.getMsg() == 1) {
                                Player p = players.get(uuid);
                                if (p == null || !((boolean) p.getData().get("type"))) {
                                    send = null;
                                    break;
                                }
                                admins.remove(p);
                            }
                            break;
                        }
                        case ADMIN_CODES: {
                            Player p = players.get(uuid);
                            if(p == null || !((boolean) p.getData().get("type"))) {
                                send = null;
                                break;
                            }
                            String m = "";
                            for(String code : activeGames.keySet()) {
                                Game g = activeGames.get(code);
                                if(g.getPlayers().isEmpty()) continue;
                                m = m.concat(code + ";");
                            }
                            send = new StringMessage(20,0,uuid,m);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
                if (send != null) {
                    String json = gson.toJson(send);
                    out.writeObject(json);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            try {
                socket.close();
                System.out.println("Socket closed.");
            } catch (IOException ex) {
                //System.err.println(ex);
            }
            if(uuid == null) return;
            Game g = manager.findGameByPlayer(players.get(uuid));
            if (g != null) {
                if (g.getState() != GameState.WAITING) {
                    g.addGhost(players.get(uuid));
                    //todo send to clients a new ghost.
                } else {
                    manager.removePlayer(g.getCode(), players.get(uuid));
                }
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                //log.error("e: ", e);
            }
        }
    }

    @Nullable
    public static UUID getUUIDByName(String value) {
        for (Map.Entry<UUID, Player> entry : players.entrySet()) {
            if (Objects.equals(entry.getValue().getName(), value)) {
                return entry.getKey();
            }
        }
        return null;
    }



}
