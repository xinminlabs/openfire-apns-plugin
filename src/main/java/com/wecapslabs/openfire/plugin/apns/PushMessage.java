package com.wecapslabs.openfire.plugin.apns;

import javapns.Push;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import javapns.notification.ResponsePacket;
import javapns.communication.exceptions.KeystoreException;
import javapns.communication.exceptions.CommunicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PushMessage extends Thread {

    private static final Logger Log = LoggerFactory.getLogger(OpenfireApns.class);

    private OpenfireApnsDBHandler dbManager;

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

        dbManager = new OpenfireApnsDBHandler();
    }

    public void run() {
        try {
            PushedNotifications notifications = Push.combined(message, badge, sound, keystore, password, production, token);

            for (PushedNotification notification : notifications) {
                if (notification.isSuccessful()) {
                    /* Apple accepted the notification and should deliver it */
                    Log.info("Push notification sent successfully to: " + notification.getDevice().getToken());
                    /* Still need to query the Feedback Service regularly */
                } else {
                    String invalidToken = notification.getDevice().getToken();
                    dbManager.deleteDeviceToken(invalidToken);

                    /* Find out more about what the problem was */
                    Exception theProblem = notification.getException();
                    Log.error(theProblem.getMessage(), theProblem);

                    /* If the problem was an error-response packet returned by Apple, get it */
                    ResponsePacket theErrorResponse = notification.getResponse();
                    if (theErrorResponse != null) {
                        Log.info(theErrorResponse.getMessage());
                    }
                }
            }
        } catch (KeystoreException e) {
            /* A critical problem occurred while trying to use your keystore */
            Log.error(e.getMessage(), e);
        } catch (CommunicationException e) {
            /* A critical communication error occurred while trying to contact Apple servers */
            Log.error(e.getMessage(), e);
        }
    }
}
