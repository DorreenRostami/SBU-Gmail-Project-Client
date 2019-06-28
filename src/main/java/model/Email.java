package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Email extends Message implements Serializable {
    private static final long serialVersionUID = 2L;

    private String sender;
    private String receiver;
    private String time;
    private String subject = "No Subject";
    private String text;
    private byte[] fileBytes;
    private boolean read = false;

    public Email(MessageType messageType, String sender, String receiver, String subject, String text, byte[] fileBytes) {
        this.messageType = messageType;
        this.sender = sender;
        this.text = text;
        this.receiver = receiver;
        if (subject.length() > 0)
            this.subject = subject;
        if (fileBytes.length > 0)
            this.fileBytes = fileBytes;
        this.time = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date());
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTime() {
        return time;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}