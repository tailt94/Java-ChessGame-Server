package com.tuantai.chessgameserver;


import javafx.application.Platform;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Lionheart on 18-Jun-17.
 */
public class Server {
    private ArrayList<Socket> clientSockets = new ArrayList<>();
    private ArrayList<PrintWriter> clientWriters = new ArrayList<>();
    private ServerSocket serverSocket;
    private ArrayList<String> playerNames = new ArrayList<>();

    private OnDataReceiveListener dataListener = null;
    private OnConnectionEstablishListener connectionListener = null;

    public void start() {
        try {
            serverSocket = new ServerSocket(5000);

            for (int i = 1; i <= 2; i++) {
                Socket socket = serverSocket.accept();
                clientSockets.add(socket);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                clientWriters.add(writer);

                Thread t = new Thread(new ConnectionHandler(socket));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnDataReceiveListener(OnDataReceiveListener listener) {
        this.dataListener = listener;
    }

    public void setOnConnectionEstablishListener(OnConnectionEstablishListener listener) {
        this.connectionListener = listener;
    }

    private void startDataThread() {
        for (Socket socket : clientSockets) {
            Thread t = new Thread(new DataHandler(socket));
            t.start();
        }
    }

    private void sendChatData(String message) {
        Iterator it = clientWriters.iterator();
        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPlayerInfo() {
        clientWriters.get(0).println("1" + "_" + playerNames.get(0) + "_" + playerNames.get(1));
        clientWriters.get(0).flush();
        clientWriters.get(1).println("2" + "_" + playerNames.get(1) + "_" + playerNames.get(0));
        clientWriters.get(1).flush();
    }

    private void sendMoveInfo(String message) {
        String[] s = message.split("_");
        String data = message.substring(2);
        if (s[0].equals("1")) {
            clientWriters.get(1).println(data);
            clientWriters.get(1).flush();
        } else if (s[0].equals("2")) {
            clientWriters.get(0).println(data);
            clientWriters.get(0).flush();
        }
    }

    private void updateLog(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dataListener.onDataReceive(message);
            }
        });
    }

    private class ConnectionHandler implements Runnable {
        BufferedReader reader;
        Socket socket;

        public ConnectionHandler(Socket clientSocket) {
            try {
                socket = clientSocket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String playerName;
            try {
                if ((playerName = reader.readLine()) != null) {
                    playerNames.add(playerName);
                    if (playerNames.size() == 2) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                connectionListener.onConnectionEstablish();
                            }
                        });
                        updateLog("Player " + playerNames.get(0) + " connected!");
                        updateLog("Player " + playerNames.get(1) + " connected!");
                        sendPlayerInfo();
                        startDataThread();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class DataHandler implements Runnable {
        Socket socket;
        BufferedReader reader;

        public DataHandler(Socket socket) {
            try {
                this.socket = socket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    if (message.contains("_")) {
                        String name = (message.split("_")[0].equals("1")) ? playerNames.get(0) : playerNames.get(1);
                        String data = "Player " + name + " made a move...";
                        updateLog(data);
                        sendMoveInfo(message);
                    } else {
                        String data = "Message received from a player...";
                        updateLog(data);
                        sendChatData(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    interface OnDataReceiveListener {
        void onDataReceive(String message);
    }

    interface OnConnectionEstablishListener {
        void onConnectionEstablish();
    }
}
