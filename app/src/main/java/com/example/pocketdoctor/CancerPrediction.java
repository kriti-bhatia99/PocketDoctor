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

public class CancerPrediction extends AppCompatActivity {

    // 'worst concave points', 'worst perimeter', 'mean concave points', 'worst radius', 'mean perimeter'

    EditText wcpEditText, wpEditText, mcpEditText, wrEditText, mpEditText;
    Button getPrediction;
    TextView predictionTextView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancer_prediction);

        wcpEditText = findViewById(R.id.cancerWCPEditText);
        wpEditText = findViewById(R.id.cancerWPEditText);
        mcpEditText = findViewById(R.id.cancerMCPEditText);
        wrEditText = findViewById(R.id.cancerWREditText);
        mpEditText = findViewById(R.id.cancerMPEditText);
        getPrediction = findViewById(R.id.getCancerPrediction);
        predictionTextView = findViewById(R.id.cancerPredictionTextView);
        progressBar = findViewById(R.id.cancerProgressBar);

        getPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String worstConcavePoints = wcpEditText.getText().toString();
                String worstPerimeter = wpEditText.getText().toString();
                String meanConcavePoints = mcpEditText.getText().toString();
                String worstRadius = wrEditText.getText().toString();
                String meanPerimeter = mpEditText.getText().toString();

                Log.d("Button", "Clicked");

                if (worstConcavePoints.equals("") || worstPerimeter.equals("") ||
                        meanConcavePoints.equals("") || worstRadius.equals("") || meanPerimeter.equals(""))
                {
                    Toast.makeText(CancerPrediction.this, "Please fill in all the details", Toast.LENGTH_LONG).show();
                }
                else
                {
                    try
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        String url = getString(R.string.api_url) + "/cancer/wcp=" +worstConcavePoints+
                            "/wp=" +worstPerimeter+ "/mcp=" +meanConcavePoints+ "/wr=" +worstRadius+ "/mp=" +meanPerimeter ;
                        GetCancerPrediction prediction = new GetCancerPrediction();
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

    class GetCancerPrediction extends AsyncTask<String, Void, String>
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
                String output = (prediction == 0) ? "Malignant" : "Benign";

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
                        Toast.makeText(CancerPrediction.this, "Saved to database", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(CancerPrediction.this, "Error saving to database", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CancerPrediction.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("phone", userPhone);
                map.put("type", "Cancer");
                map.put("prediction", predictionOutput);
                map.put("probability", probability);
                return map;
            }
        };

        queue.add(request);
    }
}