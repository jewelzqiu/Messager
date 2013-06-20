package com.jewelzqiu.messager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    DataBaseHelper DBHelper;
    ChatAdapter mChatAdapter;
//    ConversationAdapter mAdapter;

    String username;
    String TableName;
    List<Map<String, Object>> conversation_list;

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
                        mChat.sendMessage(message);
                        updateConsersation(message, true, System.currentTimeMillis());
                        composeEditText.setText("");
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

//        conversation_list = new ArrayList<Map<String, Object>>();
//        mAdapter = new ConversationAdapter(this, R.id.text, conversation_list);
//        conversationListView.setAdapter(mAdapter);

        int key = getIntent().getIntExtra("key", -1);
        if (key == -1) {
            finish();
        }
        MessagerApp messagerApp = (MessagerApp) getApplication();
        mConnection = messagerApp.getConnection();
        mEntry = messagerApp.getEntries()[key];
        username = mEntry.getUser();
        TableName = getTableName();
        setTitle(username);

        mChat = mConnection.getChatManager().createChat(username, new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                updateConsersation(message.getBody(), false, System.currentTimeMillis());
            }
        });

        DBOperations();
    }

    private String getTableName() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(username.getBytes());
        byte[] bytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            int v = bytes[i] & 0xff;
            if(v < 16){
                sb.append(0);
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    private void updateConsersation(String message, boolean mine, long time) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        int isMine = mine ? 1 : 0;
        db.execSQL("INSERT INTO " + TableName + "(" +
                DataBaseHelper.CHAT_CONTENT + ", " +
                DataBaseHelper.CHAT_IS_MINE + ", " +
                DataBaseHelper.CHAT_TIME + ")" +
                " VALUES ('" + message + "', " + isMine + ", " + time + ")");
        mChatAdapter.changeCursor(db.rawQuery("SELECT * FROM " + TableName, null));
        mChatAdapter.notifyDataSetChanged();
        conversationListView.requestFocus();
//
//
//        System.out.println(mine + "\t" + message);
//        Map<String, Object> conversationItem = new HashMap<String, Object>();
//        conversationItem.put("msg", message);
//        conversationItem.put("mine", mine);
//        conversationItem.put("id", new Long(conversation_list.size()));
//        conversation_list.add(conversationItem);
//        mAdapter.refresh(conversation_list);
//        conversationListView.refreshDrawableState();
    }

    private void DBOperations () {
        DBHelper = new DataBaseHelper(getApplicationContext(), DataBaseHelper.DB_NAME, null, 1);
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        String sql = "SELECT COUNT(*) AS c FROM Sqlite_master WHERE TYPE = 'table' AND NAME = '" +
                TableName.trim() + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null || !cursor.moveToNext() || cursor.getInt(0) <= 0) {
            db.execSQL("CREATE TABLE " + TableName + "(" +
                DataBaseHelper.CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataBaseHelper.CHAT_CONTENT + " TEXT, " +
                DataBaseHelper.CHAT_IS_MINE + " INTEGER, " +
                DataBaseHelper.CHAT_TIME + " LONG)");
        }
//        db.execSQL("IF object_id('" + username + "') IS NULL\n" +
//                "CREATE TABLE " + username + "(" +
//                DataBaseHelper.CHAT_CONTENT + " TEXT, " +
//                DataBaseHelper.CHAT_IS_MINE + " INTEGER, " +
//                DataBaseHelper.CHAT_TIME + " LONG)");
        cursor = db.rawQuery("SELECT * FROM " + TableName, null);
        mChatAdapter = new ChatAdapter(this, cursor, false);
        conversationListView.setAdapter(mChatAdapter);
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
            View view;
            boolean isMine = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.CHAT_IS_MINE)) == 1;
            String content = cursor.getString(cursor.getColumnIndex(DataBaseHelper.CHAT_CONTENT));
            int resourceID;
            if (isMine) {
                resourceID = R.layout.listitem_me;
            } else {
                resourceID = R.layout.listitem_friends;
            }
            view = mInflater.inflate(resourceID, viewGroup, false);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(content);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }

//    class ConversationAdapter extends ArrayAdapter {
//        private Context mContext;
//        private LayoutInflater mInflater;
//        private List<Map<String, Object>> mList;
//
//        public ConversationAdapter(Context context, int textViewResourceId, List<Map<String, Object>> list) {
//            super(context, textViewResourceId, list);
//            this.mContext = context;
//            this.mInflater = LayoutInflater.from(mContext);
//            this.mList = list;
//        }
//
//        @Override
//        public int getCount() {
//            return mList.size();
//        }
//
//        @Override
//        public Object getItem(int i) {
//            return mList.get(i);
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return (Long) mList.get(i).get("id");
//        }
//
//        @Override
//        public View getView(int position, View view, ViewGroup viewGroup) {
//
//            View convertView = view;
//
//            if (convertView == null) {
//                int resourceID;
//                if ((Boolean) mList.get(position).get("mine")) {
//                    resourceID = R.layout.listitem_me;
//                } else {
//                    resourceID = R.layout.listitem_friends;
//                }
//                convertView = mInflater.inflate(resourceID, null);
//                TextView textView = (TextView) convertView.findViewById(R.id.text);
//                textView.setText(mList.get(position).get("msg").toString());
//            }
//
//            view = convertView;
//
//            return view;
//        }
//
//        public void refresh(List<Map<String, Object>> list) {
//            mList = list;
//            notifyDataSetChanged();
//        }
//
//    }

}