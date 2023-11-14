package com.example.pocketdoctor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pocketdoctor.ml.Model;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class BrainTumourPrediction extends AppCompatActivity {

    ImageView imageView;
    Button uploadScanButton, getPredictionButton;
    TextView predictionTextView;
    Bitmap mriScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brain_tumour_prediction);

        imageView = findViewById(R.id.brainMriImageView);
        uploadScanButton = findViewById(R.id.brainUploadScan);
        getPredictionButton = findViewById(R.id.brainGetPrediction);
        predictionTextView = findViewById(R.id.mriPredictionTextView);

        uploadScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(BrainTumourPrediction.this).galleryOnly().cropSquare().start();
            }
        });

        getPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPrediction();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        imageView.setImageURI(uri);

        try
        {
            mriScan = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    void getPrediction()
    {
        if (mriScan == null)
        {
            Toast.makeText(this, "Please upload a scan first", Toast.LENGTH_LONG).show();
        }
        else
        {
            try
            {
                mriScan = Bitmap.createScaledBitmap(mriScan, 128, 128, true);
                Model model = Model.newInstance(getApplicationContext());

                TensorBuffer inputFeatures = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32);
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(mriScan);

                ByteBuffer byteBuffer = tensorImage.getBuffer();
                inputFeatures.loadBuffer(byteBuffer);

                Model.Outputs outputs = model.process(inputFeatures);
                TensorBuffer outputFeatures = outputs.getOutputFeature0AsTensorBuffer();
                model.close();

                int maxIndex = 0;
                String predictionOutput = "";

                for (int i = 0; i < outputFeatures.getFloatArray().length; i++)
                {
                    if (outputFeatures.getFloatArray()[i] > outputFeatures.getFloatArray()[maxIndex])
                        maxIndex = i;
                }

                double maxProbability = outputFeatures.getFloatArray()[maxIndex] * 100;
                DecimalFormat df = new DecimalFormat("##.00");
                String probability = df.format(maxProbability);

                switch (maxIndex)
                {
                    case 0:
                        predictionOutput = "No Tumour";
                        break;

                    case 1:
                        predictionOutput = "Glioma Tumour";
                        break;

                    case 2:
                        predictionOutput = "Meningioma Tumour";
                        break;

                    case 3:
                        predictionOutput = "Pituitary Tumour";
                }

                predictionTextView.setText("Prediction: " + predictionOutput + " " + probability + "%");
                saveToDatabase(predictionOutput, probability);

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
                        Toast.makeText(BrainTumourPrediction.this, "Saved to database", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(BrainTumourPrediction.this, "Error saving to database", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BrainTumourPrediction.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("phone", userPhone);
                map.put("type", "Brain Tumour");
                map.put("prediction", predictionOutput);
                map.put("probability", probability);
                return map;
            }
        };

        queue.add(request);
    }
}