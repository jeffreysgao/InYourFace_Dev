package com.example.jeffrey_gao.inyourface_dev;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tong on 3/5/2017.
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "emotion_values";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACTIVITY = "activity";
    public static final String COLUMN_ANGER = "anger";
    public static final String COLUMN_FEAR = "fear";
    public static final String COLUMN_DISGUST = "disgust";
    public static final String COLUMN_JOY = "joy";
    public static final String COLUMN_SADNESS = "sadness";
    public static final String COLUMN_SURPRISE = "surprise";
    public static final String COLUMN_ATTENTION = "attention";


    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_ACTIVITY, COLUMN_ANGER, COLUMN_FEAR, COLUMN_DISGUST,
        COLUMN_JOY, COLUMN_SADNESS, COLUMN_SURPRISE, COLUMN_ATTENTION};
    public static final String DATABASE_NAME = "emotion_values.db";
    public static final int DATABASE_VERSION = 1;

    //create our table
    public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ACTIVITY + " TEXT, "
            + COLUMN_ANGER + " FLOAT NOT NULL, "
            + COLUMN_FEAR + " FLOAT NOT NULL, "
            + COLUMN_DISGUST + " FLOAT NOT NULL, "
            + COLUMN_JOY + " FLOAT NOT NULL, "
            + COLUMN_SADNESS + " FLOAT NOT NULL, "
            + COLUMN_SURPRISE + " FLOAT NOT NULL, "
            + COLUMN_ATTENTION + " FLOAT NOT NULL " + ")";


    public static final String DATABASE_DELETE = "DELETE TABLE IF NOT EXISTS " + TABLE_NAME;

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    //we just recreate our table on upgrade
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(DATABASE_DELETE);
        database.execSQL(DATABASE_CREATE);
    }
}
