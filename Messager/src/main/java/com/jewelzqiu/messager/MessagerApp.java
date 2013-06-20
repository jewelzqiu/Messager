package com.jewelzqiu.messager;

import android.app.Application;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;

/**
 * Created by jewelzqiu on 6/13/13.
 */
public class MessagerApp extends Application {

    private Connection mConnection;
    private RosterEntry[] mEntries;

    public Connection getConnection() {
        return mConnection;
    }

    public void setConnection(Connection connection) {
        mConnection = connection;
    }

    public RosterEntry[] getEntries() {
        return mEntries;
    }

    public void setEntries(RosterEntry[] entries) {
        mEntries = entries;
    }
}
