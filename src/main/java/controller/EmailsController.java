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
    static Email forwardedMessage = null;
    static ListType currentListType = null;
    static List<Conversation> inboxList = null;
    static List<Conversation> sentList = null;
    static Conversation selectedConv = null;
    static boolean serverError = false;

    private static boolean refreshing = false;
    private static List<Email> outboxList = new ArrayList<>();
    private static Image profilePicture = null;

    private List<FileInfo> attachedFiles = new ArrayList<>();
    private long GB = 2;

    @FXML public ListView<Conversation> convosListView;
    @FXML public ListView<Email> messagesListView;
    @FXML public ToggleButton composeButton, inboxButton, sentButton, outboxButton;
    @FXML public TitledPane newPane;
    @FXML public TextField receiverTextField, subjectTextField, searchBar;
    @FXML public TextArea textArea, attachedFilesTextArea;
    @FXML public Text sizeWarning, serverErrorText;
    @FXML public Button sendButton, sendButton1, cancelButton, attachButton,
            searchButton, replyButton, blockButton, unblockButton;
    @FXML public AnchorPane blackPane, whitePane;
    @FXML public ImageView currentProfilePicture, settingsIcon, refreshIcon;
    @FXML public RadioButton searchByUser, searchBySubject, latestFirst, unreadFirst;
    @FXML public Label subjectLabel;

    /**
     * every time this page loads the imageView and listView need to be set.
     *
     * The if statement explanation:
     * The first time that this page loads, it should load everything.
     * But when it loads again (by being called from other controllers) there might have
     * been an error connecting to the server (serverError is true) so the error will be shown
     * and the changes to the list being viewed will be made but will not be saved in the
     * server until another change is made or the user signs out when connected to the server.
     *
     * If the user is forwarding a message, no changes were made since the prior viewing so
     * all lists will be loaded as they were before.
     *
     * If the user refreshed the page and there was no error connecting to the server, the messages
     *(if any) in the outbox will be sent as they would have been when connected and the lists
     * will be downloaded from the server and shown but if there was a server error, the lists will
     * be viewed just as they were before (therefore if another user has sent them a message or if
     * the user has sent a message in the time passed since their last refresh, they wont view
     * those messages unless they refresh again when they have connected to te server).
     */
    public void initialize() throws InterruptedException {
        Thread t1 = new LoadInbox();
        Thread t2 = new LoadSent();
        if (!serverError && (currentListType == null || refreshing)) {
            t1.start();
            t2.start();
        }

        calculate();

        if (profilePicture == null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(currentUser.user.getImage());
            profilePicture = new Image(bis);
            currentProfilePicture.setImage(profilePicture);
            try {
                bis.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        } else {
            currentProfilePicture.setImage(profilePicture);
        }
        currentProfilePicture.setClip(new Circle(40, 40, 40));

        if (currentListType == null || currentListType == ListType.inbox) {
            currentListType = ListType.inbox;
            inboxButton.setSelected(true);
            if (!serverError) {
                t1.join();
            }
            showConversationList(inboxList);
        }
        else if (currentListType == ListType.sent) {
            sentButton.setSelected(true);
            if (!serverError) {
                t2.join();
            }
            showConversationList(sentList);
        }
        else if (currentListType == ListType.inboxConv) {
            inboxButton.setSelected(true);
            if (!serverError) {
                t1.join();
            }
            showMessageList(selectedConv.getMessages());
        }
        else if (currentListType == ListType.sentConv) {
            sentButton.setSelected(true);
            if (!serverError) {
                t2.join();
            }
            showMessageList(selectedConv.getMessages());
        }
        else if (currentListType == ListType.outbox) {
            if (!serverError && refreshing) {
                if (outboxList.size() > 0) {
                    for (Email email : outboxList)
                        send(new Conversation(email));
                    outboxList = new ArrayList<>();
                }
            }
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
        FadeTransition ft = new FadeTransition(Duration.millis(5000), serverErrorText);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.playFromStart();
        serverError = false;
    }

    private void showConversationList(List<Conversation> list) {
        convosListView.setVisible(true);
        convosListView.setDisable(false);
        messagesListView.setVisible(false);
        messagesListView.setDisable(true);
        blackPane.setVisible(false);
        whitePane.setVisible(true);
        latestFirst.setVisible(true);
        unreadFirst.setVisible(true);
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
        convosListView.setDisable(true);
        messagesListView.setVisible(true);
        messagesListView.setDisable(false);
        if (currentListType != ListType.outbox) {
            blackPane.setVisible(true);
            whitePane.setVisible(false);
        }
        else {
            whitePane.setVisible(true);
            blackPane.setVisible(false);
            latestFirst.setVisible(false);
            unreadFirst.setVisible(false);
        }
        if (list == null || list.size() == 0) {
            messagesListView.getItems().clear();
            messagesListView.setPlaceholder(new Label("No Messages"));
        }
        else {
            if (currentListType != ListType.outbox) {
                for (int i = 0; i < list.size(); i++) {
                    if (currentUser.user.getBlockedUsers().size() > 0 &&
                            currentUser.user.getBlockedUsers().contains(list.get(i).getSender().getUsername())) {
                        blockButton.setVisible(false);
                        unblockButton.setVisible(true);
                        break;
                    }
                    if (currentUser.user.getBlockedUsers().size() == 0 || i == list.size() - 1) {
                        blockButton.setVisible(true);
                        unblockButton.setVisible(false);
                        break;
                    }
                }
                subjectLabel.setText(selectedConv.getMessages().get(0).getSubject());
            }
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

    public void select() throws IOException {
        selectedConv = convosListView.getSelectionModel().getSelectedItem();
        if (currentListType == ListType.inbox)
            currentListType = ListType.inboxConv;
        else
            currentListType = ListType.sentConv;
        for (Email e : selectedConv.getMessages()) {
            if (!e.isRead()) {
                e.setRead(true);
                int i = EmailsController.sentList.indexOf(selectedConv);
                if (i > 0)
                    EmailsController.sentList.set(i, selectedConv);
                i = EmailsController.inboxList.indexOf(selectedConv);
                if (i > 0)
                    EmailsController.inboxList.set(i, selectedConv);
                new MailUpdater().start();
            }
        }
        new PageLoader().load("/Emails.fxml");
    }

    public void changeList(MouseEvent actionEvent) {
        searchBar.setText("");

        //do nothing and toggle the button back on if it's the current list being viewed
        if (!inboxButton.isSelected() && !sentButton.isSelected() && !outboxButton.isSelected()) {
            ((ToggleButton) actionEvent.getSource()).setSelected(true);
            return;
        }
        //change the list
        if (actionEvent.getSource() == inboxButton) {
            sentButton.setSelected(false);
            outboxButton.setSelected(false);
            currentListType = ListType.inbox;
            latestFirst.setSelected(true);
            unreadFirst.setSelected(false);
            showConversationList(inboxList);
        }
        else if (actionEvent.getSource() == sentButton) {
            inboxButton.setSelected(false);
            outboxButton.setSelected(false);
            currentListType = ListType.sent;
            latestFirst.setSelected(true);
            unreadFirst.setSelected(false);
            showConversationList(sentList);
        }
        else if (actionEvent.getSource() == outboxButton) {
            currentListType = ListType.outbox;
            inboxButton.setSelected(false);
            sentButton.setSelected(false);
            showMessageList(outboxList);
        }
    }

    public void arrange(ActionEvent actionEvent) {
        if (!unreadFirst.isSelected() && !latestFirst.isSelected()) {
            ((RadioButton) actionEvent.getSource()).setSelected(true);
            return;
        }

        if (actionEvent.getSource() == latestFirst) {
            unreadFirst.setSelected(false);
            if (currentListType == ListType.inbox)
                showConversationList(inboxList);
            else
                showConversationList(sentList);
        }
        else {
            latestFirst.setSelected(false);
            if (currentListType == ListType.inbox) {
                showConversationList(ListArranger.arrangeByUnread(inboxList));
            }
            else {
                showConversationList(ListArranger.arrangeByUnread(sentList));
            }
        }
    }

    private void send(Conversation conversation) {
        Task<Void> sendTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    new Connection().sendMail(conversation);
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
        Email first = selectedConv.getMessages().get(0);
        String receiver = first.getSender().getUsername().equals(currentUser.user.getUsername()) ?
                first.getReceiver() : first.getSender().getUsername();
        receiverTextField.setText(receiver);
        receiverTextField.setEditable(false);
        subjectTextField.setText(selectedConv.getMessages().get(0).getSubject());
        subjectTextField.setEditable(false);
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
        receiverTextField.setText("");
        subjectTextField.setText("");
        textArea.setText("");
        attachedFilesTextArea.setText("");
        newPane.setVisible(false);
        composeButton.setSelected(false);
        receiverTextField.setEditable(true);
        subjectTextField.setEditable(true);
        attachedFiles = new ArrayList<>();
    }

    private void forwardMessage() {
        compose();
        subjectTextField.setText(forwardedMessage.getSubject());
        textArea.setText("Forwarded message from: " + forwardedMessage.getSender().getUsername() + "@googlemail.com\n" +
                forwardedMessage.getText());
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
        profilePicture = null;
        currentUser.user = null;
        updaterThread.join();
        inboxList = null;
        sentList = null;
        new PageLoader().load("/SignIn.fxml");
    }

    public void backToConvList() {
        selectedConv = null;
        if (inboxButton.isSelected()) {
            showConversationList(inboxList);
            currentListType = ListType.inbox;
        }
        else {
            showConversationList(sentList);
            currentListType = ListType.sent;
        }
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

    public void block() {
        String otherUser = selectedConv.getMessages().get(0).getReceiver().equals(currentUser.user.getUsername()) ?
                selectedConv.getMessages().get(0).getSender().getUsername() :
                selectedConv.getMessages().get(0).getReceiver();
        try {
            new Connection().handleBlock(MessageType.block, otherUser);
            blockButton.setVisible(false);
            unblockButton.setVisible(true);
            currentUser.user.addBlockedUser(otherUser);
        }
        catch (IOException e) {
            serverError = true;
            showServerError();
        }
    }

    public void unblock() {
        String otherUser = selectedConv.getMessages().get(0).getReceiver().equals(currentUser.user.getUsername()) ?
                selectedConv.getMessages().get(0).getSender().getUsername() :
                selectedConv.getMessages().get(0).getReceiver();
        try {
            new Connection().handleBlock(MessageType.unblock, otherUser);
            blockButton.setVisible(true);
            unblockButton.setVisible(false);
            currentUser.user.removeBlockedUser(otherUser);
        }
        catch (IOException e) {
            serverError = true;
            showServerError();
        }
    }


    public void goToBlockedList(MouseEvent mouseEvent) throws IOException {
        new PageLoader().load("/BlockedUsersList.fxml");
    }
}

class LoadInbox extends Thread {
    @Override
    public void run() {
        try {
            EmailsController.inboxList = new Connection().getList(MessageType.inbox);
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
            EmailsController.sentList = new Connection().getList(MessageType.sent);
        }
        catch (IOException e) {
            EmailsController.serverError = true;
        }
    }
}