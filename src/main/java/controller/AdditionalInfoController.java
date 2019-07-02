package controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class AdditionalInfoController {
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
        image.setClip(new Circle(70, 70, 70));
    }

    public void openFileChooser() throws IOException {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a profile picture");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            //turn chosen image into a byte array
            BufferedImage bImage = ImageIO.read(selectedFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", bos);
            byte [] data = bos.toByteArray();
            bos.close();

            //turn byte array into an image
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            Image im = new Image(bis);
            bis.close();
            image.setImage(im);

            //set image as profile picture
            currentUser.user.setImage(data);
        }
    }

    public void changePage() {
        if (mobileTextField.getText().length() != 0) {
            currentUser.user.setMobile(mobileTextField.getText());
        }
        if (choiceBox.getValue() != null) {
            currentUser.user.setGender(choiceBox.getValue());
        }

        Task<Void> makeAccountTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    new Connection(currentUser.user.getUsername()).signUpConnection(currentUser.user);
                }
                catch (IOException e) {
                    System.out.println("Server Error");
                }
                return null;
            }
        };

        makeAccountTask.setOnSucceeded(e -> {
            try {
                new PageLoader().load("/Emails.fxml");
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        new Thread(makeAccountTask).start();
    }
}