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

public class DiabetesPrediction extends AppCompatActivity {

    EditText bmiEditText, ageEditText, glucoseEditText;
    Button getPrediction;
    TextView predictionTextView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diabetes_prediction);

        bmiEditText = findViewById(R.id.diabetesBmiEditText);
        ageEditText = findViewById(R.id.diabetesAgeEditText);
        glucoseEditText = findViewById(R.id.diabetesGlucoseEditText);
        getPrediction = findViewById(R.id.getDiabetesPrediction);
        predictionTextView = findViewById(R.id.diabetesPredictionTextView);
        progressBar = findViewById(R.id.diabetesProgressBar);

        getPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bmi = bmiEditText.getText().toString();
                String glucose = glucoseEditText.getText().toString();
                String age = ageEditText.getText().toString();

                Log.d("Button", "Clicked");

                if (bmi.equals("") || glucose.equals("") || age.equals(""))
                {
                    Toast.makeText(DiabetesPrediction.this, "Please fill in all the details", Toast.LENGTH_LONG).show();
                }
                else
                {
                    try
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        String url = getString(R.string.api_url) + "/diabetes/glucose=" +glucose+ "/bmi=" +bmi+ "/age=" +age;
                        GetDiabetesPrediction prediction = new GetDiabetesPrediction();
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

    class GetDiabetesPrediction extends AsyncTask<String, Void, String>
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
                String probability = jsonObject.getString("Probability");
                String output = (prediction == 0) ? "Not Diabetic" : "Diabetic";

                predictionTextView.setText("Prediction: " +output+ " " + probability + "%");
                progressBar.setVisibility(View.GONE);
                saveToDatabase(output, probability);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    void saveToDatabase(String predictionOutput, String probability)
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
                        Toast.makeText(DiabetesPrediction.this, "Saved to database", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(DiabetesPrediction.this, "Error saving to database", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DiabetesPrediction.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("phone", userPhone);
                map.put("type", "Diabetes");
                map.put("prediction", predictionOutput);
                map.put("probability", probability);
                return map;
            }
        };

        queue.add(request);
    }
}