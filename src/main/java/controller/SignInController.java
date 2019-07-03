package controller;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import model.Connection;
import model.PageLoader;

import java.io.IOException;

public class SignInController {
    @FXML
    public TextField emailTextField, passwordTextField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public CheckBox showPassCheckBox;
    @FXML
    public Hyperlink newAccountHyperlink;
    @FXML
    public Button signInButton;
    @FXML
    public Label wrongLabel;

    public void initialize() {
        playTransitions();
    }

    public void changePage(ActionEvent actionEvent) throws IOException {
        if (actionEvent.getSource() == signInButton) {
            String pass = passwordTextField.getText().length() == 0 ?
                    passwordField.getText() : passwordTextField.getText();
            boolean signedIn = new Connection().signInConnection(emailTextField.getText(), pass);
            if (signedIn) {
                wrongLabel.setVisible(false);
                new PageLoader().load("/Emails.fxml");
            }
            else
                wrongLabel.setVisible(true);
        }
        else if (actionEvent.getSource() == newAccountHyperlink)
            new PageLoader().load("/newAccount.fxml");
    }

    private void playTransitions(){
        TranslateTransition trans = new TranslateTransition(Duration.millis(2000), signInButton);
        trans.setToY(15);
        trans.playFromStart();
    }

    public void showPassword() {
        if(showPassCheckBox.isSelected()){
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
        }
        else{
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
        }
    }
}
