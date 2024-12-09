package com.example.vballapp;

import android.os.Bundle;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText homeTeamNameInput, awayTeamNameInput, homeScoreInput, awayScoreInput;
    private TextView homeWinRateText, awayWinRateText, predictionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        homeTeamNameInput = findViewById(R.id.homeTeamNameInput);
        awayTeamNameInput = findViewById(R.id.awayTeamNameInput);
        homeScoreInput = findViewById(R.id.homeScoreInput);
        awayScoreInput = findViewById(R.id.awayScoreInput);
        homeWinRateText = findViewById(R.id.homeWinRateText);
        awayWinRateText = findViewById(R.id.awayWinRateText);
        predictionText = findViewById(R.id.predictionText);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String homeTeamName = homeTeamNameInput.getText().toString();
                String awayTeamName = awayTeamNameInput.getText().toString();
                int homeScore = Integer.parseInt(homeScoreInput.getText().toString());
                int awayScore = Integer.parseInt(awayScoreInput.getText().toString());


                databaseHelper.insertMatchResult(homeTeamName, awayTeamName, homeScore, awayScore);
                updateWinRates(homeTeamName, awayTeamName);

                String prediction = databaseHelper.predictWinner(homeTeamName, awayTeamName);
                predictionText.setText(prediction);
            }
        });
    }
    private void updateWinRates(String homeTeamName, String awayTeamName) {

        float homeWinRate = databaseHelper.calculateWinRate(homeTeamName);
        float awayWinRate = databaseHelper.calculateWinRate(awayTeamName);

        homeWinRateText.setText("Home Team Win Rate: " + homeWinRate);
        awayWinRateText.setText("Away Team Win Rate: " + awayWinRate);
    }
}