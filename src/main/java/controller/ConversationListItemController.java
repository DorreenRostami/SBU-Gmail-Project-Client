package controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
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
    public Text starterName, convoText;

    public ConversationListItemController(Conversation conversation) throws IOException {
        this.conversation = conversation;
        new PageLoader().load("/ConversationListItem.fxml", this);
    }

    public AnchorPane init() throws IOException {
        starterName.setText(conversation.getStarter().getUsername() + "@gmail.com");
        if (conversation.getStarter().getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(conversation.getStarter().getImage());
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
        Email last = conversation.getLast();
        convoText.setText(last.getSubject() + " - " +
                last.getText().substring(0, 65 - last.getSubject().length()) + "...");
        return root;
    }
}