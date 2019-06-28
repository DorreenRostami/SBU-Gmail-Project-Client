package model;

import java.io.Serializable;

public class ServerMessage extends Message implements Serializable {
    static final long serialVersionUID = 2L;

    private MessageType messageType;

    ServerMessage(MessageType messageType, User sender) {
        this.messageType = messageType;
        this.sender = sender;
    }

    ServerMessage(MessageType messageType, User sender, String pass2) {
        this(messageType, sender);
        this.text = pass2;
    }

    MessageType getMessageType() {
        return messageType;
    }
}