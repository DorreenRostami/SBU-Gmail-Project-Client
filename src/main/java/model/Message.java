package model;

import java.io.Serializable;

abstract class Message implements Serializable {
    static final long serialVersionUID = 2L;

    MessageType messageType;

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
