package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import model.Connection;
import model.MessageType;
import model.PageLoader;
import model.currentUser;

import java.io.IOException;

public class BlockedUsersListController {
    private static String user;
    @FXML
    public ListView<String> listView;

    public void initialize() {
        listView.setItems(FXCollections.observableArrayList(currentUser.user.getBlockedUsers()));
    }

    public void changePage(ActionEvent actionEvent) throws IOException {
        new PageLoader().load("/Emails.fxml");
    }


    public void unblockUser(ActionEvent actionEvent) {
        try {
            new Connection().handleBlock(MessageType.unblock, user);
            currentUser.user.removeBlockedUser(user);
            listView.setItems(FXCollections.observableArrayList(currentUser.user.getBlockedUsers()));
        }
        catch (IOException ignored) {
        }
    }

    public void select(MouseEvent mouseEvent) {
        user = listView.getSelectionModel().getSelectedItem();
    }
}
