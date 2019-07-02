package controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MessageListItemController {
    private static final String NO_PHOTO = "src/main/resources/appImages/avatar.png";
    private static final String DOWNLOAD_PATH= "resources/downloads/";
    private StringBuilder filesPaths = new StringBuilder();
    private Email message;

    @FXML
    public AnchorPane root;
    @FXML
    public RadioButton unread, imp;
    @FXML
    public Label senderName, receiverName;
    @FXML
    public TextArea messageText, pathsTextArea;
    @FXML
    public Button downloadButton;
    @FXML
    public ImageView senderImage;

    public MessageListItemController(Email message) throws IOException {
        this.message = message;
        new PageLoader().load("/MessageListItem.fxml", this);
    }

    public AnchorPane init() throws IOException {
        senderName.setText(message.getSender().getUsername() + "@googlemail.com");
        receiverName.setText(message.getReceiver() + "@googlemail.com");
        if (message.getSender().getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getSender().getImage());
            Image im = new Image(bis);
            bis.close();
            senderImage.setImage(im);
        }
        else
            senderImage.setImage(new Image(Paths.get(NO_PHOTO).toUri().toString()));
        senderImage.setClip(new Circle(30, 30, 30));

        if (message.isImp())
            imp.setSelected(true);
        if (message.isRead())
            unread.setSelected(false);
        messageText.setText(message.getText());
        if (message.getFilesInfos() != null) {
            File dir = new File("resources/downloads");
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (FileInfo uploadedInfo : message.getFilesInfos()) {
                    for (File child : directoryListing) {
                        byte[] bytes = Files.readAllBytes(child.toPath());
                        FileInfo childInfo = new FileInfo(bytes, child.getName());
                        if (uploadedInfo.equals(childInfo)) {
                            filesPaths.append(child.getPath());
                            break;
                        }
                    }
                }
            }
            if (directoryListing == null || filesPaths.toString().length() == 0) {
                downloadButton.setVisible(true);
                pathsTextArea.setVisible(false);
            }
            else {
                downloadButton.setVisible(false);
                pathsTextArea.setVisible(true);
                pathsTextArea.setText(filesPaths.toString());
            }

        }
        else {
            downloadButton.setVisible(false);
            pathsTextArea.setVisible(false);
        }
        messageText.setEditable(false);
        return root;
    }

    public void download(ActionEvent actionEvent) throws IOException {
        for (FileInfo fileInfo : message.getFilesInfos()) {
            File newFile = new File(DOWNLOAD_PATH + fileInfo.getFileName());
            if (newFile.exists()) {
                String[] parts = fileInfo.getFileName().split(".");
                for (int i = 0; ; i++) {
                    newFile = new File(DOWNLOAD_PATH + parts[0] + i + "." + parts[1]);
                    if (!newFile.exists())
                        break;
                }
            }
            filesPaths.append(newFile.getPath()).append("\n");
            FileOutputStream os = new FileOutputStream(newFile);
            os.write(fileInfo.getFileBytes());
        }
        downloadButton.setVisible(false);
        pathsTextArea.setText(filesPaths.toString());
        pathsTextArea.setVisible(true);
    }

    public void forwardMessage() throws IOException {
        EmailsController.forwardedMessage = message;
        new PageLoader().load("/Emails.fxml");
    }

    public void deleteMessage() {
        if (EmailsController.selectedConv.getMessages().size() > 1) {
            EmailsController.selectedConv.getMessages().remove(message);
            updateConv();
        }
        else {
            EmailsController.sentList.remove(EmailsController.selectedConv);
            EmailsController.inboxList.remove(EmailsController.selectedConv);
            EmailsController.selectedConv = null;
            new MailUpdater().start();
        }
    }

    public void markImp() {
        int i = EmailsController.selectedConv.getMessages().indexOf(message);
        if (imp.isSelected())
            message.setImp(true);
        else
            message.setImp(false);
        EmailsController.selectedConv.getMessages().set(i, message);
        updateConv();
    }

    public void markUnread() {
        int i = EmailsController.selectedConv.getMessages().indexOf(message);
        if (unread.isSelected())
            message.setRead(false);
        else
            message.setRead(true);
        EmailsController.selectedConv.getMessages().set(i, message);
        updateConv();
    }

    private void updateConv() {
        int i = EmailsController.sentList.indexOf(EmailsController.selectedConv);
        EmailsController.sentList.set(i, EmailsController.selectedConv);
        i = EmailsController.inboxList.indexOf(EmailsController.selectedConv);
        EmailsController.inboxList.set(i, EmailsController.selectedConv);
        new MailUpdater().start();
    }
}
