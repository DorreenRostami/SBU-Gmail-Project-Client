package controller;

import model.Email;
import model.PageLoader;

import java.io.IOException;

public class ConvoListItemController {

    private Email email;

    public ConvoListItemController(Email email) throws IOException {
        this.email = email;
        new PageLoader().load("/view/ConvoListItem.fxml", this);
    }
}
