package com.wecapslabs.openfire.plugin.apns;

import javapns.Push;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import javapns.notification.ResponsePacket;
import javapns.communication.exceptions.KeystoreException;
import javapns.communication.exceptions.CommunicationException;

class PushMessage extends Thread {

    private String message;
    private int badge;
    private String sound;
    private Object keystore;
    private String password;
    private boolean production;
    private String token;

    public PushMessage(String message, int badge, String sound, Object keystore, String password, boolean production, String token ) {
        this.message = message;
        this.badge = badge;
        this.sound = sound;
        this.keystore = keystore;
        this.password = password;
        this.production = production;
        this.token = token;
    }

    public void run() {
        try {
            Push.combined(message, badge, sound, keystore, password, production, token);
        } catch (KeystoreException e) {
            e.printStackTrace();
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }
}
