package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Connection;
import model.PageLoader;

import java.io.IOException;

public class SignInController {
    @FXML
    public TextField emailTextField, passwordTextField;
    @FXML
    public Hyperlink newAccountHyperlink;
    @FXML
    public Button signInButton;
    @FXML
    public Label wrongLabel;

    public void changePage(ActionEvent actionEvent) throws IOException {
        if (actionEvent.getSource() == signInButton) {
            Connection c = new Connection(emailTextField.getText());
            boolean signedIn = c.signInConnection(passwordTextField.getText());
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
}
