package erlab.ucla.wear_activity_recognition;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Collections;

/**
 * Created by arjun on 11/10/15.
 * mean - done
 * variance - done
 * minmax - done
 * magnit
 * diff
 * cross
 */
public class ActivityRecognition extends Service implements SensorEventListener2 {
    private static final int SAMPLE_RATE = 10; // 10 Hz
    private static final int TIME_WINDOW = 20; // 20 seconds
    private static final int BUF_SIZE    = TIME_WINDOW*SAMPLE_RATE;
    private static float[][] sensor_data = new float [BUF_SIZE][6]; // accel,gyro xyz
    private static long startTime = System.currentTimeMillis();
    private static int i;
    private static SensorManager mSensorManager;
    private static PowerManager.WakeLock mWakeLock = null;
    @Override
    public void onFlushCompleted(Sensor sensor) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER):
                sensor_data[++i][0] = event.values[0];
                sensor_data[i][1]   = event.values[1];
                sensor_data[i][2]   = event.values[2];
      break;

            case (Sensor.TYPE_GYROSCOPE):
                sensor_data[i][3] = event.values[0];
                sensor_data[i][4] = event.values[1];
                sensor_data[i][5] = event.values[2];
                if(i == (BUF_SIZE-1)){
                    setSensors(false);
                    //compute all data;
                    startTime = System.currentTimeMillis();
                    float[] mean = getMean();
                    float[] var  = getVar(mean);
                    float[] minmax = getMinMax();
                    Log.d("DATA_MEAN", mean[0] + "," + mean[1] + "," + mean[2] + "," + mean[3] + "," + mean[4] + "," + mean[5]);
                    Log.d("DATA_VAR", var[0] + "," + var[1] + "," + var[2] + "," + var[3] + "," + var[4] + "," + var[5]);
                    Log.d("DATA_MINMAX", var[0] + "," + var[1] + "," + var[2] + "," + var[3] + "," + var[4] + "," + var[5]+ "," + var[6]+ "," + var[7]+ "," + var[8]+ "," + var[9]+ "," + var[10]+ "," + var[11]);
                    setSensors(true);
                }
                  break;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static float[] getMean(){
        float[] mean = new float[6];
        for(float[] row :sensor_data){
            mean[0] += row[0];
            mean[1] += row[1];
            mean[2] += row[2];
            mean[3] += row[3];
            mean[4] += row[4];
            mean[5] += row[5];
        }
        mean[0] /= BUF_SIZE;
        mean[1] /= BUF_SIZE;
        mean[2] /= BUF_SIZE;
        mean[3] /= BUF_SIZE;
        mean[4] /= BUF_SIZE;
        mean[5] /= BUF_SIZE;
        return mean;
    }

    private static float[] getVar(float[] mean){
        float[] var = new float[6];
        for(float[] row :sensor_data){
            var[0]  += (mean[0]-row[0])*(mean[0]-row[0]);
            var[1]  += (mean[1]-row[1])*(mean[1]-row[1]);
            var[2]  += (mean[2]-row[2])*(mean[2]-row[2]);
            var[3]  += (mean[3]-row[3])*(mean[3]-row[3]);
            var[4]  += (mean[4]-row[4])*(mean[4]-row[4]);
            var[5]  += (mean[5]-row[5])*(mean[5]-row[5]);
        }
        var[0] /= BUF_SIZE;
        var[1] /= BUF_SIZE;
        var[2] /= BUF_SIZE;
        var[3] /= BUF_SIZE;
        var[4] /= BUF_SIZE;
        var[5] /= BUF_SIZE;
        return var;
    }

    private static float[] getMinMax(){
        float[] MinMax = new float[12];
        for(float[] row : sensor_data){
            if(MinMax[0] < row[0]) MinMax[0] = row[0];
            if(MinMax[1] > row[0]) MinMax[1] = row[0];
            if(MinMax[2] < row[1]) MinMax[2] = row[1];
            if(MinMax[3] > row[1]) MinMax[3] = row[1];
            if(MinMax[4] < row[2]) MinMax[4] = row[2];
            if(MinMax[5] > row[2]) MinMax[5] = row[2];
            if(MinMax[6] < row[3]) MinMax[6] = row[3];
            if(MinMax[7] > row[3]) MinMax[7] = row[3];
            if(MinMax[8] < row[4]) MinMax[8] = row[4];
            if(MinMax[9] > row[4]) MinMax[9] = row[4];
            if(MinMax[10] < row[5]) MinMax[10] = row[5];
            if(MinMax[11] > row[5]) MinMax[11] = row[5];
        }
        return MinMax;
    }
    private void setSensors(boolean STATE){
        if(!STATE){
            mSensorManager.unregisterListener(this);
            return;
        }
        // Initializing the sensors
        mSensorManager              = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final Sensor mGyroscope     = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        i = -1;
        // Start recording data
        mSensorManager.registerListener(this, mAccelerometer, (1000000 / SAMPLE_RATE));
        mSensorManager.registerListener(this, mGyroscope, (1000000 / SAMPLE_RATE));
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Prevent the CPU from going to sleep while sensors are scanning.
        if(mWakeLock == null) {
            final PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SHIT");
            mWakeLock.acquire();
        }
        setSensors(true);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Write-out any remaining data (in sensor)
        mSensorManager.unregisterListener(this);
        mSensorManager.flush(this);

        // Release the Wake Lock so CPU can go to sleep.
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
