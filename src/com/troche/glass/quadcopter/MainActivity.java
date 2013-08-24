/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.troche.glass.quadcopter;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This is the main Activity that displays options to connect and send sensor data
 */
public class MainActivity extends Activity implements
        SensorEventListener, TextToSpeech.OnInitListener {

    // Debugging
    private static final String TAG = "QuadcopterCommander";
    private static final boolean D = true;

    // Sensor data
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Float mInitialHeading;
    private float[] mRotationMatrix;
    private float[] mOrientation;

    // Text to Speech
    private TextToSpeech mSpeech;

    // Sensor constants
    private static final int HEADING_THRESHOLD_POS = 15;
    private static final int HEADING_THRESHOLD_NEG = -15;
    private static final int PITCH_THRESHOLD_POS = 15;
    private static final int PITCH_THRESHOLD_NEG = -15;
    private static final int ROLL_THRESHOLD_POS = 15;
    private static final int ROLL_THRESHOLD_NEG = -15;

    // Message types sent from the BluetoothConnectionService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothConnectionService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mTextSensorData;
    private TextView mTextOutput;
    private TextView mTextInput;
    private ToggleButton mTrackingToggle;
    private ToggleButton mTakeoffToggle;
    private ToggleButton mElevationToggle;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the Bluetooth service
    private BluetoothConnectionService mBluetoothService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);

        // Do not allow to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Toggle Buttons
        mTakeoffToggle = (ToggleButton) findViewById(R.id.takeoff_toggle);
        mTrackingToggle = (ToggleButton) findViewById(R.id.tracking_toggle);
        mElevationToggle = (ToggleButton) findViewById(R.id.elevation_toggle);

        // Sensor init
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mRotationMatrix = new float[16];
        mOrientation = new float[3];

        // Text to Speech init
        mSpeech = new TextToSpeech(this, this);

        // Initialize text views
        mTextSensorData = (TextView) findViewById(R.id.text_sensor_data);
        mTextOutput = (TextView) findViewById(R.id.text_output);
        mTextInput = (TextView) findViewById(R.id.text_input);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupBluetooth() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the Bluetooth connection
        } else {
            if (mBluetoothService == null) setupBluetooth();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Start tracking sensor data
        startSensorTracking();
        mTrackingToggle.setFocusable(false);

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothConnectionService.STATE_NONE) {
                // Start the Bluetooth service
                mBluetoothService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        stopSensorTracking();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sendMessage("Quit\n");
        speak(R.string.voice_bye);
        mSpeech.shutdown();

        // Stop the Bluetooth service
        if (mBluetoothService != null) mBluetoothService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void setupBluetooth() {
        Log.d(TAG, "setupBluetooth()");

        // Initialize the BluetoothConnectionService to perform bluetooth connections
        mBluetoothService = new BluetoothConnectionService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // The Handler that gets information back from the BluetoothConnectionService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectionService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mTextInput.setText("");
                    mTakeoffToggle.setChecked(false);
                    mTrackingToggle.setChecked(true);
                    invalidateOptionsMenu();
                    break;
                case BluetoothConnectionService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothConnectionService.STATE_WAITING:
                case BluetoothConnectionService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                // mTextOutput.setText(writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mTextInput.setText(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
                break;
        }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When BluetoothDevicePicker returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a Bluetooth connection
                    setupBluetooth();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean isSecure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(BluetoothDevicePicker.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, isSecure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // If already Bluetooth connected, do not show menu to connect
        if (mBluetoothService.getState() == BluetoothConnectionService.STATE_CONNECTED)
            return false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent;
        switch (item.getItemId()) {
            case R.id.insecure_connect_scan:
                // Launch the BluetoothDevicePicker to see devices and do scan
                serverIntent = new Intent(this, BluetoothDevicePicker.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
        }
        return false;
    }

    /**
     * Sends a message through Bluetooth
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        mTextOutput.setText(message);

        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothConnectionService to write
            byte[] send = (message + "\n").getBytes();
            mBluetoothService.write(send);

            // Reset out string buffer to zero
            mOutStringBuffer.setLength(0);
        }
    }

    public void onTakeoffToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            sendMessage("Takeoff");
            speak(R.string.voice_takeoff);

        } else {
            sendMessage("Land");
            speak(R.string.voice_land);
        }
    }

    public void onElevationToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        speak(on ? R.string.voice_elevation_on : R.string.voice_elevation_off);
    }

    private void speak(int voiceCommandId){
        mSpeech.speak(getString(voiceCommandId), TextToSpeech.QUEUE_FLUSH, null);
    }


    public void onSensorTrackingToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            startSensorTracking();
        } else {
            stopSensorTracking();
        }
    }

    private void startSensorTracking(){
        // Start listening to sensor data
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopSensorTracking(){
        // Stop listening to sensor data
        mSensorManager.unregisterListener(this);

        // Reset initial heading
        mInitialHeading = null;
    }

    @Override
    public void onInit(int status) {
        // Called when the text-to-speech engine is initialized. Nothing to do here.
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Called when sensor accuracy changes. Nothing to do here.
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

        String sensorData;
        float heading, pitch, roll;
        boolean commandSent = true;

        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
        SensorManager.remapCoordinateSystem(mRotationMatrix,
                SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, mOrientation);

        toDegrees(mOrientation);

        if (mInitialHeading == null) {mInitialHeading = new Float(mOrientation[0]);}
        heading = mOrientation[0];
        pitch = -mOrientation[1];
        roll = mOrientation[2];

        sensorData = String.format("Pitch: %+03.0f  Roll: %+03.0f  Heading: %+03.0f",
                pitch, roll, heading);

        mTextSensorData.setText(sensorData);

        if (triggerCommand(pitch, PITCH_THRESHOLD_POS)){
            if (mElevationToggle.isChecked()){
                sendMessage("Up");
            }
            else {
                sendMessage("Backward");
            }
        }
        else if (triggerCommand(pitch, PITCH_THRESHOLD_NEG)){
            if (mElevationToggle.isChecked()){
                sendMessage("Down");
            }
            else {
                sendMessage("Forward");
            }
        }
        else{
            commandSent = false;
        }

        if (triggerCommand(roll, ROLL_THRESHOLD_POS)){
            sendMessage("Right");
        }
        else if (triggerCommand(roll, ROLL_THRESHOLD_NEG)){
            sendMessage("Left");
        }
        else if (!commandSent){
            sendMessage("None");
        }
    }

    /**
     * Determines if a command should be triggered depending on the sensor values
     **/
    private boolean triggerCommand(float value, int threshold){
        return threshold > 0 ? value > threshold : value < threshold;
    }

    /**
     * Converts an array of float radians into degrees
     * @param v the array to be converted
     */
    private void toDegrees(float [] v){
        for (int i=0; i<v.length; i++) v[i]=(float)Math.toDegrees(v[i]);
    }

}
