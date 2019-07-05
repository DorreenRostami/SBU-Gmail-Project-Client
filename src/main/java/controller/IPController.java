package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.Connection;
import model.PageLoader;

import java.io.IOException;

public class IPController {

    @FXML
    public TextField IPText;

    public void done() throws IOException {
        Connection.serverIP = IPText.getText();

        new PageLoader().load("/SignIn.fxml");
    }
}
