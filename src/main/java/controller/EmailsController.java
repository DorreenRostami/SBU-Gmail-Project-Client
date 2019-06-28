package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;

public class EmailsController {

    @FXML
    public ListView<String> optionsListView;

    public void initialize() {
        List<String> options = Arrays.asList("Compose", "Inbox", "Sent", "Outbox");
    }
}