package model;

import java.io.Serializable;

public enum MessageType implements Serializable {
    /**
     * when user first signs in
     */
    signIn,
    /**
     * the message type when an email is first composed
     */
    message,
    /**
     * the message type when an email is replied to/is a reply
     */
    conversation,
    /**
     * when user signs out
     */
    signOut,
    /**
     * when user creates a new account
     */
    signUp,
    /**
     * when user is done adding additional info
     */
    makeAccount
}
