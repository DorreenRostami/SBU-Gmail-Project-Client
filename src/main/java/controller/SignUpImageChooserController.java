package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

public class SignUpImageChooserController {
    @FXML
    public Button chooseButton, doneButton;
    @FXML
    public ImageView image;
    @FXML
    public ChoiceBox<Gender> choiceBox;
    @FXML
    public TextField mobileTextField;

    public void initialize() {
        choiceBox.getItems().addAll(Gender.Male, Gender.Female);
        mobileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                mobileTextField.setText(oldValue);
            }
        });
    }

    public void openFileChooser() throws IOException {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("png", "*.png"),
                new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            String newImagePath = "/currentUser/" + selectedFile.getName();
            FileOutputStream fos = new FileOutputStream(newImagePath);
            Files.copy(selectedFile.toPath(), fos);
            File newFile = new File(newImagePath);
            Image newImage = new Image(newFile.toURI().toString());
            image.setImage(newImage);

            //turn chosen image into a byte array
            BufferedImage bImage = ImageIO.read(selectedFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", bos);
            byte [] data = bos.toByteArray();
            currentUser.user.setImage(data);
        }
    }

    public void changePage() throws IOException {
        if (mobileTextField.getText().length() != 0) {
            currentUser.user.setMobile(mobileTextField.getText());
        }
        if (choiceBox.getValue() != null) {
            currentUser.user.setGender(choiceBox.getValue());
        }
        new Connection(currentUser.user.getUsername()).signUpConnection(currentUser.user);
        new PageLoader().load("/view/Emails.fxml");
    }
}
