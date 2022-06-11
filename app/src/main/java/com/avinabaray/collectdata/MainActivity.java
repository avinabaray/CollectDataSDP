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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private TextView accText, gyroText, accValueText, gyroValueText, remText;
    private Button startBtn, pauseBtn, resumeBtn;
    private SensorManager sensorManager;

    private Sensor accelerometerSensor, gyroscopeSensor;
    private float[] lastAccData = new float[]{Integer.MAX_VALUE, 0.0f, 0.0f};
    private long lastAccTime;
    private float[] lastGyroData = new float[]{Integer.MAX_VALUE, 0.0f, 0.0f};
    private long lastGyroTime;

    private boolean collectData = false;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        mActivity = this;

        initViews();
        initSensors();
    }

    private void initSensors() {
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

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
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

//        startBtn.setVisibility(View.VISIBLE);
//        pauseBtn.setVisibility(View.GONE);
//        resumeBtn.setVisibility(View.GONE);

        setBtnVis(true, false, false);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData = true;
                setBtnVis(false, false, true);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                lastAccData[0] = modulateAccData(event.values[0]);
                lastAccData[1] = modulateAccData(event.values[1]);
                lastAccData[2] = modulateAccData(event.values[2]);

                lastAccTime = event.timestamp;
//                newDataReceived();

                break;
            case Sensor.TYPE_GYROSCOPE:

                lastGyroData[0] = modulateGyroData(event.values[0]);
                lastGyroData[1] = modulateGyroData(event.values[1]);
                lastGyroData[2] = modulateGyroData(event.values[2]);

                lastGyroTime = event.timestamp;
//                newDataReceived();

                break;

        }

        if (collectData)
            newDataReceived();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float modulateAccData(float rawData) {

        float temp = (int) (255 * (rawData + 16)) / 32.0f;
        float ans = (float) (temp / 127.5) - 1;

        return temp;
    }

    private float modulateGyroData(float rawData) {

        float temp = (255 * (rawData + 34)) / 68.0f;
        float ans = (float) (temp / 127.5) - 1;

        return temp;
    }

    private float[][][][] data = new float[1][20][20][3];
    private int x = 0;
    private int y = 0;

    private void newDataReceived() {
        Log.w("DATA", "A: " + lastAccTime + ", G: " + lastGyroTime + ", Diff: " + (lastAccTime - lastGyroTime));
        if (lastAccData[0] != Integer.MAX_VALUE && lastGyroData[0] != Integer.MAX_VALUE) {
            if (Math.abs(lastAccTime - lastGyroTime) < 6000000) {
//            if (true) {
                Log.wtf("DATA", "A: " + lastAccTime + ", G: " + lastGyroTime + ", Diff: " + (lastAccTime - lastGyroTime));

                int currRow = (x++) / 20;
                int currCol = (y++) % 20;

                if (currRow == 10) {
                    // Do prediction here
//                    predictMotionAndToast();

                    float[][][][] copyData = data.clone();
                    myCSV(copyData);
//                    generateCsv();

                    Log.w("PREDICTION", "Data filled x:" + x + ", " + y);
                    x = 0;
                    y = 0;
                    return;
                }

                accValueText.setText("" + lastAccData[0] + "\n" + lastAccData[1] + "\n" + lastAccData[2]);
                gyroValueText.setText("" + lastGyroData[0] + "\n" + lastGyroData[1] + "\n" + lastGyroData[2]);

                data[0][currRow][currCol][0] = lastAccData[0];
                data[0][currRow][currCol][1] = lastAccData[1];
                data[0][currRow][currCol][2] = lastAccData[2];

                data[0][currRow + 10][currCol][0] = lastGyroData[0];
                data[0][currRow + 10][currCol][1] = lastGyroData[1];
                data[0][currRow + 10][currCol][2] = lastGyroData[2];

                lastAccData[0] = Integer.MAX_VALUE;
                lastGyroData[0] = Integer.MAX_VALUE;
            }
        }
    }

    private float[][] dataToWrite = new float[20][1200];
    private int dataPos = 0;

    @SuppressWarnings("PointlessArithmeticExpression")
    private void myCSV(float[][][][] copyData) {
        float[] single = new float[1200];

        int locX = 0;
        int locY = 0;

        for (int i = 0; i < 600; i += 3) {
            int currRow = (locX++) / 20;
            int currCol = (locY++) % 20;

            // Acc data
            single[i + 0] = copyData[0][currRow][currCol][0];
            single[i + 1] = copyData[0][currRow][currCol][1];
            single[i + 2] = copyData[0][currRow][currCol][2];

            // Gyro data
            single[i + 600 + 0] = copyData[0][currRow + 10][currCol][0];
            single[i + 600 + 1] = copyData[0][currRow + 10][currCol][1];
            single[i + 600 + 2] = copyData[0][currRow + 10][currCol][2];

        }

        dataToWrite[dataPos++] = single;
        generateCSV();

    }

    private void generateCSV() {
        remText.setText("" + dataPos + "/20");
        if (dataPos == 20) {
            Toast.makeText(mActivity, "CSV Generated", Toast.LENGTH_SHORT).show();
            collectData = false;
            setBtnVis(true, false, false);
            data = new float[1][20][20][3];

            dataPos = 0;

            float[][] currDataToWrite = dataToWrite.clone();

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 1201; i++) {
                if (i == 0)
                    sb.append("label,");
                else {
                    sb.append(i);
                    if (i != 1200)
                        sb.append(",");
                }
            }
            sb.append("\n");

            for (int i = 0; i < 20; i++) {
                sb.append("1,");
                for (int j = 0; j < 1200; j++) {
                    sb.append(currDataToWrite[i][j]);
                    if (j != 1199) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
            }

            // TODO Uncomment this to generate file
//            writeToFile(sb.toString(), "timestamp_corrected" + System.currentTimeMillis() + ".csv");
        }
    }


//    private void generateCsv() {
//
//        StringBuilder csvOut = new StringBuilder();
//
//        csvOut.append("label,");
//        for (int i = 1; i <= 1200; i++) {
//            csvOut.append(i);
//            if (i != 1200)
//                csvOut.append(",");
//        }
//        int count = 0;
//        for (int i = 1; i <= 30; i++) {
//            csvOut.append("\n");
//            csvOut.append("1,");
//            for (int j = 0; j < 1200; j++) {
//                csvOut.append(data[count][j]);
//                if (j != 1199) {
//                    csvOut.append(",");
//                }
//            }
//            count++;
//        }
//
//
//        writeToFile(csvOut.toString(), "1_running_data" + System.currentTimeMillis() + ".csv");
//
//
//    }

    public void writeToFile(String data, String fileName) {
        // Get the directory for the user's public pictures directory.
//        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final File path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

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