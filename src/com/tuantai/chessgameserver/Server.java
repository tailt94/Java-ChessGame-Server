package com.tuantai.chessgameserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Lionheart on 18-Jun-17.
 */
public class Server {
    private ArrayList<Socket> clientSockets = new ArrayList<>();
    private ArrayList<PrintWriter> clientOutputStream = new ArrayList<>();
    private ServerSocket serverSocket;
    private ArrayList<String> playerNames = new ArrayList<>();

    public void start() {
        try {
            serverSocket = new ServerSocket(5000);

            for (int i = 1; i <= 2; i++) {
                Socket socket = serverSocket.accept();
                clientSockets.add(socket);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                clientOutputStream.add(writer);

                Thread t = new Thread(new ClientHandler(socket));
                t.start();
                System.out.println("Player " + i + " connected!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tellEveryOne(String message) {
        Iterator it = clientOutputStream.iterator();
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

    public void sendPlayerInfo() {
        clientOutputStream.get(0).println("1" + "_" + playerNames.get(0) + "_" + playerNames.get(1));
        clientOutputStream.get(0).flush();
        clientOutputStream.get(1).println("2" + "_" + playerNames.get(1) + "_" + playerNames.get(0));
        clientOutputStream.get(1).flush();
    }

    public void startChatThread() {
        for (int i = 0; i <= clientSockets.size(); i++) {
            Thread t = new Thread(new ChatHandler(clientSockets.get(i)));
            t.start();
        }
    }

    class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket socket;

        public ClientHandler(Socket clientSocket) {
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
                    System.out.println(playerName);
                    if (playerNames.size() == 2) {
                        sendPlayerInfo();
                        startChatThread();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class ChatHandler implements Runnable {
        Socket socket;
        BufferedReader reader;

        public ChatHandler(Socket socket) {
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
                    tellEveryOne(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
