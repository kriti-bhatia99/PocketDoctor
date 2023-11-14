package com.example.pocketdoctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Home extends AppCompatActivity {

    CardView heartRateCardView, oxygenCardView, cancerCardView, tumourCardView, diabetesCardView, heartDiseaseCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        heartRateCardView = findViewById(R.id.heartRateCardView);
        oxygenCardView = findViewById(R.id.oxygenCardView);
        cancerCardView = findViewById(R.id.cancerCardView);
        tumourCardView = findViewById(R.id.brainCardView);
        diabetesCardView = findViewById(R.id.diabetesCardView);
        heartDiseaseCardView = findViewById(R.id.heartDiseaseCardView);

        heartRateCardView.setOnClickListener(cardViewListener);
        oxygenCardView.setOnClickListener(cardViewListener);
        cancerCardView.setOnClickListener(cardViewListener);
        tumourCardView.setOnClickListener(cardViewListener);
        diabetesCardView.setOnClickListener(cardViewListener);
        heartDiseaseCardView.setOnClickListener(cardViewListener);
    }

    View.OnClickListener cardViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            Class targetClass = null;

            if (id == R.id.heartRateCardView)
                targetClass = HeartRateSensor.class;
            else if (id == R.id.oxygenCardView)
                targetClass = OxygenSensor.class;
            else if (id == R.id.cancerCardView)
                targetClass = CancerPrediction.class;
            else if (id == R.id.brainCardView)
                targetClass = BrainTumourPrediction.class;
            else if (id == R.id.diabetesCardView)
                targetClass = DiabetesPrediction.class;
            else if (id == R.id.heartDiseaseCardView)
                targetClass = HeartDisease.class;

            Intent in = new Intent(Home.this, targetClass);
            startActivity(in);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuEditProfile)
        {
            Intent in = new Intent(Home.this, EditProfile.class);
            startActivity(in);
        }
        else if (id == R.id.menuLogOut)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Logging out");
            builder.setMessage("Are you sure you wish to log out?");
            builder.setIcon(R.drawable.healthcare);

            builder.setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_title), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear().apply();

                    Intent in = new Intent(Home.this, Login.class);
                    startActivity(in);
                    finish();
                }
            });

            builder.setNegativeButton("Cancel", null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return true;
    }
}