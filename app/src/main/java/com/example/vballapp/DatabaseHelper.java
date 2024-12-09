package com.example.vballapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Volleyball.db";
    private static final String TABLE_MATCHES = "Matches";
    private static final String TABLE_TEAMS = "Teams";

    private static final String MATCH_ID = "match_id";
    private static final String HOME_NAME = "home_name";
    private static final String AWAY_NAME = "away_name";
    private static final String HOME_SCORE = "home_score";
    private static final String AWAY_SCORE = "away_score";

    private static final String TEAM_NAME = "team_name";
    private static final String MATCHES_PLAYED = "matches_played";
    private static final String WINS = "wins";
    private static final String LOSSES = "losses";
    private static final String AVERAGE_SET_SCORE = "average_set_score";

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_MATCHES_TABLE = "CREATE TABLE " + TABLE_MATCHES + " ("
                + MATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HOME_NAME + " TEXT, "
                + AWAY_NAME + " TEXT, "
                + HOME_SCORE + " INTEGER, "
                + AWAY_SCORE + " INTEGER)";
        db.execSQL(CREATE_MATCHES_TABLE);

        String CREATE_TABLE_TEAMS = "CREATE TABLE " + TABLE_TEAMS + " ("
                + TEAM_NAME + " TEXT PRIMARY KEY, "
                + MATCHES_PLAYED + " INTEGER, "
                + WINS + " INTEGER, "
                + LOSSES + " INTEGER, "
                + AVERAGE_SET_SCORE + " REAL)";
        db.execSQL(CREATE_TABLE_TEAMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertMatchResult(String homeTeamName, String awayTeamName, int homeScore, int awayScore){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues matchValues = new ContentValues();

        matchValues.put(HOME_NAME, homeTeamName);
        matchValues.put(AWAY_NAME, awayTeamName);
        matchValues.put(HOME_SCORE, homeScore);
        matchValues.put(AWAY_SCORE, awayScore);

        db.insert(TABLE_MATCHES, null, matchValues);

        updateTeamStats(homeTeamName, homeScore,  homeScore > awayScore);
        updateTeamStats(awayTeamName, awayScore, awayScore > homeScore);
    }

    public void updateTeamStats(String teamName, int score, boolean won) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TEAMS + " WHERE " + TEAM_NAME + " = ?", new String[]{teamName});
        if (cursor.moveToFirst()) {
            int matchesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(MATCHES_PLAYED));
            int wins = cursor.getInt(cursor.getColumnIndexOrThrow(WINS));
            int losses = cursor.getInt(cursor.getColumnIndexOrThrow(LOSSES));
            float averageSetScore = cursor.getFloat(cursor.getColumnIndexOrThrow(AVERAGE_SET_SCORE));

            matchesPlayed++;
            if (won) {
                wins++;
            } else {
                losses++;
            }
            averageSetScore = (averageSetScore * (matchesPlayed - 1) + score) / matchesPlayed;

            ContentValues values = new ContentValues();
            values.put(MATCHES_PLAYED, matchesPlayed);
            values.put(WINS, wins);
            values.put(LOSSES, losses);
            values.put(AVERAGE_SET_SCORE, averageSetScore);

            db.update(TABLE_TEAMS, values, TEAM_NAME + " = ?", new String[]{teamName});
        }
        cursor.close();
    }

    public float calculateWinRate(String teamName) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TEAMS + " WHERE " + TEAM_NAME + " = ?", new String[]{teamName});

        float winRate = 0;
        if (cursor.moveToFirst()) {
            int wins = cursor.getInt(cursor.getColumnIndexOrThrow(WINS));
            int matchesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(MATCHES_PLAYED));

            if (matchesPlayed > 0) {
                winRate = (float) wins / matchesPlayed;
            }
        }
        cursor.close();
        return winRate;
    }

    public String predictWinner(String homeTeamName, String awayTeamName) {

        float homeWinRate = calculateWinRate(homeTeamName);
        float awayWinRate = calculateWinRate(awayTeamName);

        float homePredictionScore = homeWinRate / (homeWinRate + awayWinRate);
        float awayPredictionScore = awayWinRate / (homeWinRate + awayWinRate);

        if (homePredictionScore > awayPredictionScore) {
            return "Home Team Predicted to Win";
        } else {
            return "Away Team Predicted to Win";
        }
    }
}
