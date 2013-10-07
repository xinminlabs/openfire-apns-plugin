package com.wecapslabs.openfire.plugin.apns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import org.jivesoftware.database.DbConnectionManager;

import org.xmpp.packet.JID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApnsDBHandler {

    private static final Logger Log = LoggerFactory.getLogger(ApnsPlugin.class);

    private static final String LOAD_TOKEN = "SELECT devicetoken FROM ofAPNS WHERE JID=?";
    private static final String INSERT_TOKEN = "INSERT INTO ofAPNS VALUES(?, ?) ON DUPLICATE KEY UPDATE devicetoken = ?";
    private static final String DELETE_TOKEN = "DELETE FROM ofAPNS WHERE devicetoken = ?";
    private static final String LOAD_TOKENS = "SELECT devicetoken FROM ofAPNS LEFT JOIN ofMucMember ON ofAPNS.JID = ofMucMember.jid LEFT JOIN ofMucRoom ON ofMucMember.roomID = ofMucRoom.roomID WHERE ofMucRoom.name = ?";

    public boolean insertDeviceToken(JID targetJID, String token) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isCompleted = false;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_TOKEN);
            pstmt.setString(1, targetJID.toBareJID());
            pstmt.setString(2, token);
            pstmt.setString(3, token);
            pstmt.executeUpdate();
            pstmt.close();

            isCompleted = true;
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            isCompleted = false;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return isCompleted;
    }

    public boolean deleteDeviceToken(String token) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isCompleted = false;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_TOKEN);
            pstmt.setString(1, token);
            pstmt.executeUpdate();
            pstmt.close();

            isCompleted = true;
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            isCompleted = false;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return isCompleted;
    }

    public String getDeviceToken(JID targetJID) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String returnToken = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_TOKEN);
            pstmt.setString(1, targetJID.toBareJID());
            rs = pstmt.executeQuery();
            if (rs.next()) {
            	returnToken = rs.getString(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            returnToken = sqle.getMessage();
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return returnToken;
    }

    public List<String> getDeviceTokens(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<String> returnTokens = new ArrayList<String>();
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_TOKENS);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
            	returnTokens.add(rs.getString(1));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return returnTokens;
    }
}
