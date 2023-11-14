package com.example.pocketdoctor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class EditProfile extends AppCompatActivity {

    EditText nameEditText, ageEditText, phoneEditText;
    Button updateButton;
    RadioButton maleRadio, femaleRadio, otherRadio;
    ImageView imageView;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.profileNameEditText);
        ageEditText = findViewById(R.id.profileAgeEditText);
        phoneEditText = findViewById(R.id.profilePhoneEditText);
        updateButton = findViewById(R.id.updateProfile);
        maleRadio = findViewById(R.id.editProfileMaleRadio);
        femaleRadio = findViewById(R.id.editProfileFemaleRadio);
        otherRadio = findViewById(R.id.editProfileOtherRadio);
        imageView = findViewById(R.id.profileImageView);

        preferences = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE);
        getDataFromPreferences();
        updateButton.setOnClickListener(updateListener);
    }

    void getDataFromPreferences()
    {
        nameEditText.setText(preferences.getString(getString(R.string.pref_name), ""));
        phoneEditText.setText(preferences.getString(getString(R.string.pref_phone), ""));
        ageEditText.setText(String.valueOf(preferences.getInt(getString(R.string.pref_age), -1)));

        String gender = preferences.getString(getString(R.string.pref_gender), "");

        switch (gender)
        {
            case "M":
                maleRadio.setChecked(true);
                break;
            case "F":
                femaleRadio.setChecked(true);
                imageView.setImageResource(R.drawable.profile_woman);
                break;
            case "O":
                otherRadio.setChecked(true);
                break;
        }

    }

    View.OnClickListener updateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            String name = nameEditText.getText().toString();
            String age = ageEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
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
                Toast.makeText(EditProfile.this, "Please select your gender", Toast.LENGTH_LONG).show();
            else
                updateDetails(name, age, gender, phone);
        }
    };

    void updateDetails(String name, String age, String gender, String phone)
    {
        String id = String.valueOf(preferences.getInt(getString(R.string.pref_id), -1));
        String url = getString(R.string.ip) + "update_profile.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    JSONObject json = new JSONObject(String.valueOf(response));
                    String output = json.getString("response");

                    if (output.equals("OK"))
                    {
                        Toast.makeText(EditProfile.this, "Details updated successfully", Toast.LENGTH_LONG).show();
                        editor.putString(getString(R.string.pref_phone), phone);
                        editor.putString(getString(R.string.pref_name), name);
                        editor.putString(getString(R.string.pref_gender), gender);
                        editor.putInt(getString(R.string.pref_age), Integer.parseInt(age));
                        editor.apply();
                        getDataFromPreferences();
                    }
                    else
                    {
                        Toast.makeText(EditProfile.this, "Error updating details", Toast.LENGTH_LONG).show();
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
                Toast.makeText(EditProfile.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("name", name);
                map.put("phone", phone);
                map.put("gender", gender);
                map.put("age", age);
                return map;
            }
        };

        queue.add(request);
    }

}