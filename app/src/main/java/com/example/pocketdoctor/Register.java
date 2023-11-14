package com.example.pocketdoctor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    Button registerButton;
    EditText nameEditText, ageEditText;
    RadioButton maleRadio, femaleRadio, otherRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerButton = findViewById(R.id.registerButton);
        nameEditText = findViewById(R.id.registerNameEditText);
        ageEditText = findViewById(R.id.registerAgeEditText);
        maleRadio = findViewById(R.id.registerRadioMale);
        femaleRadio = findViewById(R.id.registerRadioFemale);
        otherRadio = findViewById(R.id.registerRadioOther);

        String phone = getIntent().getStringExtra("phone");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();
                String age = ageEditText.getText().toString();
                String gender = "";

                if (maleRadio.isChecked())
                    gender = "M";
                else if (femaleRadio.isChecked())
                    gender = "F";
                else if (otherRadio.isChecked())
                    gender = "O";

                if (name.equals(""))
                    nameEditText.setError("Please enter a valid name");
                else if (age.equals("") || Integer.parseInt(age) < 0)
                    ageEditText.setError("Please enter a valid age");
                else if (gender.equals(""))
                    Toast.makeText(Register.this, "Please select your gender", Toast.LENGTH_LONG).show();
                else
                    registerUser(name, age, gender, phone);

            }
        });
    }

    void registerUser(String name, String age, String gender, String phone)
    {
        String url = getString(R.string.ip) + "register.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    JSONObject json = new JSONObject(String.valueOf(response));
                    Log.d("Response", response);
                    String result = json.getString("response");

                    if (result.equals("OK"))
                    {
                        // Saving data to SharedPreferences
                        SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(getString(R.string.pref_id), json.getInt("u_id"));
                        editor.putString(getString(R.string.pref_phone), phone);
                        editor.putString(getString(R.string.pref_name), name);
                        editor.putString(getString(R.string.pref_gender), gender);
                        editor.putInt(getString(R.string.pref_age), Integer.parseInt(age));
                        editor.apply();

                        // Moving to home screen
                        Intent in = new Intent(Register.this, Home.class);
                        startActivity(in);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(Register.this, "Error registering", Toast.LENGTH_LONG).show();
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Register.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", name);
                map.put("age", age);
                map.put("gender", gender);
                map.put("phone", phone);
                return map;
            }
        };

        queue.add(request);
    }
}