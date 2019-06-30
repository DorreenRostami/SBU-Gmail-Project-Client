package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import model.Email;
import model.FileInfo;
import model.PageLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MessageListItemController {
    private static final String NO_PHOTO = "resources/avatar.png";
    private static final String DOWNLOAD_PATH= "resources/downloads/";
    private StringBuilder filesPaths = new StringBuilder();
    private Email email;

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

    public MessageListItemController(Email email) throws IOException {
        this.email = email;
        new PageLoader().load("/MessageListItem.fxml", this);
    }

    public AnchorPane init() throws IOException {
        senderName.setText(email.getSender().getUsername() + "@googlemail.com");
        if (email.getSender().getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(email.getSender().getImage());
            Image im = new Image(bis);
            bis.close();
            senderImage.setImage(im);
        }
        else
            senderImage.setImage(new Image(Paths.get(NO_PHOTO).toUri().toString()));
        senderImage.setClip(new Circle(30, 30, 30));
        if (email.isImp())
            imp.setSelected(true);
        if (email.isRead())
            unread.setSelected(false);
        messageText.setText(email.getText());
        if (email.getFilesInfos() != null) {
            File dir = new File("resources/downloads");
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (FileInfo uploadedInfo : email.getFilesInfos()) {
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
        return root;
    }

    public void download(ActionEvent actionEvent) throws IOException {
        for (FileInfo fileInfo : email.getFilesInfos()) {
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
}
