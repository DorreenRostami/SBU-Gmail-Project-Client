package model;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection {
    private static final int requestPort = 8080;
    private static final String serverIP = "localhost";

    private String username;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Connection(String username) throws IOException {
        this.username = username;
        socket = new Socket(serverIP, requestPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public List<SignUpFeedback> settingsConnection(User user, String pass2) {
        List<SignUpFeedback> messageList = new ArrayList<>();
        try {
            out.writeObject(new ServerMessage(MessageType.change, user, pass2));
            SignUpFeedback str = null;
            while (str != SignUpFeedback.changed) {
                str = (SignUpFeedback) in.readObject();
                messageList.add(str);
            }
            terminate();
        }
        catch (IOException | ClassNotFoundException e) {
            e.getMessage();
        }
        return messageList;
    }

    public void signUpConnection(User user) {
        try {
            out.writeObject(new ServerMessage(MessageType.makeAccount, user));
        }
        catch (IOException e) {
            e.getMessage();
        }
    }

    public List<SignUpFeedback> signUpConnection (User user, String pass2) {
        List<SignUpFeedback> messageList = new ArrayList<>();
        try {
            out.writeObject(new ServerMessage(MessageType.signUp, user, pass2));
            SignUpFeedback str = null;
            while (str != SignUpFeedback.signedUp) {
                str = (SignUpFeedback) in.readObject();
                messageList.add(str);
            }
            terminate();
        }
        catch (IOException | ClassNotFoundException e) {
            e.getMessage();
        }
        return messageList;
    }

    public boolean signInConnection (String password) {
        try {
            out.writeObject(new ServerMessage(MessageType.signIn, new User(username, password)));
            out.flush();
            User u = (User) in.readObject();
            terminate();
            if (u != null) {
                currentUser.user = u;
                return true;
            }
        }
        catch (IOException | ClassNotFoundException e) {
            e.getMessage();
        }
        return false;
    }

    public List<Conversation> getList (MessageType listType) {
        List<Conversation> list = null;
        try {
            out.writeObject(new ServerMessage(listType, new User(username, "")));
            out.flush();
            list = (List<Conversation>) in.readObject();
            terminate();
        }
        catch (IOException | ClassNotFoundException e) {
            e.getMessage();
        }
        return list;
    }

    /**
     * sends the conversation which contains the new email to the server.
     * @param conversation the conversation containing the new email
     * @return void
     */
    public void sendMail(Conversation conversation) {
        try {
            out.writeObject(new ServerMessage(MessageType.send, conversation));
            out.flush();
            terminate();
        }
        catch (IOException e) {
            e.getMessage();
        }
    }

    public void deleteConversation(Conversation conversation) {
        try {
            out.writeObject(new ServerMessage(MessageType.deleteConversation, conversation));
            out.flush();
        }
        catch (IOException e) {
            e.getMessage();
        }
    }

    public void deleteMessage(Conversation conversation, Email email) {
        try {
            out.writeObject(new ServerMessage(MessageType.deleteMessage, conversation, email));
            out.flush();
        }
        catch (IOException e) {
            e.getMessage();
        }
    }

    public void saveListChanges(List<Conversation> inbox, List<Conversation> sent){
        try {
            out.writeObject(new ServerMessage(MessageType.updateInbox, inbox));
            out.flush();
            out.writeObject(new ServerMessage(MessageType.updateSent, sent));
            out.flush();
            terminate();
        }
        catch (IOException e) {
            e.getMessage();
        }
    }

    private void terminate() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}