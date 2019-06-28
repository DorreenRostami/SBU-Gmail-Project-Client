package controller;

import javafx.scene.control.ListCell;
import model.Email;

import java.io.IOException;

public class ConvoListItem extends ListCell<Email> {
    @Override
    public void updateItem(Email email, boolean empty) {
        super.updateItem(email, empty);
        if (email != null) {
            setStyle("-fx-background-color: #adb4bc");
            /*try {
                setGraphic
                        (new ConvoListItemController(email).init());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
}
