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

    public List<Conversation> getList (MessageType messageType) {
        List<Conversation> list = null;
        try {
            out.writeObject(new ServerMessage(messageType, new User(username, "")));
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
     * @return returns a conversation only containing an error email from the
     * Mail Delivery Subsystem if the receiver of the new mail didn't exist,
     * else returns the same conversation if the receiver existed.
     */
    public Conversation sendMail(Conversation conversation) {
        Conversation conv = null;
        try {
            out.writeObject(conversation);
            out.flush();
            conv = (Conversation) in.readObject();
            terminate();
        }
        catch (IOException | ClassNotFoundException e) {
            e.getMessage();
        }
        return conv;
    }

    private void terminate() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}