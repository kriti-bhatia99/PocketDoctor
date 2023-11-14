package com.example.pocketdoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HeartRateSensor extends AppCompatActivity {

    LineChart lineChart;
    ArrayList<Entry> heartRateData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_sensor);

        lineChart = findViewById(R.id.heartRateChart);

        try
        {
            String url = getString(R.string.thingspeak_api_url);
            GetHeartData getHeartData = new GetHeartData ();
            heartRateData = getHeartData.execute(url).get();

            LineDataSet dataSet = new LineDataSet(heartRateData, "Heart Rate");
            dataSet.setLineWidth(3f);
            dataSet.setColor(Color.RED);
            dataSet.setValueTextSize(0f);
            dataSet.setCircleColor(Color.RED);
            dataSet.setCircleRadius(5f);
            dataSet.setCircleHoleColor(Color.RED);

            ArrayList<ILineDataSet> iLineDataSet = new ArrayList<>(1);
            iLineDataSet.add(dataSet);
            LineData lineData = new LineData(iLineDataSet);
            lineChart.setData(lineData);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    class GetHeartData extends AsyncTask<String, Void, ArrayList<Entry>>
    {
        @Override
        protected ArrayList<Entry> doInBackground(String... strings)
        {
            try
            {
                String result = ""; URL url;
                HttpURLConnection connection;

                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();

                while (data != -1)
                {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                JSONObject jsonObject = new JSONObject(result);
                JSONArray heartRateArray = jsonObject.getJSONArray("feeds");
                int size = heartRateArray.length();
                ArrayList<Entry> outputList = new ArrayList<>(size);

                for (int i = 0; i < size; i++)
                {
                    JSONObject dataJson = heartRateArray.getJSONObject(i);
                    String heartRate = dataJson.getString("field2");
                    outputList.add(new Entry(i, Float.parseFloat(heartRate)));
                }

                return outputList;

            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }
}