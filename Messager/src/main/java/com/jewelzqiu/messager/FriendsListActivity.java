package com.jewelzqiu.messager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * Created by jewelzqiu on 6/13/13.
 */
public class FriendsListActivity extends PreferenceActivity {

    private MessagerApp mMessagerApp;

    private Connection mConnection;
    private RosterEntry[] mEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.friends_list);

        mMessagerApp = (MessagerApp) getApplication();
        mConnection = mMessagerApp.getConnection();

        updateFriendsList();

        mConnection.getRoster().addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> strings) {
                System.out.println("entries Added");
                updateFriendsList();
            }

            @Override
            public void entriesUpdated(Collection<String> strings) {
                System.out.println("entries Updated");
                updateFriendsList();
            }

            @Override
            public void entriesDeleted(Collection<String> strings) {
                System.out.println("entries Deleted");
                updateFriendsList();
            }

            @Override
            public void presenceChanged(Presence presence) {
                System.out.println("presence Changed");
            }
        });

        registerForContextMenu(getListView());

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
        mEntries = new RosterEntry[entries.size()];
        int i = 0;
        for (RosterEntry entry : entries) {
            mEntries[i] = entry;
            String name = entry.getUser();
            Preference preference = new Preference(this);
            preference.setTitle(name);
            preference.setKey("" + i++);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(FriendsListActivity.this, ConversationActivity.class);
                    intent.putExtra("key", new Integer(preference.getKey()));
                    startActivity(intent);
                    return true;
                }
            });
            getPreferenceScreen().addPreference(preference);
        }
        mMessagerApp.setEntries(mEntries);
    }

    private void addFriend(String user, String name, String[] group) {
        try {
            mConnection.getRoster().createEntry(user, name, group);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void deleteFriend(int index) {
        try {
            mConnection.getRoster().removeEntry(mEntries[index]);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

}
