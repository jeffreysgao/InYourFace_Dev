package com.example.jeffrey_gao.inyourface_dev;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by jinnan on 2/25/17.
 *   edited by Tong on 3/5/17.
 *
 * The emotion analysis fragment, triggered by "Emotion Tracking" button
 * on the setting page.
 */

public class EmotionsFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    public static LineChart lineChart;
    public static RadarChart radarChart;
    public static Context context;
    public static Spinner spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.emotions_fragment, container, false);

        context = view.getContext();

        lineChart = (LineChart) view.findViewById(R.id.line_chart);

        radarChart = (RadarChart) view.findViewById(R.id.radar_chart);

        spinner = (Spinner) view.findViewById(R.id.activity_spinner);


        spinner.setOnItemSelectedListener(this);


        //we get a chartData object from the database


        new Thread() {

            public void run() {


                LineData lineData = generateLineData();


                lineChart.setDescription("Instances");

                lineChart.setData(lineData);
                lineChart.invalidate();


                RadarData radarData = generateRadarData();


                radarChart.setDescription("Rated from 0 to 100");

                radarChart.setData(radarData);
                radarChart.invalidate();
                radarChart.animate();
            }

        }.run();


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        refresh();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    /**
     * Generate the line charts.
     * @return the line chart data
     */
    public static LineData generateLineData() {

        List<Entry> values = new ArrayList<Entry>();

        DataSource source = new DataSource(context);
        source.open();


        //List<DataPoint> points = source.getAllDataPoints();

        List<DataPoint> points = source.getAllDataPoints();

        source.close();

        ArrayList<String> labels = new ArrayList<String>();

        int size = points.size();
        int i = 0;

        while (i < size) {
            Entry entry = new Entry(points.get(i).getAttention(), i);
            values.add(entry);

            labels.add(Integer.toString(i + 1));
            i++;
        }

        /*try {
            //read from the csv file
            //FileInputStream fis = context.openFileInput("emotions.csv");
            FileInputStream fis = context.openFileInput("emotionz.csv");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            String line;
            int instances = 0;



            //we read each comma-separated line in the file and build a line point object
            //the line point is (#instance, joy level)
            try {
                while ((line = reader.readLine()) != null) {
                    String[] broken = line.split(",");
                    Entry entry = new Entry(Float.parseFloat(broken[3]), instances);
                    values.add(entry);


                    labels.add(Integer.toString(instances + 1));

                    instances++;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            //close readers
            try {
                fis.close();
                isr.close();
                reader.close();
            } catch(IOException a) {
                a.printStackTrace();
            }

        } catch(FileNotFoundException i) {
            i.printStackTrace();
        }*/

        //create a line object from the point objects
        LineDataSet lineDataSet = new LineDataSet(values, "ATTENTION");
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(4);


        List<LineDataSet> lines = new ArrayList<LineDataSet>();
        lines.add(lineDataSet);

        //now we have the chart data
        LineData lineData = new LineData(labels, lineDataSet);

        return lineData;
    }

    /**
     * Generates the data for the hexagon data.
     * @return
     */
    public static RadarData generateRadarData() {

        List<Entry> values = new ArrayList<Entry>();

        DataSource source = new DataSource(context);
        source.open();
        //List<DataPoint> points = source.getAllDataPoints();

        String selected = context.getResources().getStringArray(R.array.app_list)[(int) spinner.getSelectedItemId()];

        List<DataPoint> points = source.getSelectedActivityDataPoints(selected);
        source.close();

        int size = points.size();
        int i = 0;

        float angerAvg = 0;
        float fearAvg = 0;
        float disgustAvg = 0;
        float joyAvg = 0;
        float sadnessAvg = 0;
        float surpriseAvg = 0;
        float attentionAvg = 0;


        while (i < size) {
            angerAvg += points.get(i).getAnger();
            fearAvg += points.get(i).getFear();
            disgustAvg += points.get(i).getDisgust();
            joyAvg += points.get(i).getJoy();
            sadnessAvg += points.get(i).getSadness();
            surpriseAvg += points.get(i).getSurprise();
            attentionAvg += points.get(i).getAttention();

            i++;

        }

        if (i != 0) {
            angerAvg = angerAvg/ (float) i;
            fearAvg = fearAvg/ (float) i;
            disgustAvg = disgustAvg/ (float) i;
            joyAvg = joyAvg/ (float) i;
            sadnessAvg = sadnessAvg/ (float) i;
            surpriseAvg = surpriseAvg/ (float) i;
            attentionAvg = attentionAvg/ (float) i;
        }

        values.add(new Entry(angerAvg, 0));
        values.add(new Entry(fearAvg, 1));
        values.add(new Entry(disgustAvg, 2));
        values.add(new Entry(joyAvg, 3));
        values.add(new Entry(sadnessAvg, 4));
        values.add(new Entry(surpriseAvg, 5));
        values.add(new Entry(attentionAvg, 6));

        /*try {
            //read from the csv file
            //FileInputStream fis = context.openFileInput("emotions.csv");
            FileInputStream fis = context.openFileInput("emotionz.csv");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            int instances = 0;

            String line;
            float angerAvg = 0;
            float fearAvg = 0;
            float disgustAvg = 0;
            float joyAvg = 0;
            float sadnessAvg = 0;
            float surpriseAvg = 0;

            //we read each comma-separated line in the file and build a line point object
            //the line point is (#instance, joy level)
            try {
                while ((line = reader.readLine()) != null) {
                    String[] broken = line.split(",");
                    angerAvg += Float.parseFloat(broken[0]);
                    fearAvg += Float.parseFloat(broken[1]);
                    disgustAvg += Float.parseFloat(broken[2]);
                    joyAvg += Float.parseFloat(broken[3]);
                    sadnessAvg += Float.parseFloat(broken[4]);
                    surpriseAvg += Float.parseFloat(broken[5]);

                    instances ++;

                }
            } catch(IOException e) {
                e.printStackTrace();
            }

            if (instances != 0) {
                angerAvg = angerAvg/ (float) instances;
                fearAvg = fearAvg/ (float) instances;
                disgustAvg = disgustAvg/ (float) instances;
                joyAvg = joyAvg/ (float) instances;
                sadnessAvg = sadnessAvg/ (float) instances;
                surpriseAvg = surpriseAvg/ (float) instances;
            }

            values.add(new Entry(angerAvg, 0));
            values.add(new Entry(fearAvg, 1));
            values.add(new Entry(disgustAvg, 2));
            values.add(new Entry(joyAvg, 3));
            values.add(new Entry(sadnessAvg, 4));
            values.add(new Entry(surpriseAvg, 5));

            //close readers
            try {
                fis.close();
                isr.close();
                reader.close();
            } catch(IOException a) {
                a.printStackTrace();
            }

        } catch(FileNotFoundException i) {
            i.printStackTrace();
        }*/


        RadarDataSet radarDataSet = new RadarDataSet(values, "Emotions");
        radarDataSet.setColor(Color.RED);

        radarDataSet.setDrawFilled(true);


        ArrayList<String> labels = new ArrayList<String>();

        labels.add("Anger");
        labels.add("Fear");
        labels.add("Disgust");
        labels.add("Joy");
        labels.add("Sadness");
        labels.add("Surprise");
        labels.add("Attention");


        RadarData radarData = new RadarData(labels, radarDataSet);

        return radarData;
    }

    /**
     * Refresh the line chart data and the hexagon data.
     */
    public static void refresh() {
        if (lineChart != null) {

            new Thread() {
                public void run() {
                    LineData lineData = generateLineData();


                    lineChart.setDescription("Instances");

                    lineChart.setData(lineData);
                    lineChart.invalidate();
                }
            }.run();
        }


        if (radarChart != null) {
            new Thread() {
                public void run() {
                    RadarData radarData = generateRadarData();


                    radarChart.setDescription("Rated from 0 to 100");

                    radarChart.setData(radarData);
                    radarChart.invalidate();
                    radarChart.animate();
                }
            }.run();
        }

    }


}
