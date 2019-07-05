package controller;

import javafx.concurrent.Task;
import model.Connection;
import model.MessageType;

import java.io.IOException;

public class MailUpdater extends Thread {
    @Override
    public void run() {
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Connection c = new Connection();
                    if (EmailsController.inboxList != null && EmailsController.inboxList.size() > 0)
                        c.saveListChanges(MessageType.updateInbox, EmailsController.inboxList);
                    if (EmailsController.sentList != null && EmailsController.sentList.size() > 0)
                        c.saveListChanges(MessageType.updateSent, EmailsController.sentList);
                }
                catch (IOException e) {
                    EmailsController.serverError = true;
                }
                return null;
            }
        };

        new Thread(updateTask).start();
    }
}
