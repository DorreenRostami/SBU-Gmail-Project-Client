package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SettingsController {
    @FXML public PasswordField oldPasswordField, newPasswordField, newPasswordField2;
    @FXML public Text passwordLengthText, wrongPassWarning, passwordCharText, passwordLengthWarning,
            passwordCharWarning, passwordMatchWarning, nameWarning, youngWarning, birthdayWarning;
    @FXML public TextField firstNameTextField, lastNameTextField, mobileTextField;
    @FXML public Button backButton, saveButton, chooseImageButton;
    @FXML public ImageView profilePicture;
    @FXML public TextField dayTextField, monthTextField, yearTextField;
    @FXML public ChoiceBox<Gender> choiceBox;

    private byte[] chosenImage = currentUser.user.getImage();

    public void initialize() {
        choiceBox.getItems().addAll(Gender.Male, Gender.Female);
        if (currentUser.user.getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(chosenImage);
            Image im = new Image(bis);
            profilePicture.setImage(im);
            try {
                bis.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
        String[] bday = currentUser.user.getBirthday().split("/");
        firstNameTextField.setText(currentUser.user.getName());
        lastNameTextField.setText(currentUser.user.getSurname());
        dayTextField.setText(bday[0]);
        monthTextField.setText(bday[1]);
        yearTextField.setText(bday[2]);
        mobileTextField.setText(currentUser.user.getMobile());
        choiceBox.setValue(currentUser.user.getGender());
    }

    public void changePage(ActionEvent actionEvent) throws IOException {
        if (actionEvent.getSource() == backButton)
            new PageLoader().load("/Emails.fxml");
        else if (actionEvent.getSource() == saveButton) {
            resetWarnings();
            if (oldPasswordField.getText().length() > 0 && !currentUser.user.getPassword().equals(oldPasswordField.getText())){
                wrongPassWarning.setVisible(true);
                return;
            }

            String newPass = oldPasswordField.getText().length() > 0 ? newPasswordField.getText() : currentUser.user.getPassword();

            //establish a connection with the server to check if given information is valid
            Connection c = new Connection(currentUser.user.getUsername());
            String birthday = dayTextField.getText() + "/" + monthTextField.getText() + "/" + yearTextField.getText();
            User user = new User(firstNameTextField.getText(), lastNameTextField.getText(), birthday,
                    currentUser.user.getUsername(), newPass);
            user.setGender(choiceBox.getValue());
            user.setMobile(mobileTextField.getText());
            user.setImage(chosenImage);
            List<SignUpFeedback> feedback = c.settingsConnection(user, newPasswordField2.getText());
            if (feedback.contains(SignUpFeedback.changed)) {
                currentUser.user = user;
                new PageLoader().load("/Emails.fxml");
            }
            else {
                if (feedback.contains(SignUpFeedback.fullName))
                    nameWarning.setVisible(true);
                if (feedback.contains(SignUpFeedback.birthday))
                    birthdayWarning.setVisible(true);
                else if (feedback.contains(SignUpFeedback.young))
                    youngWarning.setVisible(true);
                if (feedback.contains(SignUpFeedback.shortPass)) {
                    passwordLengthText.setVisible(false);
                    passwordLengthWarning.setVisible(true);
                }
                else {
                    if (feedback.contains(SignUpFeedback.badPass)) {
                        passwordCharText.setVisible(false);
                        passwordCharWarning.setVisible(true);
                    }
                    else if (feedback.contains(SignUpFeedback.mismatchedPass))
                        passwordMatchWarning.setVisible(true);
                }
            }
        }
    }

    public void chooseFile() throws IOException {
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
            profilePicture.setImage(im);

            chosenImage = data;
        }
    }

    private void resetWarnings(){
        nameWarning.setVisible(false);
        birthdayWarning.setVisible(false);
        passwordLengthText.setVisible(true);
        passwordLengthWarning.setVisible(false);
        passwordCharText.setVisible(true);
        passwordCharWarning.setVisible(false);
        passwordMatchWarning.setVisible(false);
        youngWarning.setVisible(false);
        wrongPassWarning.setVisible(false);
    }
}