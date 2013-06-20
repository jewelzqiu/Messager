package com.jewelzqiu.messager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}
