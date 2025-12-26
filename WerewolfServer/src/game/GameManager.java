package game;

import client_objects.StringMessage;
import objects.Card;
import objects.CardTeam;
import objects.CardType;
import objects.Player;
import server_objects.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static database.Connection.setData;
import static game.Server.*;

public class GameManager {

    public final static Map<String, Game> activeGames = new HashMap<>();
    public final int codeLength = 4;

    // min max length
    public final static int minLength = 4;
    public final static int maxLength = 12;

    public final static Map<Player, String> admins = new HashMap<>();

    public GameManager() {

    }

    /**
     * creates a new game and adds to active games
     *
     * @param p the creator of the game
     * @return the code of the game.
     * @author Omri
     */
    public String createGame(Player p, int actTime, int votTime, int discTime, int num) throws IOException { // creator
        ArrayList<Player> players = new ArrayList<>();
        players.add(p);
        String code = generateGameCode();
        Game game = new Game(GameState.WAITING, code, num, players, p, actTime, discTime, votTime);
        activeGames.put(code, game);
        return code;
    }

    public Game findGameByPlayer(Player p) {
        if (p == null) return null;

        for (Game g : activeGames.values()) {
            if (g.hasPlayer(p) || g.hasGhost(p)) return g;
        }
        return null;
    }

    /**
     * removes an existing game if in waiting state (not in-game)
     *
     * @param code the code of the game
     */
    public void removeGame(String code) {
        Game game = activeGames.get(code);
        if (game != null && !game.hasStarted()) activeGames.remove(code);

        if (game != null) activeGames.remove(code);
    }

