package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class newAccountController {
    @FXML
    public TextField firstNameTextField, lastNameTextField, usernameTextField,
            dayTextField, monthTextField, yearTextField;
    @FXML
    public PasswordField passwordField, passwordField2;
    @FXML
    public Text nameWarning, enterUsernameWarning, usernameCharText, usernameCharWarning, usernameTakenWarning, passwordLengthText, passwordLengthWarning,
            passwordCharText, passwordCharWarning, birthdayWarning, passwordMatchWarning, youngWarning;
    @FXML
    public Button nextButton, backButton;

    public void changePage(ActionEvent actionEvent) throws IOException, ParseException {
        if (actionEvent.getSource() == backButton)
            new PageLoader().load("/view/SignIn.fxml");
        else if (actionEvent.getSource() == nextButton) {
            resetWarnings();

            //establish a connection with the server to check if given information is valid
            Connection c = new Connection(usernameTextField.getText());
            String birthday = dayTextField.getText() + "/" + monthTextField.getText() + "/" + yearTextField.getText();
            User user = new User(firstNameTextField.getText(), lastNameTextField.getText(), birthday,
                    usernameTextField.getText(), passwordField.getText());
            List<SignUpFeedback> feedback = c.signUpConnection(user, passwordField2.getText());

            //check if user is signed up or not (and why they aren't)
            if (feedback.contains(SignUpFeedback.signedUp)) {
                currentUser.user = user;
                new PageLoader().load("/view/SignUpImageChooser.fxml");
            }
            else {
                if (feedback.contains(SignUpFeedback.fullName))
                    nameWarning.setVisible(true);
                if (feedback.contains(SignUpFeedback.birthday))
                    birthdayWarning.setVisible(true);
                else if (feedback.contains(SignUpFeedback.young))
                    youngWarning.setVisible(true);
                if (feedback.contains(SignUpFeedback.enterUsername))
                    enterUsernameWarning.setVisible(true);
                else {
                    if (feedback.contains(SignUpFeedback.badUsername)) {
                        usernameCharText.setVisible(false);
                        usernameCharWarning.setVisible(true);
                    }
                     else if (feedback.contains(SignUpFeedback.takenUsername)) {
                         usernameTakenWarning.setVisible(true);
                     }
                }
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

    private void resetWarnings(){
        nameWarning.setVisible(false);
        birthdayWarning.setVisible(false);
        enterUsernameWarning.setVisible(false);
        usernameCharText.setVisible(true);
        usernameCharWarning.setVisible(false);
        passwordLengthText.setVisible(true);
        passwordLengthWarning.setVisible(false);
        passwordCharText.setVisible(true);
        passwordCharWarning.setVisible(false);
        usernameTakenWarning.setVisible(false);
        passwordMatchWarning.setVisible(false);
        youngWarning.setVisible(false);
    }
}
