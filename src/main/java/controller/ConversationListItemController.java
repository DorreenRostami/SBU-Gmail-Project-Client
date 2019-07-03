package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ConversationListItemController {
    private Conversation conversation;

    @FXML
    public AnchorPane root;
    @FXML
    public ImageView senderImage;
    @FXML
    public RadioButton imp, unread;
    @FXML
    public Label starterName, textLabel;
    @FXML
    public Text timeText;

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
        senderImage.setClip(new Circle(30, 30, 30));

        for (Email msg : conversation.getMessages()) {
            if (!unread.isSelected() && !msg.isRead())
                unread.setSelected(true);
            if (!imp.isSelected() && msg.isImp())
                imp.setSelected(true);
            if (unread.isSelected() && imp.isSelected())
                break;
        }

        textLabel.setText(conversation.getText());
//        timeText.setText(conversation.getTime());
        return root;
    }

    public void delete(MouseEvent mouseEvent) throws IOException {
        EmailsController.sentList.remove(conversation);
        EmailsController.inboxList.remove(conversation);
        new MailUpdater().start();
        new PageLoader().load("/Emails.fxml");
    }

    public void markImp() throws IOException {
        if (imp.isSelected())
            conversation.getMessages().get(conversation.getMessages().size() - 1).setImp(true);
        else
            conversation.getMessages().get(conversation.getMessages().size() - 1).setImp(false);
        updateConv();
    }

    public void markUnread() throws IOException {
        if (unread.isSelected())
            conversation.getMessages().get(conversation.getMessages().size() - 1).setRead(false);
        else
            conversation.getMessages().get(conversation.getMessages().size() - 1).setRead(true);
        updateConv();
    }

    private void updateConv() throws IOException {
        int i = EmailsController.sentList.indexOf(conversation);
        EmailsController.sentList.set(i, conversation);
        i = EmailsController.inboxList.indexOf(conversation);
        EmailsController.inboxList.set(i, conversation);
        new MailUpdater().start();
        new PageLoader().load("/Emails.fxml");
    }
}