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
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmailsController {
    static boolean serverError = false;
    static Email forwardedMessage = null;
    private static boolean refreshing = false;
    private static List<Email> outboxList = new ArrayList<>();
    private static ListType currentListType = null;
    static List<Conversation> inboxList = null;
    static List<Conversation> sentList = null;
    static Conversation selectedConv = null;

    private List<FileInfo> attachedFiles = new ArrayList<>();
    private long GB = 2;

    @FXML public ListView<Conversation> convosListView;
    @FXML public ListView<Email> messagesListView;
    @FXML public ToggleButton composeButton, inboxButton, sentButton, outboxButton;
    @FXML public TitledPane newPane;
    @FXML public TextField receiverTextField, subjectTextField, searchBar;
    @FXML public TextArea textArea, attachedFilesTextArea;
    @FXML public Text sizeWarning, serverErrorText;
    @FXML public Button sendButton, sendButton1, cancelButton, attachButton, searchButton;
    @FXML public AnchorPane conversationMessagesPane;
    @FXML public ImageView currentProfilePicture, settingsIcon, refreshIcon;
    @FXML public RadioButton searchByUser, searchBySubject;

    /**
     * every time this page loads the imageView and listView need to be set.
     *
     * The if statement explanation:
     * The first time that this page loads, it should load everything.
     * But when it loads again (by being called from other controllers) there might have
     * been an error connecting to server (serverError is true) so the error will be shown
     * and the changes to the list being viewed will be made but will not be saved in the
     * server until another change is made or the user signs out when connected to the server.
     *
     * If the user is forwarding a message, no changes were made since the prior viewing so
     * all lists will be loaded as they were before.
     *
     * If the user refreshed the page and there was no error connecting to the server, the messages
     *(if any) in the outbox will be sent as they should have been when connected and the lists
     * will be downloaded from the server and shown but if there was a server error the lists will
     * be viewed just as they were before (therefore if another user has sent them a message or if
     * the user has sent a message in the passed time from their last refresh, they wont view
     * those messages unless they refresh again when they have connected to te server).
     */
    public void initialize() {
        Thread t1 = new LoadInbox();
        Thread t2 = new LoadSent();
        if (!serverError && refreshing) {
            t1.start();
            t2.start();
        }

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
        currentProfilePicture.setClip(new Circle(40, 40, 40));

        if (currentListType == null || currentListType == ListType.inbox) {
            currentListType = ListType.inbox;
            inboxButton.setSelected(true);
            if (!serverError && refreshing) {
                try {
                    t1.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showConversationList(inboxList);
        }
        else if (currentListType == ListType.sent) {
            sentButton.setSelected(true);
            if (!serverError && refreshing) {
                try {
                    t2.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showConversationList(sentList);
        }
        else if (currentListType == ListType.inboxConv) {
            inboxButton.setSelected(true);
            if (!serverError && refreshing) {
                try {
                    t1.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showMessageList(selectedConv.getMessages());
        }
        else if (currentListType == ListType.sentConv) {
            sentButton.setSelected(true);
            if (!serverError && refreshing) {
                try {
                    t2.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showMessageList(selectedConv.getMessages());
        }
        else if (currentListType == ListType.outbox) {
            if (!serverError && refreshing) {
                if (outboxList.size() > 0) {
                    for (Email email : outboxList)
                        send(new Conversation(email));
                }
                showMessageList(null);
            }
            else
                showMessageList(outboxList);
            outboxButton.setSelected(true);
        }
        refreshing = false;
        if (serverError) {
            showServerError();
        }
        if (forwardedMessage != null)
            forwardMessage();
    }

    private void showServerError() {
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
        if (currentListType == ListType.inbox)
            currentListType = ListType.inboxConv;
        else
            currentListType = ListType.sentConv;
        for (Email e : selectedConv.getMessages()) {
            if (!e.isRead())
                e.setRead(true);
        }
        int i = EmailsController.sentList.indexOf(selectedConv);
        EmailsController.sentList.set(i, selectedConv);
        i = EmailsController.inboxList.indexOf(selectedConv);
        EmailsController.inboxList.set(i, selectedConv);
        new MailUpdater().start();
        showMessageList(selectedConv.getMessages());
    }

    public void changeList(MouseEvent actionEvent) {
        //do nothing and toggle the button back on if it's the current list being viewed
        if (!inboxButton.isSelected() && !sentButton.isSelected() && !outboxButton.isSelected()) {
            ((ToggleButton) actionEvent.getSource()).setSelected(true);
            return;
        }
        //changeInfo the list
        if (actionEvent.getSource() == inboxButton) {
            sentButton.setSelected(false);
            outboxButton.setSelected(false);
            currentListType = ListType.inbox;
            showConversationList(inboxList);
        }
        else if (actionEvent.getSource() == sentButton) {
            inboxButton.setSelected(false);
            outboxButton.setSelected(false);
            currentListType = ListType.sent;
            showConversationList(sentList);
        }
        else if (actionEvent.getSource() == outboxButton) {
            currentListType = ListType.outbox;
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
        composeButton.setSelected(true);
        if (!newPane.isVisible()) {
            newPane.setVisible(true);
            sendButton.setVisible(true);
            sendButton1.setVisible(false);
        }
    }

    public void sendComposed() {
        Email email = new Email(currentUser.user, receiverTextField.getText(),
                subjectTextField.getText(), textArea.getText(), attachedFiles);
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
                String fileName = file.getName();
                FileInfo info = new FileInfo(data, fileName);
                attachedFiles.add(info);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        attachedFilesTextArea.setText(filesNames.toString());
    }

    public void closeComposePane() {
        receiverTextField.setText("yo");
        subjectTextField.setText("");
        textArea.setText("");
        attachedFilesTextArea.setText("");
        newPane.setVisible(false);
        composeButton.setSelected(false);
        receiverTextField.setEditable(true);
        attachedFiles = new ArrayList<>();
    }

    private void forwardMessage() {
        compose();
        subjectTextField.setText(forwardedMessage.getSubject());
        textArea.setText(forwardedMessage.getText());
        StringBuilder filesNames = new StringBuilder();
        if (forwardedMessage.getFilesInfos() != null) {
            for (FileInfo fileInfo : forwardedMessage.getFilesInfos())
                filesNames.append(fileInfo.getFileName()).append("\n");
            attachButton.setVisible(false);
        }
        attachedFilesTextArea.setText(filesNames.toString());
        forwardedMessage = null;
    }

    public void signOut() throws IOException, InterruptedException {
        Thread updaterThread = new MailUpdater();
        updaterThread.start();
        outboxList = new ArrayList<>();
        serverError = false;
        currentListType = null;
        currentUser.user = null;
        updaterThread.join();
        inboxList = null;
        sentList = null;
        new PageLoader().load("/SignIn.fxml");
    }

    public void backToConvList() {
        selectedConv = null;
        if (inboxButton.isSelected())
            showConversationList(inboxList);
        else
            showConversationList(sentList);
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
        List<Conversation> searchResult = new ArrayList<>();
        Set<Conversation> setOfAllConversations = Stream.concat(inboxList.stream(), sentList.stream()).
                collect(Collectors.toSet());
        if (searchByUser.isSelected()) {
            for (Conversation conversation : setOfAllConversations) {
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
            for (Conversation conversation : setOfAllConversations) {
                for (Email email : conversation.getMessages()) {
                    if (email.getSubject().toLowerCase().contains(searchBar.getText().toLowerCase())) {
                        searchResult.add(conversation);
                        break; //break the loop of searching the mails in a conversation
                    }
                }
            }//end searching conversations
        }
        showConversationList(searchResult);
        inboxButton.setSelected(false);
        outboxButton.setSelected(false);
        sentButton.setSelected(false);
    }

    public void refresh() throws IOException {
        refreshing = true;
        new PageLoader().load("/Emails.fxml");
    }

    public void block(ActionEvent actionEvent) {
    }
}

class LoadInbox extends Thread {
    @Override
    public void run() {
        try {
            EmailsController.inboxList = new Connection(currentUser.user.getUsername()).getList(MessageType.getInbox);
        }
        catch (IOException e) {
            EmailsController.serverError = true;
        }
    }
}

class LoadSent extends Thread {
    @Override
    public void run() {
        try {
            EmailsController.sentList = new Connection(currentUser.user.getUsername()).getList(MessageType.getSent);
        }
        catch (IOException e) {
            EmailsController.serverError = true;
        }
    }
}