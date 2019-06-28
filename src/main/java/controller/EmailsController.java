package controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;

import java.io.IOException;
import java.util.List;

public class EmailsController {

    @FXML
    public ListView<Conversation> convosListView;
    @FXML
    public ListView<Email> messagesListView;
    @FXML
    public ToggleButton composeButton, inboxButton, sentButton, outboxButton;

    public void initialize() {
        inboxButton.setSelected(true);
        List<Conversation> inbox = currentUser.user.getInbox();
        if (inbox != null) {
            convosListView.setItems(FXCollections.observableArrayList(inbox));
            convosListView.setCellFactory(item -> new ConversationListItem());
        }
        else
            convosListView.setItems(null);
    }

    public void select() {
        Conversation selected = convosListView.getSelectionModel().getSelectedItem();
        convosListView.setVisible(false);
        convosListView.setDisable(true);
        messagesListView.setVisible(true);
        messagesListView.setItems(FXCollections.observableArrayList(selected.getMessages()));
        messagesListView.setCellFactory(item -> new MessageListItem());
    }

    public void changeList(ActionEvent actionEvent) {
        MessageType messageType = null;
        if (!inboxButton.isSelected() && actionEvent.getSource() == inboxButton) {
            messageType = MessageType.inbox;
        }
        else if (!sentButton.isSelected() && actionEvent.getSource() == sentButton) {
            messageType = MessageType.sent;
        }
        else if (!outboxButton.isSelected() && actionEvent.getSource() == outboxButton) {
            // TO DO
            return;
        }

        MessageType finalMessageType = messageType;
        final List<Conversation>[] list = null;
        Task<Void> showListTask = new Task<>() {
            @Override
            protected Void call() {
                list[0] = new Connection(currentUser.user.getUsername()).getList(finalMessageType);
                return null;
            }
        };
        if (list != null) {
            showListTask.setOnSucceeded(e -> convosListView.setItems(FXCollections.observableArrayList(list[0])));
            convosListView.setCellFactory(item -> new ConversationListItem());
        }
        else
            showListTask.setOnSucceeded(e -> convosListView.setItems(null));

        new Thread(showListTask).start();
    }

    public void compose() {
        //TO DO

        Email email = null;

        Task<Void> sendMailTask = new Task<>() {
            @Override
            protected Void call() {
                new Connection(currentUser.user.getUsername()).sendMail(email);
                return null;
            }
        };

//        makeAccountTask.setOnSucceeded(e -> );

        new Thread(sendMailTask).start();
    }
}