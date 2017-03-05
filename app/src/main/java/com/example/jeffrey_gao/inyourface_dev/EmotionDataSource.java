package com.example.jeffrey_gao.inyourface_dev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;


/**
 * Created by ER on 1/28/2017.
 */

//the middle layer which handles the insertions of the ExerciseEntries into the database
public class EmotionDataSource {
    private SQLiteDatabase database;
    private SQLiteOpenHelper mySQLiteOpenHelper;
    private Context context;


    //constructor, which just creates a SQLiteOpenHelper
    public EmotionDataSource(Context context) {
        this.context = context;
        mySQLiteOpenHelper = new MySQLiteOpenHelper(context);

    }

    //opens a database for writing
    public void open() {
        database = mySQLiteOpenHelper.getWritableDatabase();
    }


    //closes the database
    public void close() {
        mySQLiteOpenHelper.close();
    }



    //inserts a point of emotion data into the database
    public long insertDataPoint(EmotionDataPoint emotionDataPoint) {
        ContentValues values  = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_ACTIVITY, emotionDataPoint.getActivity());
        values.put(MySQLiteOpenHelper.COLUMN_ANGER, emotionDataPoint.getAnger());
        values.put(MySQLiteOpenHelper.COLUMN_FEAR, emotionDataPoint.getFear());
        values.put(MySQLiteOpenHelper.COLUMN_DISGUST, emotionDataPoint.getDisgust());
        values.put(MySQLiteOpenHelper.COLUMN_JOY, emotionDataPoint.getJoy());
        values.put(MySQLiteOpenHelper.COLUMN_SADNESS, emotionDataPoint.getSadness());
        values.put(MySQLiteOpenHelper.COLUMN_SURPRISE, emotionDataPoint.getSurprise());


        long id = database.insert(MySQLiteOpenHelper.TABLE_NAME, null, values);
        return id;

    }







    //this returns a data point from a row in the table given the row ID
    public EmotionDataPoint getEntry(long id) {
        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.ALL_COLUMNS,
                MySQLiteOpenHelper.COLUMN_ID + " = " + id, null, null, null, null);

        cursor.moveToFirst();

        EmotionDataPoint emotionDataPoint = cursorToDataPoint(cursor);

        cursor.close();

        return emotionDataPoint;
    }

    //this deletes the row of a table given its row ID
    public void deleteEntry(long id) {

        database.delete(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.COLUMN_ID + " = " + id, null);


    }

    //used during debugging
    /*public void deleteAllDataPoints() {
        database.delete(MySQLiteOpenHelper.TABLE_NAME, null, null);
    }*/

    public ArrayList<EmotionDataPoint> getAllDataPoints() {
        ArrayList<EmotionDataPoint> list = new ArrayList<EmotionDataPoint>();

        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.ALL_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            list.add(cursorToDataPoint(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }


    private EmotionDataPoint cursorToDataPoint(Cursor cursor) {

        EmotionDataPoint emotionDataPoint = new EmotionDataPoint(context);

        emotionDataPoint.setId(cursor.getLong(0));
        emotionDataPoint.setActivity(cursor.getString(1));
        emotionDataPoint.setAnger(cursor.getInt(2));
        emotionDataPoint.setFear(cursor.getInt(3));
        emotionDataPoint.setDisgust(cursor.getInt(4));
        emotionDataPoint.setJoy(cursor.getInt(5));
        emotionDataPoint.setSadness(cursor.getInt(6));
        emotionDataPoint.setSurprise(cursor.getInt(7));



        return emotionDataPoint;
    }


}
