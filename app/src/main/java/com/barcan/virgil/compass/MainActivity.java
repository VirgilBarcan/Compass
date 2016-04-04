package com.barcan.virgil.compass;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private float[] gravity;
    private float[] geomagnetic;
    private float[] rotation;
    private float[] orientation; //(azimuth, pitch, roll)
    private float[] smoothed;
    private double bearing;

    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private Sensor sensorMagnetic;
    private GeomagneticField geomagneticField;

    private TextView orientationDegreesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orientationDegreesTextView = (TextView) findViewById(R.id.orientation_degrees);

        initVectors();
        initSensors();

        registerToSensors();
    }

    private void initVectors() {
        gravity = new float[3];
        geomagnetic = new float[3];
        rotation = new float[9];
        orientation = new float[3];
        smoothed = new float[3];
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void registerToSensors() {
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorMagnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterFromSensors() {
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();

        registerToSensors();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterFromSensors();
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean accelOrMagnetic = false;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smoothed = LowPassFilter.filter(event.values, gravity);

            for (int i = 0; i < 3; ++i) {
                gravity[i] = smoothed[i];
            }
            accelOrMagnetic = true;
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, geomagnetic);

            for (int i = 0; i < 3; ++i) {
                geomagnetic[i] = smoothed[i];
            }
            accelOrMagnetic = true;
        }

        //get rotation matrix to get gravity and magnetic data
        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);

        //get bearing to target
        SensorManager.getOrientation(rotation, orientation);


        //east degrees of true North
        bearing = orientation[0];

        //convert from radians to degrees
        bearing = Math.toDegrees(bearing);

        //fix difference between true North and magnetic North
        if (null != geomagneticField) {
            bearing += geomagneticField.getDeclination();
        }

        //bearing must be in 0-360
        if (bearing < 0) {
            bearing += 360;
        }

        updateTextView(bearing);
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //TODO
    }

    private void updateTextView(double bearing) {
        int range = (int) (bearing / (360f / 16f));
        String text = "";

        if (range == 15 || range == 0)
            text = "N";
        if (range == 1 || range == 2)
            text = "NE";
        if (range == 3 || range == 4)
            text = "E";
        if (range == 5 || range == 6)
            text = "E";
        if (range == 7 || range == 8)
            text = "E";
        if (range == 9 || range == 10)
            text = "E";
        if (range == 11 || range == 12)
            text = "E";
        if (range == 13 || range == 14)
            text = "E";

        orientationDegreesTextView.setText(String.format("%.4f %s", bearing, text));
    }
}
