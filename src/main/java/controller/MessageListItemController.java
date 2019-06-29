package controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import model.Email;
import model.PageLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class MessageListItemController {
    private static final String NO_PHOTO = "resources/avatar.png";
    private Email email;

    @FXML
    public AnchorPane root;

    public MessageListItemController(Email email) throws IOException {
        this.email = email;
        new PageLoader().load("/MessageListItem.fxml", this);
    }

    public AnchorPane init() throws IOException {
        /*starterName.setText(conversation.getSender().getUsername() + "@googlemail.com");
        if (conversation.getSender().getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(conversation.getSender().getImage());
            Image im = new Image(bis);
            bis.closeComposePane();
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
        textLable.setText(conversation.getText());*/
        return root;
    }
}
