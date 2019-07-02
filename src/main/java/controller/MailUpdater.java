package controller;

import javafx.concurrent.Task;
import model.Connection;
import model.MessageType;
import model.PageLoader;
import model.currentUser;

import java.io.IOException;

public class MailUpdater extends Thread {
    @Override
    public void run() {
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connection c = new Connection(currentUser.user.getUsername());
                    c.saveListChanges(MessageType.updateInbox, EmailsController.inboxList);
                    c.saveListChanges(MessageType.updateSent, EmailsController.sentList);
                }
                catch (IOException e) {
                    EmailsController.serverError = true;
                }
                return null;
            }
        };
        updateTask.setOnSucceeded(e -> {
            try {
                new PageLoader().load("/Emails.fxml");
            }
            catch (IOException e1) {
                e1.getMessage();
            }
        });
        new Thread(updateTask).start();
    }
}
