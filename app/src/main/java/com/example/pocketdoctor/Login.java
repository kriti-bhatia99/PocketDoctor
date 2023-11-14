package com.example.pocketdoctor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    EditText editText, codeEditText;
    Button getOtpButton;
    String userPhone;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText = findViewById(R.id.loginEditText);
        getOtpButton = findViewById(R.id.getOtpButton);
        codeEditText = findViewById(R.id.loginCodeEditText);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();

        getOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = editText.getText().toString();
                String countryCode = codeEditText.getText().toString();

                if (phone.equals(""))
                    editText.setError("Please enter a valid phone number");
                else if (countryCode.equals("") || countryCode.length() > 3)
                    codeEditText.setError("Please enter a valid country code");
                else
                    verifyPhone(countryCode + phone);
                
            }
        });
    }

    void verifyPhone(final String phone)
    {
        userPhone = phone;
        progressBar.setVisibility(View.VISIBLE);

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    Toast.makeText(Login.this,"Phone number verified", Toast.LENGTH_SHORT).show();
                    checkUser();
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(Login.this,"Error getting OTP", Toast.LENGTH_SHORT).show();
                    Log.d("Verification error", e.toString());
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);

                    String verificationCode = s;
                    final EditText editText = new EditText(Login.this);

                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle("Enter OTP");
                    builder.setView(editText);

                    builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String code = editText.getText().toString();
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, code);
                            signInWithPhoneAuthCredential(credential);
                            checkUser();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            };

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phone)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(Login.this)                 // Activity (for callback binding)
                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }

        private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
        {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = task.getResult().getUser();
                            }
                            else
                            {
                                Toast.makeText(Login.this,"OTP verification failed! Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }


    void checkUser()
    {
        String url = getString(R.string.ip) + "login.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject json = new JSONObject(String.valueOf(response));
                    String result = json.getString("response");

                    if (result.equals("FOUND")) // User already exists
                    {
                        // Saving data to SharedPreferences
                        SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(getString(R.string.pref_id), json.getInt("id"));
                        editor.putString(getString(R.string.pref_phone), userPhone);
                        editor.putString(getString(R.string.pref_name), json.getString("name"));
                        editor.putString(getString(R.string.pref_gender), json.getString("gender"));
                        editor.putInt(getString(R.string.pref_age), json.getInt("age"));
                        editor.apply();

                        // Moving to home screen
                        Intent in = new Intent(Login.this, Home.class);
                        startActivity(in);
                        finish();
                    }
                    else // New user
                    {
                        Intent in = new Intent(Login.this, Register.class);
                        in.putExtra("phone", userPhone);
                        startActivity(in);
                        finish();
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
                Toast.makeText(Login.this, "Error connecting", Toast.LENGTH_LONG).show();
                Log.d("Volley Error", error.toString());
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("phone", userPhone);
                return map;
            }
        };

        queue.add(request);
    }
}