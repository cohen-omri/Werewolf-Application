package com.example.werewolfclient.Objects;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.example.werewolfclient.ClientObjects.Message;
import com.google.gson.Gson;

public class SocketManager {
    private static Socket socket;
    private static final String serverAddress = "censored";
    private static final int port = 12345;
    public static AtomicReference<String> ip = new AtomicReference<>();
    public static final Gson gson = new Gson();
    private static boolean isConnected = false;
    private static ObjectOutputStream outStream;
    private static ObjectInputStream inStream;
    private static Thread listenerThread;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static ServerMessageListener listener;

    private SocketManager() {}

    public static synchronized void initializeSocket() throws IOException {
        if (isConnected && socket != null && !socket.isClosed()) {
            return;
        }

        closeSocket();

        // Create an unconnected socket
        socket = new Socket();
        socket.setReuseAddress(true);

        // Connect to the remote server
        InetSocketAddress remoteAddr = new InetSocketAddress(serverAddress, port);
        socket.connect(remoteAddr);

        socket.setKeepAlive(true);
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
        isConnected = true;
        System.out.println("Socket initialized and streams created");
    }

    public static synchronized void sendToServer(Message message) {
        if (!isConnected || socket == null || socket.isClosed() || outStream == null || inStream == null) {
            try {
                initializeSocket();
            } catch (IOException e) {
                System.err.println("Failed to initialize socket: " + e.getMessage());
                e.printStackTrace();
                isConnected = false;
                return;
            }
        }

        try {
            String json = gson.toJson(message);
            System.out.println("Sending to server: " + json);
            outStream.writeObject(json);
            outStream.flush();
        } catch (IOException e) {
            System.err.println("IO Error while sending: " + e.getMessage());
            e.printStackTrace();
            isConnected = false;
        }
    }

    public static synchronized Object sendAndGetResponse(Message message) {
        sendToServer(message);

        try {
            Object response = inStream.readObject();
            System.out.println("Received from server: " + response);
            return response;
        } catch (IOException e) {
            System.err.println("IO Error while sending/receiving: " + e.getMessage());
            e.printStackTrace();
            isConnected = false;
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized void closeSocket() {
        try {
            if (outStream != null) {
                outStream.close();
                outStream = null;
            }
            if (inStream != null) {
                inStream.close();
                inStream = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            isConnected = false;

            System.out.println("Socket and streams closed");
        } catch (IOException e) {
            System.err.println("Error closing socket/streams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void setListener (ServerMessageListener l) {
        listener = l;
        startListener();
    }

    private static synchronized void startListener() {
        if (listenerThread != null && listenerThread.isAlive()) {
            return;
        }

        listenerThread = new Thread(() -> {
            try {
                while (true) {

                    String json = inStream.readObject().toString();
                    Message msg = gson.fromJson(json, Message.class);
                    if (msg == null) {
                        System.out.println("Problem connecting to server");
                        notifyError(new Exception("Received null message"));
                        //serverSocket.close();
                        continue;
                    }

                    notifyMessage(json);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Listener error: " + e.getMessage());
                e.printStackTrace();
                notifyError(e);
            }
        });
        listenerThread.start();
    }

    private static synchronized void stopListener() {
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    private static void notifyMessage(Object obj) {
        mainHandler.post(() -> listener.onMessageReceived(obj));
    }

    private static void notifyError(Exception e) {
        mainHandler.post(() ->  listener.onError(e));
    }

    public interface ServerMessageListener {
        void onMessageReceived(Object obj);
        void onError(Exception e);
    }
}