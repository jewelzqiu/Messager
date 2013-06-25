package com.jewelzqiu.messager;

import android.app.Application;
import android.os.Handler;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;

import java.util.HashMap;

/**
 * Created by jewelzqiu on 6/13/13.
 */
public class MessagerApp extends Application {

    private HashMap<String, Integer> EntryIndex;

    private Handler ConversationHandler;
    private Handler FriendsListHandler;

    private Connection mConnection;
    private RosterEntry[] mEntries;

    private Chat[] mChats;

    public static int REFRESH_UI = 0;
    public static int CLEAR_NEW_MESSAGES = 0;

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

    public HashMap<String, Integer> getEntryIndex() {
        return EntryIndex;
    }

    public void setEntryIndex(HashMap<String, Integer> entryIndex) {
        EntryIndex = entryIndex;
    }

    public Chat[] getChats() {
        return mChats;
    }

    public void setChats(Chat[] chats) {
        mChats = chats;
    }

    public Handler getConversationHandler() {
        return ConversationHandler;
    }

    public void setConversationHandler(Handler conversationHandler) {
        ConversationHandler = conversationHandler;
    }

    public Handler getFriendsListHandler() {
        return FriendsListHandler;
    }

    public void setFriendsListHandler(Handler friendsListHandler) {
        FriendsListHandler = friendsListHandler;
    }
}
