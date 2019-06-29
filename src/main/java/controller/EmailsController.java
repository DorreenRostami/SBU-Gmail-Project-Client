package controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EmailsController {
    public static boolean serverError = false;
    private static List<Conversation> inboxList = null;
    private static List<Conversation> sentList = null;
    private static List<Email> outboxList = null;
    private static List<Conversation> currentList = null;
    private List<byte[]> attachedFiles = new ArrayList<>();
    private long GB = 2;

    @FXML
    public static ListView<Conversation> convosListView;
    @FXML
    public ListView<Email> messagesListView;
    @FXML
    public ToggleButton composeButton, inboxButton, sentButton, outboxButton;
    @FXML
    public TitledPane newPane;
    @FXML
    public TextField receiverTextField, subjectTextField;
    @FXML
    public TextArea textArea, attachedFilesTextArea;
    @FXML
    public Button sendButton, attachButton, cancelButton;
    @FXML
    public Text sizeWarning;
    @FXML
    public static Text serverErrorText;

    public void initialize() {
        calculate();
        inboxButton.setSelected(true);
        try {
            inboxList = currentUser.user.getInbox();
            currentList = inboxList;
        }
        catch (IOException e) {
            serverError = true;
        }
        if (!serverError && inboxList != null) {
            showConversationList(inboxList);
        }
        else if (serverError) {
            showServerError();
        }
    }

    private static void showServerError() {
        serverErrorText.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(3000), serverErrorText);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.playFromStart();

        serverError = false;
    }

    private static void showConversationList(List<Conversation> list) {
        convosListView.setItems(FXCollections.observableArrayList(list));
        convosListView.setCellFactory(item -> new ConversationListItem());
    }

    private void showMessageList(List<Email> list) {
        convosListView.setVisible(false);
        messagesListView.setVisible(true);
        messagesListView.setItems(FXCollections.observableArrayList(list));
        messagesListView.setCellFactory(item -> new MessageListItem());
    }

    private void calculate() {
        for (int i = 0; i < 5; i++) {
            GB *= GB;
        }
        GB /= 4;
    }

    public void select() {
        Conversation selected = convosListView.getSelectionModel().getSelectedItem();
        showMessageList(selected.getMessages());
    }

    public void changeList(MouseEvent actionEvent) {
        //do nothing and toggle the button back on if it's the current list being viewed
        if (!inboxButton.isSelected() && !sentButton.isSelected() && !outboxButton.isSelected()) {
            ((ToggleButton) actionEvent.getSource()).setSelected(true);
            return;
        }

        //change the list to the selected list
        MessageType messageType = null;
        if (actionEvent.getSource() == inboxButton) {
            sentButton.setSelected(false);
            outboxButton.setSelected(false);
            if (inboxList != null) {
                currentList = inboxList;
                showConversationList(inboxList);
                return;
            }
            messageType = MessageType.inbox;
        }
        else if (actionEvent.getSource() == sentButton) {
            inboxButton.setSelected(false);
            outboxButton.setSelected(false);
            if (sentList != null) {
                currentList = sentList;
                showConversationList(sentList);
                return;
            }
            messageType = MessageType.sent;
        }
        else if (actionEvent.getSource() == outboxButton) {
            currentList = null;
            inboxButton.setSelected(false);
            sentButton.setSelected(false);
            showMessageList(outboxList);
            return;
        }

        //load the list from the server if it hasn't been loaded before
        List<Conversation> list = ListLoader.load(messageType);
        if (!serverError && list != null) {
            showConversationList(list);
        }
        else if (serverError) {
            showServerError();
        }
    }

    public void compose() {
        if (newPane.isVisible()) {
            composeButton.setSelected(true);
            return;
        }
        newPane.setVisible(true);
    }

    public void sendComposed() {

        Email email = new Email(currentUser.user, receiverTextField.getText(),
               subjectTextField.getText(), textArea.getText(), attachedFiles);

        outboxList.add(email);

        final Conversation[] conv = new Conversation[1];
        Task<Void> sendMailTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    conv[0] = new Connection(currentUser.user.getUsername()).sendMail(new Conversation(email));
                }
                catch (IOException e) {
                    serverError = true;
                }
                return null;
            }
        };

        sendMailTask.setOnSucceeded(e -> updateOutbox());

        new Thread(sendMailTask).start();
        closeComposePane();
    }

    private void updateOutbox() {
        if (!serverError)
            outboxList.clear();
        else
            showServerError();
    }

    public void chooseFiles() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose files to attach");
        List<File> selectedFiles = fc.showOpenMultipleDialog(null);
        long size = 0;
        for (File f : selectedFiles) {
            size += f.length();
            if (size > GB) {
                sizeWarning.setVisible(true);
                return;
            }
        }

        sizeWarning.setVisible(false);
        StringBuilder filesNames = new StringBuilder();
        for (File file : selectedFiles) {
            try {
                filesNames.append(file.getAbsolutePath()).append("\n");
                byte[] data = Files.readAllBytes(file.toPath());
                attachedFiles.add(data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        attachedFilesTextArea.setText(filesNames.toString());
    }

    public void closeComposePane() {
        receiverTextField.setText("");
        subjectTextField.setText("");
        textArea.setText("");
        attachedFilesTextArea.setText("");
        newPane.setVisible(false);
        composeButton.setSelected(false);
    }

    static void deleteConversation(Conversation conversation) {
        try {
            new Connection(currentUser.user.getUsername());
        }
        catch (IOException e) {
            serverError = true;
            showServerError();
            return;
        }
        boolean currIsInbox = currentList == inboxList;
        inboxList.remove(conversation);
        sentList.remove(conversation);
        currentList = currIsInbox ? inboxList : sentList;
        showConversationList(currentList);
    }
}

class ListLoader {
    static List<Conversation> load(MessageType listType) {
        final List<Conversation>[] list = new List[]{null};

        Task<Void> showListTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    list[0] = new Connection(currentUser.user.getUsername()).getList(listType);
                }
                catch (IOException e) {
                    EmailsController.serverError = true;
                }
                return null;
            }
        };
//      showListTask.setOnSucceeded(e ->); progress pane

        new Thread(showListTask).start();

        return list[0];
    }
}