package controller;

import model.Email;
import model.PageLoader;

import java.io.IOException;

public class MessageListItemController {

    private Email email;

    public MessageListItemController(Email email) throws IOException {
        this.email = email;
        new PageLoader().load("/ConversationListItem.fxml", this);
    }
}
