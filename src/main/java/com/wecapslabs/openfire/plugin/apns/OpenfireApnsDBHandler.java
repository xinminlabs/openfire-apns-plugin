package com.wecapslabs.openfire.plugin.apns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.NotFoundException;

import org.xmpp.packet.JID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class OpenfireApnsDBHandler {

    private static final Logger Log = LoggerFactory.getLogger(OpenfireApns.class);

    private static final String LOAD_TOKEN = "SELECT devicetoken FROM ofAPNS WHERE JID=?";
    private static final String INSERT_TOKEN = "INSERT INTO ofAPNS VALUES(?, ?) ON DUPLICATE KEY UPDATE devicetoken = ?";

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
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            isCompleted = false;
        }
        finally {
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
        }
        catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            returnToken = sqle.getMessage();
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return returnToken;
    }
}
