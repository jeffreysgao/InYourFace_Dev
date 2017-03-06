package com.example.jeffrey_gao.inyourface_dev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;


/**
 * Created by Tong on 3/5/2017.
 *
 * Database class: database, and database operation functions.
 */

//the middle layer which handles the insertions of the ExerciseEntries into the database
public class DataSource {
    private SQLiteDatabase database;
    private SQLiteOpenHelper mySQLiteOpenHelper;
    private Context context;


    /**
     * Constructor, which just creates a SQLiteOpenHelper
     */
    public DataSource(Context context) {
        this.context = context;
        mySQLiteOpenHelper = new MySQLiteOpenHelper(context);

    }

    /**
     * Opens a database for writing
     */
    public void open() {
        database = mySQLiteOpenHelper.getWritableDatabase();
    }


    /**
     * Closes the database
     */
    public void close() {
        mySQLiteOpenHelper.close();
    }


    /**
     * Inserts a point of emotion data into the database.
     */
    public long insertDataPoint(DataPoint dataPoint) {
        ContentValues values  = new ContentValues();
        values.put(MySQLiteOpenHelper.COLUMN_ACTIVITY, dataPoint.getActivity());
        values.put(MySQLiteOpenHelper.COLUMN_ANGER, dataPoint.getAnger());
        values.put(MySQLiteOpenHelper.COLUMN_FEAR, dataPoint.getFear());
        values.put(MySQLiteOpenHelper.COLUMN_DISGUST, dataPoint.getDisgust());
        values.put(MySQLiteOpenHelper.COLUMN_JOY, dataPoint.getJoy());
        values.put(MySQLiteOpenHelper.COLUMN_SADNESS, dataPoint.getSadness());
        values.put(MySQLiteOpenHelper.COLUMN_SURPRISE, dataPoint.getSurprise());
        values.put(MySQLiteOpenHelper.COLUMN_ATTENTION, dataPoint.getAttention());


        long id = database.insert(MySQLiteOpenHelper.TABLE_NAME, null, values);
        return id;
    }


    /**
     * this returns a data point from a row in the table given the row ID
     */
    public DataPoint getEntry(long id) {
        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.ALL_COLUMNS,
                MySQLiteOpenHelper.COLUMN_ID + " = " + id, null, null, null, null);

        cursor.moveToFirst();

        DataPoint dataPoint = cursorToDataPoint(cursor);

        cursor.close();

        return dataPoint;
    }

    /**
     * this deletes the row of a table given its row ID.
     */
    public void deleteEntry(long id) {

        database.delete(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.COLUMN_ID + " = " + id, null);


    }

    //used during debugging
    /*public void deleteAllDataPoints() {
        database.delete(MySQLiteOpenHelper.TABLE_NAME, null, null);
    }*/

    public ArrayList<DataPoint> getSelectedActivityDataPoints(String packageName) {
        ArrayList<DataPoint> list = new ArrayList<DataPoint>();

        Cursor cursor = database.query(MySQLiteOpenHelper.TABLE_NAME, MySQLiteOpenHelper.ALL_COLUMNS,
                MySQLiteOpenHelper.COLUMN_ACTIVITY + " = " + packageName, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            list.add(cursorToDataPoint(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }

    /**
     * Returns all emotion data points.
     * @return an arraylist of emotion data points
     */
    public ArrayList<DataPoint> getAllDataPoints() {
        ArrayList<DataPoint> list = new ArrayList<DataPoint>();

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


    /**
     * Converts the cursor into emotion data points
     * @param cursor
     * @return
     */
    private DataPoint cursorToDataPoint(Cursor cursor) {

        DataPoint dataPoint = new DataPoint(context);

        dataPoint.setId(cursor.getLong(0));
        dataPoint.setActivity(cursor.getString(1));
        dataPoint.setAnger(cursor.getFloat(2));
        dataPoint.setFear(cursor.getFloat(3));
        dataPoint.setDisgust(cursor.getFloat(4));
        dataPoint.setJoy(cursor.getFloat(5));
        dataPoint.setSadness(cursor.getFloat(6));
        dataPoint.setSurprise(cursor.getFloat(7));
        dataPoint.setAttention(cursor.getFloat(8));

        return dataPoint;
    }
}
