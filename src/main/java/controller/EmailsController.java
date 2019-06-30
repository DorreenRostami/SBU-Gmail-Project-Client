package controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailsController {
    static boolean serverError = false;
    private List<Email> outboxList = new ArrayList<>();
    private static List<Conversation> currentList = null;
    private static List<Conversation> inboxList = null;
    private static List<Conversation> sentList = null;
    private static Conversation selectedConv = null;
    private List<FileInfo> attachedFiles = new ArrayList<>();
    private long GB = 2;

    @FXML public static ListView<Conversation> convosListView;
    @FXML public static ListView<Email> messagesListView;
    @FXML public ToggleButton composeButton, inboxButton, sentButton, outboxButton;
    @FXML public TitledPane newPane;
    @FXML public static TitledPane newPane2;
    @FXML public TextField receiverTextField;
    @FXML public TextField subjectTextField;
    @FXML public TextField receiverTextField2;
    @FXML public static TextField subjectTextField2;
    @FXML public TextField searchBar;
    @FXML public TextArea textArea;
    @FXML public TextArea attachedFilesTextArea;
    @FXML public static TextArea textArea2;
    @FXML public static TextArea attachedFilesTextArea2;
    @FXML public Button sendButton;
    @FXML public Button sendButton1;
    @FXML public Button cancelButton;
    @FXML public Button attachButton;
    @FXML public Button sendButton2;
    @FXML public Button cancelButton2;
    @FXML public static Button attachButton2;
    @FXML public Button searchButton;
    @FXML public Text sizeWarning, sizeWarning2;
    @FXML public static Text serverErrorText;
    @FXML public AnchorPane conversationMessagesPane;
    @FXML public ImageView currentProfilePicture, settingsIcon, refreshIcon;
    @FXML public RadioButton searchByUser, searchBySubject;

    public void initialize() {
        calculate();
        if (currentUser.user.getImage() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(currentUser.user.getImage());
            Image im = new Image(bis);
            currentProfilePicture.setImage(im);
            try {
                bis.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
        inboxButton.setSelected(true);
        loadList(MessageType.inbox);
        loadList(MessageType.sent);
        currentList = inboxList;
        if (!serverError) {
            showConversationList(currentList);
        }
        else {
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

    private void showConversationList(List<Conversation> list) {
        convosListView.setVisible(true);
        messagesListView.setVisible(false);
        conversationMessagesPane.setVisible(false);
        if (list == null || list.size() == 0) {
            convosListView.getItems().clear();
            convosListView.setPlaceholder(new Label("No Conversation"));
        }
        else {
            currentList = list;
            List<Conversation> copy = new ArrayList<>(list);
            Collections.reverse(copy);
            convosListView.setItems(FXCollections.observableArrayList(copy));
            convosListView.setCellFactory(conversationListView -> new ConversationListItem());
        }
    }

    private void showMessageList(List<Email> list) {
        convosListView.setVisible(false);
        messagesListView.setVisible(true);
        conversationMessagesPane.setVisible(true);
        if (list == null || list.size() == 0) {
            messagesListView.getItems().clear();
            messagesListView.setPlaceholder(new Label("No Messages"));
        }
        else {
            messagesListView.setItems(FXCollections.observableArrayList(list));
            messagesListView.setCellFactory(messagesListView -> new MessageListItem());
        }
    }

    private void calculate() {
        for (int i = 0; i < 5; i++) {
            GB *= GB;
        }
        GB /= 4;
    }

    public void select() {
        selectedConv = convosListView.getSelectionModel().getSelectedItem();
        showMessageList(selectedConv.getMessages());
    }

    public void changeList(MouseEvent actionEvent) {
        //do nothing and toggle the button back on if it's the current list being viewed
        if (!inboxButton.isSelected() && !sentButton.isSelected() && !outboxButton.isSelected()) {
            ((ToggleButton) actionEvent.getSource()).setSelected(true);
            return;
        }
        //change the list to the selectedConv list
        if (actionEvent.getSource() == inboxButton) {
            sentButton.setSelected(false);
            outboxButton.setSelected(false);
            showConversationList(inboxList);
        }
        else if (actionEvent.getSource() == sentButton) {
            inboxButton.setSelected(false);
            outboxButton.setSelected(false);
            showConversationList(sentList);
        }
        else if (actionEvent.getSource() == outboxButton) {
            currentList = null;
            inboxButton.setSelected(false);
            sentButton.setSelected(false);
            showMessageList(outboxList);
        }
    }

    private void send(Conversation conversation) {
        Task<Void> sendTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    new Connection(currentUser.user.getUsername()).sendMail(conversation);
                }
                catch (IOException e) {
                    serverError = true;
                }
                return null;
            }
        };
        sendTask.setOnSucceeded(e -> {
            if (!serverError)
                outboxList.remove(conversation.getMessages().get(conversation.getMessages().size() - 1));
            else
                showServerError();
        });
        new Thread(sendTask).start();
    }

    public void compose() {
        if (newPane.isVisible()) {
            composeButton.setSelected(true);
        }
        else if (newPane2.isVisible()) {
            composeButton.setSelected(false);
        }
        else {
            newPane.setVisible(true);
            sendButton.setVisible(true);
            sendButton1.setVisible(false);
            composeButton.setSelected(true);
        }
    }

    public void sendComposed() {
        Email email;
        if (newPane.isVisible())
            email = new Email(currentUser.user, receiverTextField.getText(),
                subjectTextField.getText(), textArea.getText(), attachedFiles);
        else
            email = new Email(currentUser.user, receiverTextField2.getText(),
                    subjectTextField2.getText(), textArea2.getText(), attachedFiles);
        outboxList.add(email);
        send(new Conversation(email));
        closeComposePane();
    }

    public void reply() {
        compose();
        sendButton.setVisible(false);
        sendButton1.setVisible(true);
        String receiver = "";
        for (Email e : selectedConv.getMessages()) {
            if (!e.getSender().equals(currentUser.user))
                receiver = e.getSender().getUsername();
        }
        receiverTextField.setText(receiver);
        receiverTextField.setEditable(false);
    }

    public void sendReply() {
        Email email = new Email(currentUser.user, receiverTextField.getText(),
                subjectTextField.getText(), textArea.getText(), attachedFiles);
        outboxList.add(email);
        selectedConv.addMessage(email);
        send(selectedConv);
        closeComposePane();
    }

    public void chooseFiles() {
        attachedFiles = new ArrayList<>();
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose files to attach");
        List<File> selectedFiles = fc.showOpenMultipleDialog(null);
        long size = 0;
        for (File f : selectedFiles) {
            size += f.length();
            if (size > GB) {
                if (newPane.isVisible())
                    sizeWarning.setVisible(true);
                else
                    sizeWarning2.setVisible(true);
                return;
            }
        }
        sizeWarning.setVisible(false);
        sizeWarning2.setVisible(false);
        StringBuilder filesNames = new StringBuilder();
        for (File file : selectedFiles) {
            try {
                filesNames.append(file.getAbsolutePath()).append("\n");
                byte[] data = Files.readAllBytes(file.toPath());
                String fileName = file.getName();
                FileInfo info = new FileInfo(data, fileName);
                attachedFiles.add(info);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(newPane.isVisible())
            attachedFilesTextArea.setText(filesNames.toString());
        else
            attachedFilesTextArea2.setText(filesNames.toString());
    }

    public void closeComposePane() {
        if (newPane.isVisible()) {
            receiverTextField.setText("");
            subjectTextField.setText("");
            textArea.setText("");
            attachedFilesTextArea.setText("");
            newPane.setVisible(false);
            composeButton.setSelected(false);
            receiverTextField.setEditable(true);
        }
        else {
            receiverTextField2.setText("");
            subjectTextField2.setText("");
            textArea2.setText("");
            attachedFilesTextArea2.setText("");
            newPane2.setVisible(false);
        }
        attachedFiles = new ArrayList<>();
    }

    static void forwardMessage(Email email) {
        newPane2.setVisible(true);
        subjectTextField2.setText(email.getSubject());
        textArea2.setText(email.getText());
        StringBuilder filesNames = new StringBuilder();
        if (email.getFilesInfos() != null) {
            for (FileInfo fileInfo : email.getFilesInfos())
                filesNames.append(fileInfo.getFileName()).append("\n");
            attachButton2.setVisible(false);
        }
        attachedFilesTextArea2.setText(filesNames.toString());
    }

    static void deleteConversation(Conversation conv) {
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    new Connection(currentUser.user.getUsername()).deleteConversation(conv);
                }
                catch (IOException e) {
                    serverError = true;
                }
                return null;
            }
        };
        deleteTask.setOnSucceeded(e -> {
            if (!serverError) {
                boolean currIsInbox = currentList == inboxList;
                inboxList.remove(conv);
                sentList.remove(conv);
                currentList = currIsInbox ? inboxList : sentList;

                List<Conversation> copy = new ArrayList<>(currentList);
                Collections.reverse(copy);
                convosListView.setItems(FXCollections.observableArrayList(copy));
                convosListView.setCellFactory(conversationListView -> new ConversationListItem());
            }
            else
                showServerError();
        });
        new Thread(deleteTask).start();
    }

    public static void deleteMessage(Email email) {
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    new Connection(currentUser.user.getUsername()).deleteMessage(selectedConv, email);
                }
                catch (IOException e) {
                    serverError = true;
                }
                return null;
            }
        };
        deleteTask.setOnSucceeded(e -> {
            if (!serverError) {
                boolean currIsInbox = currentList == inboxList;
                selectedConv.getMessages().remove(email);
                for (int i = 0; i < inboxList.size(); i++) {
                    if (inboxList.get(i).equals(selectedConv)) {
                        inboxList.set(i, selectedConv);
                        break;
                    }
                }
                for (int i = 0; i < sentList.size(); i++) {
                    if (sentList.get(i).equals(selectedConv)) {
                        sentList.set(i, selectedConv);
                        break;
                    }
                }
                currentList = currIsInbox ? inboxList : sentList;

                messagesListView.setItems(FXCollections.observableArrayList(selectedConv.getMessages()));
                messagesListView.setCellFactory(messagesListView -> new MessageListItem());
            }
            else
                showServerError();
        });
        new Thread(deleteTask).start();
    }

    public void changePage() throws IOException {
        new PageLoader().load("/SignIn.fxml");
    }

    public void backToConvList() {
        selectedConv = null;
        showConversationList(currentList);
    }

    public void goToSettings() throws IOException {
        new PageLoader().load("/Settings.fxml");
    }

    public void selectSearchFilter(ActionEvent actionEvent) {
        if (actionEvent.getSource() == searchBySubject) {
            searchBySubject.setSelected(true);
            searchByUser.setSelected(false);
        }
        else {
            searchBySubject.setSelected(false);
            searchByUser.setSelected(true);
        }
    }

    public void search() {
        if (currentList == null || currentList.size() == 0)
            return;
        List<Conversation> searchResult = new ArrayList<>();
        if (searchByUser.isSelected()) {
            for (Conversation conversation : currentList) {
                for (Email email : conversation.getMessages()) {
                    if (email.getSender().getUsername().toLowerCase().contains(searchBar.getText().toLowerCase()) ||
                            email.getReceiver().toLowerCase().contains(searchBar.getText().toLowerCase())) {
                        searchResult.add(conversation);
                        break; //break the loop of searching the mails in a conversation
                    }
                }
            }//end searching conversations
        }
        else {
            for (Conversation conversation : currentList) {
                for (Email email : conversation.getMessages()) {
                    if (email.getSubject().toLowerCase().contains(searchBar.getText().toLowerCase())) {
                        searchResult.add(conversation);
                        break; //break the loop of searching the mails in a conversation
                    }
                }
            }//end searching conversations
        }
        showConversationList(searchResult);
    }

    public void refresh() {
        if (currentList == inboxList) {
            loadList(MessageType.inbox);
            showConversationList(inboxList);
            loadList(MessageType.sent);
        }
        else {
            loadList(MessageType.sent);
            showConversationList(sentList);
            loadList(MessageType.inbox);
        }
        if (outboxList != null && outboxList.size() > 0) {
            for (Email email : outboxList)
                send(new Conversation(email));
        }
    }

    private static List<Conversation> loaded;
    private void loadList(MessageType messageType) {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    loaded = new Connection(currentUser.user.getUsername()).getList(messageType);;
                }
                catch (IOException e) {
                    serverError = true;
                }
                return null;
            }
        };
        loadTask.setOnSucceeded(e -> {
            if (!serverError) {
                if (messageType == MessageType.inbox)
                    inboxList = loaded;
                else
                    sentList = loaded;
            }
            else {
                showServerError();
            }
        });
        new Thread(loadTask).start();
    }
}