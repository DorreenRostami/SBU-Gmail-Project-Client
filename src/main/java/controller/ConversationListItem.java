package controller;

import javafx.scene.control.ListCell;
import model.Conversation;

import java.io.IOException;

public class ConversationListItem extends ListCell<Conversation> {
    @Override
    public void updateItem(Conversation conversation, boolean empty) {
        super.updateItem(conversation, empty);
        if (conversation != null) {
            setStyle("-fx-background-color: #adb4bc");
            try {
                setGraphic
                        (new ConversationListItemController(conversation).init());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
