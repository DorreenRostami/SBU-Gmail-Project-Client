package controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
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
import java.util.List;
import java.util.concurrent.*;

public class EmailsController {
    static boolean serverError = false;
    private List<Conversation> inboxList = null;
    private List<Conversation> sentList = null;
    private List<Email> outboxList = new ArrayList<>();
    private List<Conversation> currentList = null;
    private static Conversation convToDelete = null;
    private List<FileInfo> attachedFiles = new ArrayList<>();
    private long GB = 2;

    @FXML public ListView<Conversation> convosListView;
    @FXML public ListView<Email> messagesListView;
    @FXML public ToggleButton composeButton, inboxButton, sentButton, outboxButton;
    @FXML public TitledPane newPane;
    @FXML public TextField receiverTextField, subjectTextField;
    @FXML public TextArea textArea, attachedFilesTextArea;
    @FXML public Button backToSignInButton, sendButton, attachButton, cancelButton;
    @FXML public Text sizeWarning;
    @FXML public Text serverErrorText;
    @FXML public AnchorPane conversationMessagesPane;
    @FXML public ImageView currentProfilePicture;

    public void initialize() {
        calculate();
        ByteArrayInputStream bis = new ByteArrayInputStream(currentUser.user.getImage());
        Image im = new Image(bis);
        currentProfilePicture.setImage(im);
        inboxButton.setSelected(true);
        try {
            bis.close();
            inboxList = currentUser.user.getInbox();
            currentList = inboxList;
        }
        catch (IOException e) {
            serverError = true;
        }
        if (!serverError) {
            showConversationList(inboxList);
        }
        else {
            showServerError();
        }
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
            convosListView.setPlaceholder(new Label("No Conversation"));
        }
        else {
            currentList = list;
            convosListView.setItems(FXCollections.observableArrayList(list));
            convosListView.setCellFactory(item -> new ConversationListItem());
        }
    }

    private void showMessageList(List<Email> list) {
        convosListView.setVisible(false);
        messagesListView.setVisible(true);
        conversationMessagesPane.setVisible(true);
        if (list == null || list.size() == 0) {
            messagesListView.setPlaceholder(new Label("No Messages"));
        }
        else {
            messagesListView.setItems(FXCollections.observableArrayList(list));
            messagesListView.setCellFactory(item -> new MessageListItem());
        }
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
            showConversationList(inboxList);
            if (inboxList != null) {
                return;
            }
            messageType = MessageType.inbox;
        }
        else if (actionEvent.getSource() == sentButton) {
            inboxButton.setSelected(false);
            outboxButton.setSelected(false);
            showConversationList(sentList);
            if (sentList != null) {
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
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<List<Conversation>> future = executor.submit(new ListLoader(messageType));
        if (future.isDone()) {
            List<Conversation> list = null;
            try {
                list = future.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.getMessage();
            }
            if (!serverError && list != null) {
                showConversationList(list);
            }
            else if (serverError) {
                showServerError();
            }
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
        Conversation newConv = new Conversation(email);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Conversation> future = executor.submit(new MailSender(newConv));

        if (future.isDone()) {
            Conversation returnedConv = null;
            try {
                returnedConv = future.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (!serverError && returnedConv != null) {
                if (!returnedConv.equals(newConv))
                    inboxList.add(returnedConv);
                outboxList.clear();

            }
            else showServerError();
        }


        /*final Conversation[] conv = new Conversation[1];
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

        new Thread(sendMailTask).start();*/
        closeComposePane();
    }

    private void updateOutbox() {
        if (!serverError)
            outboxList.clear();
        else
            showServerError();
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
        attachedFiles = new ArrayList<>();
    }

    static void setDeletedConversation(Conversation conversation) {
        convToDelete = conversation;
    }

    private void deleteConversation() {
        //TO DO make conn to dlete conv
        try {
            new Connection(currentUser.user.getUsername());
        }
        catch (IOException e) {
            serverError = true;
            return;
        }
        boolean currIsInbox = currentList == inboxList;
        inboxList.remove(convToDelete);
        sentList.remove(convToDelete);
        if (currIsInbox)
            showConversationList(inboxList);
        else
            showConversationList(sentList);
    }

    public void changePage(ActionEvent actionEvent) throws IOException {
        if (actionEvent.getSource() == backToSignInButton)
            new PageLoader().load("/SignIn.fxml");
    }
}

class ListLoader implements Callable<List<Conversation>> {
    private MessageType listType;
    ListLoader(MessageType listType) {
        this.listType = listType;
    }
    @Override
    public List<Conversation> call() {
        try {
            return new Connection(currentUser.user.getUsername()).getList(listType);
        }
        catch (IOException e) {
            EmailsController.serverError = true;
        }
        return null;
    }
}

class MailSender implements Callable<Conversation> {
    private Conversation conversation;
    MailSender(Conversation conversation) {
        this.conversation = conversation;
    }
    @Override
    public Conversation call() {
        try {
            return new Connection(currentUser.user.getUsername()).sendMail(conversation);
        }
        catch (IOException e) {
            EmailsController.serverError = true;
        }
        return null;
    }
}