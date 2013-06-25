package com.jewelzqiu.messager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jewelzqiu on 6/18/13.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "messager";
    public static final String TABLE_NAME_INFO = "info";
    public static final String INFO_USERNAME = "username";
    public static final String INFO_NICKNAME = "nickname";
    public static final String INFO_PASSWORD = "password";
    public static final String CHAT_ID = "_id";
    public static final String CHAT_CONTENT = "content";
    public static final String CHAT_TIME = "time";
    public static final String CHAT_IS_MINE = "ismine";

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("onCreate");
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME_INFO + "(" +
                INFO_USERNAME + " VARCHAR(10), " +
                INFO_NICKNAME + " VARCHAR(10), " +
                INFO_PASSWORD + " STRING)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public Cursor insertMessage(String username, String message, boolean isMine, long time) {
        String TableName = getTableName(username);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT COUNT(*) AS c FROM Sqlite_master WHERE TYPE = 'table' AND NAME = '" +
                TableName.trim() + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null || !cursor.moveToNext() || cursor.getInt(0) <= 0) {
            db.execSQL("CREATE TABLE " + TableName + "(" +
                    CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CHAT_CONTENT + " TEXT, " +
                    CHAT_IS_MINE + " INTEGER, " +
                    CHAT_TIME + " LONG)");
        } /*else {
            db.execSQL("DROP TABLE " + TableName);
            db.execSQL("CREATE TABLE " + TableName + "(" +
                    DataBaseHelper.CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DataBaseHelper.CHAT_CONTENT + " TEXT, " +
                    DataBaseHelper.CHAT_IS_MINE + " INTEGER, " +
                    DataBaseHelper.CHAT_TIME + " LONG)");
        }*/
        int ismine = isMine ? 1 : 0;
        db.execSQL("INSERT INTO " + TableName + "(" +
                CHAT_CONTENT + ", " + CHAT_IS_MINE + ", " + CHAT_TIME + ")" +
                " VALUES ('" + message + "', " + ismine + ", " + time + ")");
        String querySQL =
                "SELECT * FROM (" +
                        "SELECT * FROM " + TableName +
                        " ORDER BY " + CHAT_ID + " DESC LIMIT 100)" +
                        " ORDER BY " + CHAT_ID + " ASC";
        return db.rawQuery(querySQL, null);
    }

    public Cursor queryMessage(String username) {
        String TableName = getTableName(username);
        SQLiteDatabase db = getReadableDatabase();
        String querySQL =
                "SELECT * FROM (" +
                        "SELECT * FROM " + TableName +
                        " ORDER BY " + CHAT_ID + " DESC LIMIT 100)" +
                        " ORDER BY " + CHAT_ID + " ASC";
        return db.rawQuery(querySQL, null);
    }

    private String getTableName(String username) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(username.getBytes());
        byte[] bytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            if (v < 16) {
                sb.append(0);
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

}
