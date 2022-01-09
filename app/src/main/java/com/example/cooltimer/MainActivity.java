package com.example.cooltimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SeekBar seekBar;
    private TextView textView;
    private Button button;
    private CountDownTimer countDownTimer;
    private boolean isTimerOn;
    private MediaPlayer mediaPlayer;
    private int duration;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        seekBar.setMax(Integer.MAX_VALUE);
        setDurationFromSharedPreferences(sharedPreferences);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int minutes = i / 60;
                int seconds = i % 60;

                String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                textView.setText(time);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void start(View view) {

        if (!isTimerOn) {
            button.setText("STOP");
            seekBar.setEnabled(false);
            isTimerOn = true;

            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000L, 1000) {
                @Override
                public void onTick(long l) {
                    int minutes = (int) (l / 1000 / 60);
                    int seconds = (int) (l / 1000 % 60);

                    String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    textView.setText(time);
                }

                @Override
                public void onFinish() {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if (sharedPreferences.getBoolean("sound", true)) {
                        String melody = sharedPreferences.getString("melody", "bell_sound");

                        switch (melody) {
                            case "bell_sound":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                                mediaPlayer.start();
                                break;
                            case "bip_sound":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                                mediaPlayer.start();
                                break;
                            case "siren_sound":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.siren_sound);
                                mediaPlayer.start();
                                break;
                        }

                    }
                    resetTimer();
                }
            };
            countDownTimer.start();

        } else {
            resetTimer();
        }
    }

    private void updateTimer(long millisUntilFinished) {
        int minutes = (int) millisUntilFinished / 1000 / 60;
        int seconds = (int) millisUntilFinished / 1000 - (minutes * 60);

        String minutesString;
        String secondsString;

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = String.valueOf(minutes);
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = String.valueOf(seconds);
        }
        textView.setText(String.format("%s:%s", minutesString, secondsString));

    }

    private void resetTimer() {
        countDownTimer.cancel();
        button.setText("START");
        seekBar.setEnabled(true);
        isTimerOn = false;
        setDurationFromSharedPreferences(sharedPreferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDurationFromSharedPreferences(SharedPreferences sharedPreferences) {

        try {
            duration = Integer.parseInt(sharedPreferences.getString("duration", "30"));
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Duration should be a number", Toast.LENGTH_SHORT).show();
        }

        long durationInMillis = duration * 1000L;
        updateTimer(durationInMillis);
        seekBar.setProgress(duration);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals("duration")) {
            setDurationFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}