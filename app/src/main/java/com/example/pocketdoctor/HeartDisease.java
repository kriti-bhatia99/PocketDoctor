package com.example.pocketdoctor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HeartDisease extends AppCompatActivity {

    EditText ageEditText, glucoseEditText;
    Button getPrediction;
    RadioButton hypertensionYes, hyperTensionNo, heartDiseaseYes, heartDiseaseNo;
    TextView predictionTextView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_disease);

        ageEditText = findViewById(R.id.heartAgeEditText);
        glucoseEditText = findViewById(R.id.heartGlucoseEditText);
        hypertensionYes = findViewById(R.id.hypertensionYesRadio);
        hyperTensionNo = findViewById(R.id.hypertensionNoRadio);
        heartDiseaseYes = findViewById(R.id.heartDiseaseYesRadio);
        heartDiseaseNo = findViewById(R.id.heartDiseaseNoRadio);
        getPrediction = findViewById(R.id.getHeartPrediction);
        predictionTextView = findViewById(R.id.heartPredictionTextView);
        progressBar = findViewById(R.id.heartDiseaseProgressBar);

        getPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String glucose = glucoseEditText.getText().toString();
                String age = ageEditText.getText().toString();
                String hyperTension = "", heartDisease = "";

                Log.d("Button", "Clicked");

                if (hypertensionYes.isChecked())
                    hyperTension = "1";
                else if (hyperTensionNo.isChecked())
                    hyperTension = "0";

                if (heartDiseaseYes.isChecked())
                    heartDisease = "1";
                else if (heartDiseaseNo.isChecked())
                    heartDisease = "0";

                if (glucose.equals("") || age.equals("") || hyperTension.equals("") || heartDisease.equals(""))
                {
                    Toast.makeText(HeartDisease.this, "Please fill in all the details", Toast.LENGTH_LONG).show();
                }
                else
                {
                    try
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        String url = getString(R.string.api_url) + "/stroke/hypertension=" +hyperTension+
                                "/avg_glucose=" +glucose+ "/heart_disease=" +heartDisease+ "/age=" +age;
                        GetStrokePrediction prediction = new GetStrokePrediction();
                        prediction.execute(url).get();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    class GetStrokePrediction extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            try
            {
                String result = ""; URL url;
                HttpURLConnection connection;

                url = new URL(urls[0]);
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

                return result;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try
            {
                JSONObject jsonObject = new JSONObject(s);
                Log.d("Output", s);
                int prediction = jsonObject.getInt("Response");
                String output = (prediction == 0) ? "No Heart Stroke" : "Chances of Heart Stroke";

                predictionTextView.setText("Prediction: " +output);
                progressBar.setVisibility(View.GONE);
                saveToDatabase(output);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    void saveToDatabase(String predictionOutput)
    {
        String userPhone = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE)
                .getString(getString(R.string.pref_phone), "");

        String url = getString(R.string.ip) + "save_prediction.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    JSONObject json = new JSONObject(String.valueOf(response));
                    String output = json.getString("response");

                    if (output.equals("OK"))
                        Toast.makeText(HeartDisease.this, "Saved to database", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(HeartDisease.this, "Error saving to database", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HeartDisease.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("phone", userPhone);
                map.put("type", "Heart Stroke");
                map.put("prediction", predictionOutput);
                return map;
            }
        };

        queue.add(request);
    }
}