package com.wecapslabs.openfire.plugin.apns;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.NotFoundException;

import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class OpenfireApns implements Plugin, PacketInterceptor {

    public static final Logger Log = LoggerFactory.getLogger(OpenfireApns.class);

    private String password;
    private InterceptorManager interceptorManager;
    private OpenfireApnsDBHandler dbManager;

    public OpenfireApns() {
        interceptorManager = InterceptorManager.getInstance();
        dbManager = new OpenfireApnsDBHandler();
    }

    public static String keystorePath() {
        return "./keystore.p12";
    }

    public void setPassword(String password) {
        JiveGlobals.setProperty("plugin.apns.password", password);
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void initializePlugin(PluginManager pManager, File pluginDirectory) {
        interceptorManager.addInterceptor(this);

        IQHandler myHandler = new OpenfireApnsIQHandler();
        IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
        iqRouter.addHandler(myHandler);

        this.password = JiveGlobals.getProperty("plugin.apns.password", "");
    }

    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {

        if (isValidTargetPacket(packet, read, processed)) {
            Packet original = packet;

            if(original instanceof Message) {
                Message receivedMessage = (Message) original;

                JID targetJID = receivedMessage.getTo();

                String targetJID_Bare = targetJID.toBareJID();
                String body = receivedMessage.getBody();

                String[] userID = targetJID_Bare.split("@");

                String payloadString = userID[0];
                payloadString = payloadString.concat(": ");
                payloadString = payloadString.concat(body);

                String deviceToken = dbManager.getDeviceToken(targetJID);
                if (deviceToken == null) return;

                PushMessage message = new PushMessage(payloadString, 1, "Default.caf", OpenfireApns.keystorePath(), getPassword(), false, deviceToken);
                message.start();
            }
        }
    }

    private boolean isValidTargetPacket(Packet packet, boolean read, boolean processed) {
        return  !processed && read && packet instanceof Message;
    }
}