    public void changeState(String code) {
        Game game = activeGames.get(code);
        if (game == null) return;
        ScheduledFuture<?> future = null;

        Thread tt = new Thread(() -> sendGameStateChange(code, game.getState()));
        tt.start();
        switch (game.getState()) {
            case WAITING -> {
                game.start();
                changeState(code);
                break;
            }
            case STARTING -> {
                List<CardType> availableRoles = new ArrayList<>();
                boolean iswerewolf = false;
                for (Map.Entry<Integer, Boolean> entry : game.getActiveRoles().entrySet()) {
                    if (entry.getValue()) {
                        availableRoles.add(CardType.cardFromInt(entry.getKey()));
                        if (entry.getKey() == 1 || entry.getKey() == 2) iswerewolf = true;
                    }
                }
                Collections.shuffle(availableRoles); // Shuffle to randomize roles
                availableRoles = availableRoles.reversed();
                //System.out.println(availableRoles.toString());
                if (iswerewolf) { // Forcing werewolf into the game if at least one is active.
                    availableRoles.remove(CardType.WEREWOLF);
                    availableRoles.addFirst(CardType.WEREWOLF);
                    System.err.println("WEREWOLF");
                }
                System.out.println(availableRoles.toString());


                //System.out.println(availableRoles.toString());
                Iterator<CardType> roleIterator = availableRoles.iterator();
                System.out.println(roleIterator.toString());
                ArrayList<String> names = new ArrayList<>();

                ArrayList<Player> playes = new ArrayList<>();
                for (Player r : game.getPlayers()) playes.add(r);
                Collections.shuffle(playes);
                for (Player r : playes) names.add(r.getName());
                Thread t = new Thread(() -> {
                    for (Player p : playes) {

                        try {
                            ObjectOutputStream s = p.handler.out;
                            CardType assignedRole = roleIterator.hasNext() ? roleIterator.next() : CardType.VILLAGER;
                            GameObject sm = new GameObject(0, p.getUuid(), names, assignedRole, game.getActionTime(),
                                    game.getDiscussionTime(), game.getVotingTime(), (Objects.equals(game.getCreator().getName(), p.getName())));
                            game.getCurrentRoles().put(p.getUuid(), new Card(assignedRole));
                            game.getFinalRoles().put(p.getUuid(), new Card(assignedRole));

                            s.writeObject(gson.toJson(sm));
                            s.flush();
                        } catch (IOException e) {
                            System.err.println("Couldn't get O STREAM update " + "socket of player " + p.getName());
                        }

                    }
                    game.getMiddleCards().add(roleIterator.next());
                    game.getMiddleCards().add(roleIterator.next());
                    game.getMiddleCards().add(roleIterator.next());
                });
                t.start();

                game.setState(GameState.PLAYING);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //todo something.
                        CardType type = nextTurn(code);
                        switch (type) {
                            case DOPPLEGANGER -> {
                                sendTurnAction(game, 0);

                                break;
                            }
                            case WEREWOLF -> {
                                sendTurnAction(game, 1);
                                Thread t = new Thread(() -> {
                                    ArrayList<UUID> werewolves = new ArrayList<>();
                                    for (UUID u : game.getCurrentRoles().keySet()) {
                                        if (game.getCurrentRoles().get(u).getRole() == CardType.WEREWOLF) {
                                            werewolves.add(u);
                                        } else if (game.getCurrentRoles().get(u).getRole() == CardType.DOPPLEGANGER
                                                && game.getCurrentDopple() == CardType.WEREWOLF) {
                                            werewolves.add(u);
                                        }
                                    }
                                    String were = "";
                                    for (UUID u : werewolves) {
                                        were = were.concat(players.get(u).getName() + ";");
                                    }
                                    if (!were.isEmpty()) were = were.substring(0, were.length() - 1);

                                    try {
                                        for (int i = 0; i < werewolves.size(); i++) {
                                            Player p = players.get(werewolves.get(i));
                                            ObjectOutputStream o = p.handler.out;
                                            int msg = (werewolves.size() == 1) ? 0 : 1;
                                            o.writeObject(gson.toJson(new StringMessage(13, msg, p.getUuid(), were
                                                    .replace(p.getName() + ";", "").replace(p.getName(), ""))));
                                            o.flush();
                                        }
                                    } catch (IOException E) {
                                        System.err.println(E.getMessage());
                                    }
                                });
                                t.start();
                                break;
                            }
                            case MINION -> {
                                sendTurnAction(game, 2);
                                Thread t = new Thread(() -> {
                                    ArrayList<UUID> werewolves = new ArrayList<>();
                                    for (UUID u : game.getCurrentRoles().keySet()) {
                                        if (game.getCurrentRoles().get(u).getRole() == CardType.WEREWOLF) {
                                            werewolves.add(u);
                                        } else if (game.getCurrentRoles().get(u).getRole() == CardType.DOPPLEGANGER
                                                && game.getCurrentDopple() == CardType.WEREWOLF) {
                                            werewolves.add(u);
                                        }
                                    }
                                    List<Player> ps = new ArrayList<>();
                                    for (UUID uuid : game.getCurrentRoles().keySet()) {
                                        if (game.getCurrentRoles().get(uuid).getRole() == CardType.MINION) {
                                            ps.add(players.get(uuid));
                                        } else if (game.getCurrentRoles().get(uuid).getRole() == CardType.DOPPLEGANGER
                                                && game.getCurrentDopple() == CardType.MINION) {
                                            ps.add(players.get(uuid));
                                        }
                                    }
                                    String were = "";
                                    for (UUID u : werewolves) {
                                        were = were.concat(players.get(u).getName() + ";");
                                    }
                                    if (!were.isEmpty()) were = were.substring(0, were.length() - 1);
                                    for (Player p : ps) {
                                        if (p != null) {
                                            try {
                                                ObjectOutputStream o = p.handler.out;
                                                if (werewolves.isEmpty()) {
                                                    o.writeObject(gson.toJson(new StringMessage(13, 1, p.getUuid(), "")));
                                                    o.flush();
                                                    continue;
                                                }
                                                o.writeObject(gson.toJson(new StringMessage(13, 1, p.getUuid(), were)));
                                                o.flush();
                                            } catch (IOException E) {
                                                System.err.println(E.getMessage());
                                            }
                                        }
                                    }
                                });
                                t.start();
                                break;
                            }
                            case MASON -> {
                                sendTurnAction(game, 3);
                                Thread t = new Thread(() -> {
                                    ArrayList<UUID> masons = new ArrayList<>();
                                    for (UUID u : game.getCurrentRoles().keySet()) {
                                        if (game.getCurrentRoles().get(u).getRole() == CardType.MASON) {
                                            masons.add(u);
                                        } else if (game.getCurrentRoles().get(u).getRole() == CardType.DOPPLEGANGER
                                                && game.getCurrentDopple() == CardType.MASON) {
                                            masons.add(u);
                                        }
                                    }
                                    String mas = "";
                                    for (UUID u : masons) {
                                        mas = mas.concat(players.get(u).getName() + ";");
                                    }
                                    if (!mas.isEmpty()) mas = mas.substring(0, mas.length() - 1);

                                    try {
                                        for (int i = 0; i < masons.size(); i++) {
                                            Player p = players.get(masons.get(i));
                                            ObjectOutputStream o = p.handler.out;
                                            if (masons.size() == 1) {
                                                o.writeObject(gson.toJson(new
                                                        StringMessage(13, 3, p.getUuid(), "")));
                                                o.flush(); // alone
                                                break;
                                            } else {
                                                o.writeObject(gson.toJson(new
                                                        StringMessage(13, 3, p.getUuid(),
                                                        mas.replace(p.getName() + ";", "").replace(p.getName(), ""))
                                                ));
                                                o.flush();
                                            }
                                        }
                                    } catch (IOException E) {
                                        System.err.println(E.getMessage());
                                    }
                                });
                                t.start();
                                break;
                            }
                            case SEER -> {
                                sendTurnAction(game, 4);
                                break;
                            }
                            case ROBBER -> {
                                sendTurnAction(game, 5);
                                break;
                            }
                            case TROUBLEMAKER -> {
                                sendTurnAction(game, 6);
                                break;
                            }
                            case DRUNK -> {
                                sendTurnAction(game, 7);
                                break;
                            }
                            case INSOMNIAC -> {
                                sendTurnAction(game, 8);
                                Thread t = new Thread(() -> {
                                    ArrayList<Player> insos = new ArrayList<>();
                                    for (UUID u : game.getCurrentRoles().keySet()) {
                                        if (game.getCurrentRoles().get(u).getRole() == CardType.INSOMNIAC) {
                                            insos.add(players.get(u));
                                        } else if (game.getCurrentRoles().get(u).getRole() == CardType.DOPPLEGANGER
                                                && game.getCurrentDopple() == CardType.INSOMNIAC) {
                                            //if(players.get(u) != null)
                                            insos.add(players.get(u));
                                            //else insos.add(game.getGhosts());
                                        }
                                    }

                                    for (Player inso : insos) {
                                        try {
                                            if (inso != null) {
                                                ObjectOutputStream o = inso.handler.out;
                                                o.writeObject(gson.toJson(new
                                                        StringMessage(13, game
                                                        .getFinalRoles().get(inso.getUuid()).getRole().getType(),
                                                        inso.getUuid(), inso.getName())));
                                                o.flush(); // alone
                                            }
                                        } catch (IOException E) {
                                            System.err.println(E.getMessage());
                                        }
                                    }
                                });
                                t.start();
                                break;
                            }
                            case VILLAGER -> {
                                type = null;
                                changeState(code);
                                break;
                            }
                            case null, default -> {
                                game.getActionFuture().cancel(true);
                                break;
                            }
                        }
                    }
                };
                future = scheduler.scheduleAtFixedRate(runnable, 1, game.getActionTime(), TimeUnit.SECONDS);
                game.setActionFuture(future);
                System.out.println("set action future " + future.state());

                break;
            }
            case PLAYING -> {
                sendStateChange(game, 15);
                game.getActionFuture().cancel(true);
                game.setState(GameState.DISCUSSION);
                ScheduledFuture<?> f = scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        //DO Discussion
                        changeState(game.getCode());
                    }
                }, game.getDiscussionTime(), TimeUnit.SECONDS);
                game.setDiscussionFuture(f);
                break;
            }
            case DISCUSSION -> {
                sendStateChange(game, 16);
                game.setState(GameState.VOTING);
                for (Player p : game.getPlayers()) {
                    game.getVotes().put(p.getUuid(), 0);
                }
                ScheduledFuture<?> f = scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        //voting
                        changeState(game.getCode());
                    }
                }, game.getVotingTime(), TimeUnit.SECONDS);
                game.setVotingFuture(f);
                break;
            }
            case VOTING -> {
                //sendStateChange(game, 18);
                //game.setState(GameState.ENDING);
                ScheduledFuture<?> fut = scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        //DO ending
                        HashMap<String, CardType> roles = new HashMap<>();
                        int maxVotes = 0;
                        UUID out = null;
                        for (UUID uuid : game.getVotes().keySet()) {
                            if (maxVotes <= game.getVotes().get(uuid)) {
                                maxVotes = game.getVotes().get(uuid);
                                out = uuid;
                            }
                            roles.put(players.get(uuid).getName(), game.getFinalRoles().get(uuid).getRole());
                        }
                        for (int i = 0; i < game.getMiddleCards().size(); i++) {
                            CardType type = game.getMiddleCards().get(i);
                            roles.put(i + "", type);
                        }

                        ArrayList<String> pls = new ArrayList<>();
                        for (Player p : game.getPlayers()) pls.add(p.getName());
                        for (Player p : game.getGhosts()) pls.add(p.getName());

                        Player pOut = players.get(out);
                        if (pOut == null) {
                            pOut = game.getPlayers().getFirst();
                            out = pOut.getUuid();
                        }
                        Card car = game.getFinalRoles().get(out);

                        if (car.getRole() == CardType.HUNTER) {
                            UUID uui = game.getVotesByUser().get(pOut.getUuid());
                            if (uui == null) uui = pOut.getUuid();

                            pOut = players.get(uui);
                            out = uui;
                            car = game.getFinalRoles().get(out);
                        }
                        if (car.getRole() == CardType.DOPPLEGANGER) {
                            if (game.getCurrentDopple() == CardType.WEREWOLF || game.getCurrentDopple() == CardType.MINION)
                                car.setTeam(CardTeam.WEREWOLF);
                            else if (game.getCurrentDopple() == CardType.TANNER) {
                                car.setTeam(CardTeam.TANNER);
                            } else car.setTeam(CardTeam.VILLAGE);
                        }
                        game.setChosen(pOut);

                        Player finalPOut = pOut;
                        Card finalCar = car;
                        game.getPlayers().forEach(p -> {
                            try {
                                ObjectOutputStream s = p.handler.out;
                                boolean iswin = false;
                                Card cur = game.getFinalRoles().get(p.getUuid());
                                if (cur.getRole() == CardType.DOPPLEGANGER) {
                                    if (game.getCurrentDopple() == CardType.WEREWOLF || game.getCurrentDopple() == CardType.MINION)
                                        cur.setTeam(CardTeam.WEREWOLF);
                                    else if (game.getCurrentDopple() == CardType.TANNER) {
                                        cur.setTeam(CardTeam.TANNER);
                                    } else cur.setTeam(CardTeam.VILLAGE);
                                }
                                if (cur.getTeam() == CardTeam.VILLAGE && finalCar.getRole() == CardType.WEREWOLF) {
                                    iswin = true;
                                } else if (cur.getTeam() == CardTeam.WEREWOLF && finalCar.getTeam() == CardTeam.VILLAGE ||
                                        cur.getTeam() == CardTeam.WEREWOLF && finalCar.getRole() == CardType.MINION) {
                                    iswin = true;
                                } else if (finalCar.getTeam() == CardTeam.TANNER && p.getUuid() == finalPOut.getUuid()) {
                                    iswin = true;
                                }
                                ResultsObject sm = new ResultsObject(18, 0,
                                        p.getUuid(), roles, pls, finalPOut.getName(), iswin, game.getCode());

                                s.writeObject(gson.toJson(sm));
                                s.flush();
                            } catch (IOException e) {
                                System.out.println("Couldn't get O STREAM update socket of player " + p.getName());
                            }
                        });

                        game.setState(GameState.ENDING);
                        changeState(game.getCode());
                    }
                }, 0, TimeUnit.SECONDS);
                game.setEndingFuture(fut);
                break;
            }
            case ENDING -> {
                game.setState(GameState.WAITING);
                for (Player p : game.getPlayers())
                    manager.updateStatistic(game, p);
                //send to lobby
                game.setCurrentDopple(CardType.DOPPLEGANGER);
                for (Player p : game.getPlayers()) {
                    for (Player pp : admins.keySet()) {
                        if (!Objects.equals(admins.get(pp), game.getCode())) continue;

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
                }
                game.getPlayers().clear();
                game.getGhosts().clear();
                game.setCurrentTurn(null);
                game.setCreator(null);
                game.setChosen(null);
                game.setVotes(new HashMap<>());
                game.setDiscussionFuture(null);
                game.setActionFuture(null);
                game.setEndingFuture(null);
                game.setVotingFuture(null);
                game.setCurrentRoles(new HashMap<>());
                game.setFinalRoles(new HashMap<>());
                game.setMiddleCards(new ArrayList<>());
                break;
            }
        }
    }

    /**
     * generates a random game code with length of codeLength
     *
     * @return a random generated game code
     * @author Omri
     */
    public String generateGameCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // Generate the random code
        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    public void sendGameStateChange(String code, GameState state) {
        for (Player p : admins.keySet()) {
            if (!Objects.equals(admins.get(p), code)) continue;

            Game g = activeGames.get(code);
            if (g == null) continue;
            ArrayList<String> pls = new ArrayList<>();
            for (Player ps : g.getPlayers()) pls.add(ps.getName());
            AdminObject ad = new AdminObject(19, 0, p.getUuid(), new LobbyObject(0, p.getUuid(), g.getMaxPlayers(),
                    g.getActionTime(), g.getDiscussionTime(), g.getVotingTime(), g.getChat(), g.getActiveRoles(), g.getCode(), pls, false), state);
            ObjectOutputStream o = p.handler.out;
            try {
                o.writeObject(gson.toJson(ad));
                o.flush();
            } catch (IOException e) {
                continue;
            }
        }
    }

    @Deprecated
    public UUID getUUIDByRole(Game g, CardType type) {
        if (g == null) return null;
        for (UUID u : g.getCurrentRoles().keySet()) {
            if (g.getCurrentRoles().get(u).getRole() == type) return u;
        }
        return null;
    }

    public void sendTurnAction(Game g, int msg) {
        Thread t = new Thread(() -> {
            for (Player p : g.getPlayers()) {
                ObjectOutputStream o = p.handler.out;
                Message m = new Message(12, msg, p.getUuid());
                try {
                    o.writeObject(gson.toJson(m));
                    o.flush();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        t.start();
    }

    /**
     * adds player to game, doesn't if above max,
     * if player is already in game then removes "ghost" title
     *
     * @param code the game code
     * @param p    the player
     * @author Omri
     */
    public boolean addPlayer(String code, Player p) {
        Game game = activeGames.get(code);
        if (game == null) return false;

        //if (game.hasStarted()) return false;

        if (game.hasStarted() && game.hasGhost(p)) game.removeGhost(p);
        else if (!game.hasStarted()) {
            game.addPlayer(p);
            return true;
        }
        return false;
    }

    /**
     * removes a player from a given game if exists there.
     *
     * @param code the game code
     * @param p    the player
     * @author Omri
     */
    public void removePlayer(String code, Player p) {
        Game game = activeGames.get(code);
        if (game == null || game.hasStarted()) return;
        if (game.getPlayers().size() - 1 == 0) {
            removeGame(code);
            return;
        }
        game.removePlayer(p);
    }

    public void sendStateChange(Game game, int state) {
        for (Player p : game.getPlayers()) {
            Thread t = new Thread(() -> {
                try {
                    ObjectOutputStream s = p.handler.out;

                    Message sm = new Message(state, 0, p.getUuid());
                    s.writeObject(gson.toJson(sm));
                    s.flush();
                } catch (IOException e) {
                    System.err.println("Couldn't get O STREAM update " + "socket of player " + p.getName());
                }
            });
            t.start();
        }
    }

    public void updateStatistic(Game game, Player p) {
        if (game == null || p == null) return;
        //if(!p.isConnected()) return;

        CardType played = game.getCurrentRoles().get(p.getUuid()).getRole();
        Map<String, Object> data = p.getData();
        int wins = (int) data.get("wins");
        int wins_town = (int) data.get("wins-town");
        int wins_werewolf = (int) data.get("wins-werewolf");
        int wins_tanner = (int) data.get("wins-tanner");
        int games = (int) data.get("games");
        List<Number> plays = (List<Number>) data.get("characters");

        Number is = plays.get(played.getType());
        plays.set(played.getType(), is.intValue() + 1);
        games++;
        Card cur = new Card(played);
        Card car = game.getFinalRoles().get(game.getChosen().getUuid());
        if (cur.getTeam() == CardTeam.VILLAGE && car.getTeam() == CardTeam.WEREWOLF) {
            wins++;
            wins_town++;
        } else if (cur.getTeam() == CardTeam.WEREWOLF && car.getTeam() == CardTeam.VILLAGE) {
            wins++;
            wins_werewolf++;
        } else if (car.getTeam() == CardTeam.TANNER && p.getUuid() == game.getChosen().getUuid()) {
            wins++;
            wins_tanner++;
        }
        data.put("wins", wins);
        data.put("wins-town", wins_town);
        data.put("wins-werewolf", wins_werewolf);
        data.put("wins-tanner", wins_tanner);
        data.put("games", games);
        data.put("characters", plays);

        setData(p.getName(), data);
    }

    public CardType nextTurn(String code) {
        Game game = activeGames.get(code);
        if (game == null || !game.hasStarted()) return null;

        switch (game.getCurrentTurn()) {
            case DOPPLEGANGER -> {
                //tod
                game.setCurrentTurn(CardType.WEREWOLF);
                break;
            }
            case WEREWOLF -> {
                //todo
                game.setCurrentTurn(CardType.MINION);
                break;
            }
            case MINION -> {
                //todo i
                game.setCurrentTurn(CardType.MASON);
                break;
            }
            case MASON -> {
                //todo ii
                game.setCurrentTurn(CardType.SEER);
                break;
            }
            case SEER -> {
                //todo iii
                game.setCurrentTurn(CardType.ROBBER);
                break;
            }
            case ROBBER -> {
                //todo iv
                game.setCurrentTurn(CardType.TROUBLEMAKER);
                break;
            }
            case TROUBLEMAKER -> {
                //todo v
                game.setCurrentTurn(CardType.DRUNK);
                break;
            }
            case DRUNK -> {
                //todo vi
                game.setCurrentTurn(CardType.INSOMNIAC);
                break;
            }
            case INSOMNIAC -> {
                //todo vii
                //changeState(code);
                return CardType.VILLAGER;
            }
            case HUNTER, VILLAGER, TANNER -> {
                //todo nothing cuz no turn;
                return null;
            }
            case null -> {
                game.setCurrentTurn(CardType.DOPPLEGANGER);
                //return CardType.DOPPLEGANGER;
                break;
            }
        }
        //if(game.getActiveRoles().get(game.getCurrentTurn().getType()))
        HashMap<CardType, Boolean> roles = new HashMap<>();
        for (int i : game.getActiveRoles().keySet()) {
            if (i == 2 && roles.get(CardType.WEREWOLF)) continue;
            if (i == 5 && roles.get(CardType.MASON)) continue;

            roles.put(CardType.cardFromInt(i), game.getActiveRoles().get(i));
        }
        if (roles.get(game.getCurrentTurn())) {
            return game.getCurrentTurn();
        } else {
            return nextTurn(code);
        }

        //return CardType.VILLAGER;
    }
}
