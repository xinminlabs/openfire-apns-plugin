package com.wecapslabs.openfire.plugin.apns;

import java.io.File;
import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;

import org.jivesoftware.util.JiveGlobals;

import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApnsPlugin implements Plugin, PacketInterceptor {

    private static final Logger Log = LoggerFactory.getLogger(ApnsPlugin.class);

    private InterceptorManager interceptorManager;
    private ApnsDBHandler dbManager;

    public ApnsPlugin() {
        interceptorManager = InterceptorManager.getInstance();
        dbManager = new ApnsDBHandler();
    }

    public static String keystorePath() {
        return "./keystore.p12";
    }

    public void setPassword(String password) {
        JiveGlobals.setProperty("plugin.apns.password", password);
    }

    public String getPassword() {
        return JiveGlobals.getProperty("plugin.apns.password", "");
    }

    public void setBadge(String badge) {
        JiveGlobals.setProperty("plugin.apns.badge", badge);
    }

    public int getBadge() {
        return Integer.parseInt(JiveGlobals.getProperty("plugin.apns.badge", "1"));
    }

    public void setSound(String sound) {
        JiveGlobals.setProperty("plugin.apns.sound", sound);
    }

    public String getSound() {
        return JiveGlobals.getProperty("plugin.apns.sound", "default");
    }

    public void setProduction(String production) {
        JiveGlobals.setProperty("plugin.apns.production", production);
    }

    public boolean getProduction() {
        return Boolean.parseBoolean(JiveGlobals.getProperty("plugin.apns.production", "false"));
    }

    public void initializePlugin(PluginManager pManager, File pluginDirectory) {
        interceptorManager.addInterceptor(this);

        IQHandler myHandler = new ApnsIQHandler();
        IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
        iqRouter.addHandler(myHandler);
    }

    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {

        if (isValidTargetPacket(packet, read, processed)) {
            Packet original = packet;

            if(original instanceof Message) {
                Message receivedMessage = (Message) original;

                if (receivedMessage.getType() == Message.Type.chat) {
                    JID targetJID = receivedMessage.getTo();

                    String user = targetJID.getNode();
                    String body = receivedMessage.getBody();
                    String payloadString = user + ": " + body;

                    String deviceToken = dbManager.getDeviceToken(targetJID);
                    if (deviceToken == null) return;

                    new PushMessage(payloadString, getBadge(), getSound(), ApnsPlugin.keystorePath(), getPassword(), getProduction(), deviceToken).start();
                } else if (receivedMessage.getType() == Message.Type.groupchat) {
                    JID sourceJID = receivedMessage.getFrom();
                    JID targetJID = receivedMessage.getTo();

                    String user = sourceJID.getNode();
                    String body = receivedMessage.getBody();
                    String payloadString = user + ": " + body;
                    String roomName = targetJID.getNode();

                    List<String> deviceTokens = dbManager.getDeviceTokens(roomName);
                    if (deviceTokens.isEmpty()) return;

                    new PushMessage(payloadString, getBadge(), getSound(), ApnsPlugin.keystorePath(), getPassword(), getProduction(), deviceTokens).start();
                }
            }
        }
    }

    private boolean isValidTargetPacket(Packet packet, boolean read, boolean processed) {
        return  !processed && read && packet instanceof Message;
    }
}
