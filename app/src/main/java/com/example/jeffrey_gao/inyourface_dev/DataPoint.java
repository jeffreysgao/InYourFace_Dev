package com.example.jeffrey_gao.inyourface_dev;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Tong on 3/5/2017.
 *
 * This class stores the level of emotions for emotion analysis.
 */

public class DataPoint {

    private long id;
    private String activity = "";
    private float anger;
    private float fear;
    private float disgust;
    private float joy;
    private float sadness;
    private float surprise;
    private float attention;


    private Context context;


    public DataPoint(Context context) {this.context = context;}

    //getter and setter methods for the instance fields

    public void setId(long id) {this.id = id;}

    public long getId() {return id;}

    public void setActivity(String activity){this.activity = activity;}

    public String getActivity(){return activity;}

    public void setAnger(float anger){this.anger = anger;}

    public float getAnger(){return anger;}

    public void setFear(float fear){this.fear = fear;}

    public float getFear(){return fear;}

    public void setDisgust(float disgust){this.disgust = disgust;}

    public float getDisgust() {return disgust;}

    public void setJoy(float joy) {this.joy = joy;}

    public float getJoy() {return joy;}

    public void setSadness(float sadness) {this.sadness = sadness;}

    public float getSadness() {return sadness;}

    public void setSurprise(float surprise) {this.surprise = surprise;}

    public float getSurprise() {return surprise;}

    public void setAttention(float attention) {this.attention = attention;}

    public float getAttention() {return attention;}



}
