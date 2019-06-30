package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import model.Conversation;
import model.Email;
import model.PageLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class ConversationListItemController {
    private static final String NO_PHOTO = "resources/avatar.png";
    private Conversation conversation;

    @FXML
    public AnchorPane root;
    @FXML
    public ImageView senderImage;
    @FXML
    public RadioButton imp, unread;
    @FXML
    public Label starterName, textLable;

    public ConversationListItemController(Conversation conversation) throws IOException {
        this.conversation = conversation;
        new PageLoader().load("/ConversationListItem.fxml", this);
    }

    public AnchorPane init() throws IOException {
        starterName.setText(conversation.getSender().getUsername() + "@googlemail.com");
        if (conversation.getSender().getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(conversation.getSender().getImage());
            Image im = new Image(bis);
            bis.close();
            senderImage.setImage(im);
        }
        else
            senderImage.setImage(new Image(Paths.get(NO_PHOTO).toUri().toString()));
        senderImage.setClip(new Circle(30, 30, 30));
        for (Email msg : conversation.getMessages()) {
            if (!unread.isSelected() && !msg.isRead())
                unread.setSelected(true);
            if (!imp.isSelected() && msg.isImp())
                imp.setSelected(true);
            if (unread.isSelected() && imp.isSelected())
                break;
        }
        textLable.setText(conversation.getText());
        return root;
    }

    public void delete(MouseEvent mouseEvent) {
        EmailsController.setDeletedConversation(conversation);
    }
}