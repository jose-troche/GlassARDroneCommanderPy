
package com.troche.glass.quadcopter;

///*
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Chronometer;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.lang.reflect.Method;
//import java.util.UUID;
//
///**
// * Main activity.
// */
//public class MainActivity extends Activity {
//
//  private Chronometer mChronometer;
//  private TextView mHint;
//
//  private long mBase = 0;
//  private boolean mStarted = false;
//
//  @Override
//  protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.layout_stopwatch);
//
//    mChronometer = (Chronometer) findViewById(R.id.stopwatch);
//    mHint = (TextView) findViewById(R.id.phrase_hint);
//  }
//
//  /**
//   * Handle the tap event from the touchpad.
//   */
//  @Override
//  public boolean onKeyDown(int keyCode, KeyEvent event) {
//    switch (keyCode) {
//    // Handle tap events.
//    case KeyEvent.KEYCODE_DPAD_CENTER:
//    case KeyEvent.KEYCODE_ENTER:
//      toggleStopWatch();
//      return true;
//    default:
//      return super.onKeyDown(keyCode, event);
//    }
//  }
//
//  @Override
//  public void onResume() {
//    super.onResume();
//    if (!mStarted) {
//      mBase = mChronometer.getBase();
//      toggleStopWatch();
//    }
//  }
//
//  @Override
//  public void onPause() {
//    super.onPause();
//    if (mStarted) {
//      mChronometer.stop();
//      mStarted = false;
//    }
//  }
//
//  @Override
//  public void onDestroy() {
//    if (mStarted) {
//      mChronometer.stop();
//      mStarted = false;
//    }
//    super.onDestroy();
//  }
//
//  /**
//   * Toggle the StopWatch states.
//   */
//  private void toggleStopWatch() {
//    if (mStarted) {
//      mBase = SystemClock.elapsedRealtime();
//      mChronometer.stop();
//      mHint.setText(R.string.stopwatch_resume);
//    } else {
//      mChronometer.setBase(mChronometer.getBase() + SystemClock.elapsedRealtime() - mBase);
//      mChronometer.start();
//      mHint.setText(R.string.stopwatch_pause);
//    }
//    mStarted = !mStarted;
//  }
//}

/**
 *  @version 1.1 (28.01.2013)
 *  http://english.cxem.net/arduino/arduino5.php
 *  @author Koltykov A.V. (�������� �.�.)
 *
 */


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "quadcopter-commander";

    Button btnOn, btnOff;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:88:65:3E:4C:B3";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        btnOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("1");
                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("0");
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

    /*try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }*/

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }
}



