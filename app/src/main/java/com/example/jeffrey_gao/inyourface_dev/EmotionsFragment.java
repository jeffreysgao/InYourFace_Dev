package com.example.jeffrey_gao.inyourface_dev;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jinnan on 2/25/17.
 */


public class EmotionsFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.emotions_fragment, container, false);

        LineChart lineChart = (LineChart) view.findViewById(R.id.line_chart);

        //we get a chartData object from the csv file
        LineData lineData = generateLineData();

        Description description = new Description();
        description.setText("Instances");
        lineChart.setDescription(description);

        lineChart.setData(lineData);
        lineChart.invalidate();


        return view;


    }

    public LineData generateLineData() {

        List<Entry> values = new ArrayList<Entry>();

        try {
            //read from the csv file
            FileInputStream fis = getActivity().openFileInput("emotions.csv");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            String line;
            int instances = 1;

            //we read each comma-separated line in the file and build a line point object
            //the line point is (#instance, joy level)
            try {
                while ((line = reader.readLine()) != null) {
                    String[] broken = line.split(",");
                    Entry entry = new Entry(instances, Float.parseFloat(broken[2]));
                    values.add(entry);
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
        }

        //create a line object from the point objects
        LineDataSet lineDataSet = new LineDataSet(values, "Joy");
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setLineWidth(4);


        List<ILineDataSet> lines = new ArrayList<ILineDataSet>();
        lines.add(lineDataSet);

        //now we have the chart data
        LineData lineData = new LineData(lines);

        return lineData;
    }
}
