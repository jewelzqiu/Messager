package com.jewelzqiu.messager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by jewelzqiu on 6/13/13.
 */
public class FriendsListActivity extends PreferenceActivity {

    private HashMap<String, Integer> mEntryIndex;
    private Handler mHandler;
    private Connection mConnection;
    private RosterEntry[] mEntries;
    private MessagerApp mMessagerApp;
    private DataBaseHelper DBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBHelper = new DataBaseHelper(this, DataBaseHelper.DB_NAME, null, 1);

        addPreferencesFromResource(R.xml.friends_list);

        mMessagerApp = (MessagerApp) getApplication();
        mConnection = mMessagerApp.getConnection();
        mConnection.getChatManager().addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new NewMessageListener());
            }
        });

        updateFriendsList();

        mConnection.getRoster().addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> strings) {
                updateFriendsList();
            }

            @Override
            public void entriesUpdated(Collection<String> strings) {
                updateFriendsList();
            }

            @Override
            public void entriesDeleted(Collection<String> strings) {
                updateFriendsList();
            }

            @Override
            public void presenceChanged(Presence presence) {
            }
        });

        registerForContextMenu(getListView());

        mHandler = new FriendListHandler();
        mMessagerApp.setFriendsListHandler(mHandler);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mConnection.disconnect();
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.delete
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateFriendsList() {
        getPreferenceScreen().removeAll();
        Roster roster = mConnection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();
        int EntrySize = entries.size();
        mEntries = new RosterEntry[EntrySize];
        mEntryIndex = new HashMap<String, Integer>(EntrySize);
        Chat[] chats = new Chat[EntrySize];
        int i = 0;
        for (RosterEntry entry : entries) {
            mEntries[i] = entry;
            String name = entry.getUser();
            mEntryIndex.put(name, i);
            Preference preference = new Preference(this);
            preference.setTitle(name);
            preference.setKey("" + i++);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    preference.setSummary("");
                    Intent intent = new Intent(FriendsListActivity.this, ConversationActivity.class);
                    intent.putExtra("key", new Integer(preference.getKey()));
                    startActivity(intent);
                    return true;
                }
            });
            getPreferenceScreen().addPreference(preference);
        }
        mMessagerApp.setEntries(mEntries);
        mMessagerApp.setEntryIndex(mEntryIndex);
        mMessagerApp.setChats(chats);
    }

    private void addFriend(String user, String name, String[] group) {
        try {
            mConnection.getRoster().createEntry(user, name, group);
            updateFriendsList();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void deleteFriend(int index) {
        try {
            mConnection.getRoster().removeEntry(mEntries[index]);
            updateFriendsList();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

//    class MyChatListener implements ChatManagerListener {
//
//        @Override
//        public void chatCreated(Chat chat, boolean createdLocally) {
////            StringTokenizer st = new StringTokenizer(chat.getParticipant(), "/");
////            String user = st.nextToken();
//            chat.addMessageListener(new NewMessageListener());
////            int index = mEntryIndex.get(user);
////            mMessagerApp.getChats()[index] = chat;
//        }
//    }

    class NewMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            StringTokenizer st = new StringTokenizer(chat.getParticipant(), "/");
            String user = st.nextToken();
            System.out.println("" + user + ": " + message.getBody());
            DBHelper.insertMessage(user, message.getBody(), false, System.currentTimeMillis());
            runOnUiThread(new NewMessageRunnable(mEntryIndex.get(user)));
        }
    }

    class NewMessageRunnable implements Runnable {

        Preference mPreference;

        public NewMessageRunnable(int position) {
            mPreference = getPreferenceScreen().getPreference(position);
        }

        @Override
        public void run() {
            mPreference.setSummary("New message!");
            Handler handler = mMessagerApp.getConversationHandler();
            if (handler != null) {
                handler.sendEmptyMessage(MessagerApp.REFRESH_UI);
            }
        }
    }

    class FriendListHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MessagerApp.CLEAR_NEW_MESSAGES) {
                runOnUiThread(new ClearNewMsgRunnable(msg.arg1));
            }
        }
    }

    class ClearNewMsgRunnable implements Runnable {

        Preference mPreference;

        public ClearNewMsgRunnable(int position) {
            mPreference = getPreferenceScreen().getPreference(position);
        }

        @Override
        public void run() {
            mPreference.setSummary("");
        }
    }
}
