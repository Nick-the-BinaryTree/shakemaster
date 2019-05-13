package com.example.shakemaster;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShakeActivity extends AppCompatActivity {

    private ConstraintLayout layout;
    private TextView scoreView;

    private int goColor = Color.parseColor("#00E676");
    private int stopColor = Color.parseColor("#FF1744");


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

    private void toggleBackground() {
        shouldShake = !shouldShake;
        layout.setBackgroundColor(shouldShake ? goColor : stopColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        layout = findViewById(R.id.layout);
        scoreView = findViewById(R.id.score);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }
}
