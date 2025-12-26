package game;

import client_objects.StringMessage;
import com.google.gson.Gson;
import objects.Player;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.*;

import static database.FirestoreConfig.getFirestore;

public class Server {

    private static final int PORT = 12345;
    private static final ExecutorService requestHandlerPool = Executors.newCachedThreadPool();
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static Gson gson = new Gson();

    public static ConcurrentMap<UUID, Player> players = new ConcurrentHashMap<>();
    public static GameManager manager;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("game.Server listening on port " + PORT);
            getFirestore();
            manager = new GameManager();
            startPinging(2);

            while (true) {
                // Listening thread waiting for connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection accepted. " + clientSocket.toString());

                // Spawn a thread to handle the received object
                requestHandlerPool.execute(new ObjectHandler(clientSocket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            requestHandlerPool.shutdown();
            scheduler.shutdown();
        }
    }

    public static void startPinging(long intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            for (Player p : players.values()) {
                if (!isClientAlive(p)) {
                    Game g = manager.findGameByPlayer(p);
                    System.out.println("Client disconnected: " + p.getName());
                    players.remove(p.getUuid());
                    if (g != null) {
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
                    }
                    // Optionally remove or handle the dead socket here
                }
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public static boolean isClientAlive(Player p) {
        try {
            //client.sendUrgentData(0xFF); // Silent ping
            p.handler.out.writeObject("PING");
            p.handler.out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

