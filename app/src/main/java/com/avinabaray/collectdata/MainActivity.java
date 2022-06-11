package com.avinabaray.collectdata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final int totalTimeInSeconds = 15;

    private TextView accText, gyroText, accValueText, gyroValueText, remText;
    private Button startBtn, pauseBtn, resumeBtn;
    private SensorManager sensorManager;

    private Sensor accelerometerSensor, gyroscopeSensor;
    private SensorData lastAccData = null;
    private SensorData lastGyroData = null;

    private boolean collectData = false;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        mActivity = this;

        initViews();
        initSensors(false);
    }

    private void initSensors(boolean registerListeners) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (checkSensorAvailability(Sensor.TYPE_ACCELEROMETER)) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accText.setText("TRUE");
        } else {
            accelerometerSensor = null;
            Log.wtf("SENSOR_ERROR", "TYPE_ACCELEROMETER not found in device");
            accText.setText("FALSE");
        }

        if (checkSensorAvailability(Sensor.TYPE_GYROSCOPE)) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            gyroText.setText("TRUE");
        } else {
            gyroscopeSensor = null;
            Log.wtf("SENSOR_ERROR", "TYPE_GYROSCOPE not found in device");
            gyroText.setText("FALSE");
        }

        if (registerListeners) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initViews() {
        accText = findViewById(R.id.accText);
        gyroText = findViewById(R.id.gyroText);
        accValueText = findViewById(R.id.accValueText);
        gyroValueText = findViewById(R.id.gyroValueText);
        startBtn = findViewById(R.id.startBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        resumeBtn = findViewById(R.id.resumeBtn);
        remText = findViewById(R.id.remText);

        remText.setText("0/" + totalTimeInSeconds);

//        startBtn.setVisibility(View.VISIBLE);
//        pauseBtn.setVisibility(View.GONE);
//        resumeBtn.setVisibility(View.GONE);

        setBtnVis(true, false, false);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData = true;
                setBtnVis(false, false, true);
                initSensors(true);

                int[] counter = {0};
                CountDownTimer countDownTimer = new CountDownTimer(totalTimeInSeconds * 1000L, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        remText.setText("" + ++counter[0] + "/" + totalTimeInSeconds);
                    }

                    @Override
                    public void onFinish() {
                        collectData = false;
                        generateCSV();
                        sensorManager.unregisterListener(MainActivity.this);
                    }
                };

                countDownTimer.start();

            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData = false;
                setBtnVis(false, true, false);
            }
        });

        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData = true;
                setBtnVis(false, false, true);
            }
        });

    }

    private void setBtnVis(boolean start, boolean resume, boolean pause) {
        // TODO Resume and Pause disabled
        resume = false;
        pause = false;

        if (start) {
            startBtn.setVisibility(View.VISIBLE);
        } else {
            startBtn.setVisibility(View.GONE);
        }

        if (resume) {
            resumeBtn.setVisibility(View.VISIBLE);
        } else {
            resumeBtn.setVisibility(View.GONE);
        }

        if (pause) {
            pauseBtn.setVisibility(View.VISIBLE);
        } else {
            pauseBtn.setVisibility(View.GONE);
        }

    }

    public boolean checkSensorAvailability(int SensorType) {
        boolean isSensor = false;
        Log.d("Sensors Availability: ",
                "Check Sensor Availability: " + (sensorManager.getDefaultSensor(SensorType) != null));
        if (sensorManager.getDefaultSensor(SensorType) != null) {
            isSensor = true;
        }
        return isSensor;
    }

    List<SensorData> accData = new ArrayList<>();
    List<SensorData> gyroData = new ArrayList<>();

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                lastAccData = new SensorData(
                        System.currentTimeMillis() - SystemClock.elapsedRealtime() + (event.timestamp / 1000000L),
                        event.values[0],
                        event.values[1],
                        event.values[2]
                );
                break;

            case Sensor.TYPE_GYROSCOPE:

                lastGyroData = new SensorData(
                        System.currentTimeMillis() - SystemClock.elapsedRealtime() + (event.timestamp / 1000000L),
                        event.values[0],
                        event.values[1],
                        event.values[2]
                );
                break;
        }

        if (collectData)
            newDataReceived();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void newDataReceived() {
        if (lastAccData != null) {
            Log.w("DATA", "" + lastAccData.getTimestamp() + ", " + lastAccData.getX());

//            if (Math.abs(lastAccData.getTimestamp() - lastGyroData.getTimestamp()) < 6000000) {
            accValueText.setText("" + lastAccData.getX() + "\n" + lastAccData.getY() + "\n" + lastAccData.getZ());
            accData.add(lastAccData);
            lastAccData = null;

        }

        if (lastGyroData != null) {
            gyroValueText.setText("" + lastGyroData.getX() + "\n" + lastGyroData.getY() + "\n" + lastGyroData.getZ());
            gyroData.add(lastGyroData);
            lastGyroData = null;
        }
    }

    private float[][] dataToWrite = new float[20][1200];
    private int dataPos = 0;

    private void generateCSV() {
//        remText.setText("" + dataPos + "/20");
        Toast.makeText(mActivity, "CSV Generated", Toast.LENGTH_SHORT).show();
        setBtnVis(true, false, false);

        StringBuilder sbAcc = new StringBuilder();
        sbAcc.append("timestamp,formattedTime,accX,accY,accZ\n");

        for (SensorData sensorData : accData) {
            sbAcc.append(sensorData.getTimestamp()).append(",");
            sbAcc.append(sensorData.getFormattedTime()).append(",");
            sbAcc.append(sensorData.getX()).append(",");
            sbAcc.append(sensorData.getY()).append(",");
            sbAcc.append(sensorData.getZ()).append("\n");
        }

        StringBuilder sbGyro = new StringBuilder();
        sbGyro.append("timestamp,formattedTime,gyroX,gyroY,gyroZ\n");

        for (SensorData sensorData : gyroData) {
            sbGyro.append(sensorData.getTimestamp()).append(",");
            sbGyro.append(sensorData.getFormattedTime()).append(",");
            sbGyro.append(sensorData.getX()).append(",");
            sbGyro.append(sensorData.getY()).append(",");
            sbGyro.append(sensorData.getZ()).append("\n");
        }

        // TODO Uncomment this to generate file
        writeToFile(sbAcc.toString(), "Acc_Data_" + System.currentTimeMillis() + ".csv");
        writeToFile(sbGyro.toString(), "Gyro_Data_" + System.currentTimeMillis() + ".csv");

    }

    public void writeToFile(String data, String fileName) {
        // Get the directory for the user's public pictures directory.
//        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        final File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

//        // Make sure the path directory exists.
//        if(!path.exists())
//        {
//            // Make it, if it doesn't exit
//            path.mkdirs();
//        }

        final File file = new File(path, fileName);

        Log.w("FILE", file.getAbsolutePath());
        // Save your stream, don't forget to flush() it before closing it.
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(mActivity, "Something wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Todo The below code is used to create a download request for the file so that it shows up in the downloads
        try {
            DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(
                    file.getName(),
                    file.getName(),
                    true,
                    "text/csv",
                    file.getAbsolutePath(),
                    file.length(),
                    true
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "Something wrong: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}