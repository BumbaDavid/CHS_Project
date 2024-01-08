package com.example.chs_project.ui.gyroscope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GyroscopeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    SensorEventListener gyroscopeSensorListener;
    public GyroscopeViewModel() {
        mText = new MutableLiveData<>();
        initializeGyroscopeListener();
    }


    private void initializeGyroscopeListener() {
         gyroscopeSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                mText.postValue("Gyroscope data - X: " + x + ", Y: " + y + ", Z: " + z);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    public void startListeningToSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(gyroscopeSensorListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListeningToSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(gyroscopeSensorListener);
        }
    }
    public LiveData<String> getText() {
        return mText;
    }
}