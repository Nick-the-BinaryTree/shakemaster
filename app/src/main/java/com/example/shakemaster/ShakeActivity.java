package com.example.shakemaster;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ShakeActivity extends AppCompatActivity {

    final Handler uiHandler = new Handler();

    private ConstraintLayout layout;
    private TextView scoreView;

    private int goColor = Color.parseColor("#00E676");
    private int stopColor = Color.parseColor("#FF1744");

    private Random rand = new Random();
    private Timer timer = new Timer();
    private TimerTask timerTask;

    private final Runnable toggleBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            shouldShake = !shouldShake;
            layout.setBackgroundColor(shouldShake ? goColor : stopColor);
            scheduleBackgroundChange();
        }
    };

    private boolean shouldShake = true;
    private int score = 0;

    // Shake detection credits: https://stackoverflow.com/questions/2317428/how-to-refresh-app-upon-shaking-the-device
    private SensorManager mSensorManager;
    private final int SHAKE_THRESHOLD = 12;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));

            float delta = mAccelCurrent - mAccelLast;

            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > SHAKE_THRESHOLD) {
                score += shouldShake ? 1 : -1;
                scoreView.setText(String.valueOf(score));
            }
        }
    };

    private void scheduleBackgroundChange() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() { toggleBackground(); }
        };
        timer.schedule(timerTask, rand.nextInt(10000) + 2000);
    }

    private void toggleBackground() {
        uiHandler.post(toggleBackgroundRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        layout = findViewById(R.id.layout);
        scoreView = findViewById(R.id.score);

        scheduleBackgroundChange();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    protected void onPause() {
        timer.cancel();
        timerTask.cancel();
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleBackgroundChange();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }
}
