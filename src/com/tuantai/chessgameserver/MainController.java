package com.tuantai.chessgameserver;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Lionheart on 23-Jun-17.
 */
public class MainController implements Initializable, Server.OnDataReceiveListener{
    @FXML
    private TextArea log;
    private Server server = null;

    public MainController(Server server) {
        this.server = server;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        server.setOnDataReceiveListener(this);
    }

    @Override
    public void onDataReceive(String message) {
        log.appendText(message + "\n");
    }
}
