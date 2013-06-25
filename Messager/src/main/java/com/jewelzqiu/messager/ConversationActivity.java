package com.jewelzqiu.messager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

/**
 * Created by jewelzqiu on 6/17/13.
 */
public class ConversationActivity extends Activity {

    RosterEntry mEntry;
    Connection mConnection;
    Chat mChat;

    Button sendButton;
    EditText composeEditText;
    ListView conversationListView;

    Handler mHandler;

    MessagerApp mMessagerApp;
    DataBaseHelper DBHelper;
    ChatAdapter mChatAdapter;

    String username;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation);
        conversationListView = (ListView) findViewById(R.id.list_conversation);
        composeEditText = (EditText) findViewById(R.id.text_compose);
        sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChat != null) {
                    String message = composeEditText.getText().toString();
                    try {
                        System.out.println("local: me: " + message);
                        mChat.sendMessage(message);
                        mChatAdapter.changeCursor(DBHelper.insertMessage(
                                username, message, true, System.currentTimeMillis()));
                        composeEditText.setText("");
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        int key = getIntent().getIntExtra("key", -1);
        if (key == -1) {
            finish();
        }
        mMessagerApp = (MessagerApp) getApplication();
        mConnection = mMessagerApp.getConnection();
        mEntry = mMessagerApp.getEntries()[key];
        username = mEntry.getUser();
        setTitle(username);

        DBHelper = new DataBaseHelper(this, DataBaseHelper.DB_NAME, null, 1);
        mHandler = new UIHandler();
        mMessagerApp.setConversationHandler(mHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChat = mConnection.getChatManager().createChat(username, null);
        if (mChatAdapter == null) {
            mChatAdapter = new ChatAdapter(this, DBHelper.queryMessage(username), false);
            conversationListView.setAdapter(mChatAdapter);
        } else {
            mChatAdapter.changeCursor(DBHelper.queryMessage(username));
        }
        sendClearMessage();
    }

    @Override
    protected void onPause() {
        sendClearMessage();
        super.onPause();
    }

    private void sendClearMessage() {
        Handler handler = mMessagerApp.getFriendsListHandler();
        android.os.Message msg = handler.obtainMessage();
        msg.what = MessagerApp.CLEAR_NEW_MESSAGES;
        msg.arg1 = mMessagerApp.getEntryIndex().get(username);
        handler.sendMessage(msg);
    }

    class ChatAdapter extends CursorAdapter {

        private LayoutInflater mInflater;
        private Context mContext;

        public ChatAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            boolean isMine = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.CHAT_IS_MINE)) == 1;
            View view;
            int resourceID;
            if (isMine) {
                resourceID = R.layout.listitem_me;
            } else {
                resourceID = R.layout.listitem_friend;
            }
            view = mInflater.inflate(resourceID, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) view.findViewById(R.id.text);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String content = cursor.getString(cursor.getColumnIndex(DataBaseHelper.CHAT_CONTENT));
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            TextView textView = viewHolder.mTextView;
            textView.setText(content);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) getItem(position);
            return getItemViewType(cursor);
        }

        private int getItemViewType(Cursor cursor) {
            return cursor.getInt(cursor.getColumnIndex(DataBaseHelper.CHAT_IS_MINE));
        }
    }

    class ViewHolder {
        TextView mTextView;
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MessagerApp.REFRESH_UI) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatAdapter.changeCursor(DBHelper.queryMessage(username));
                    }
                });
            }
        }
    }
}