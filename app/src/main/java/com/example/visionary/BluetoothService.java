package com.example.visionary;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothAdapter mAdapter;
    private Set<BluetoothDevice> paired;
    private BluetoothDevice mDevice = null;
    private UUID uuid;
    private BluetoothSocket mSocket;
    private IBinder mBinder = new BluetoothBinder();

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Bluetooth", "onCreate");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            Log.d("Bluetooth Service", "device not support");
        }
        paired = mAdapter.getBondedDevices();
        if (paired.size() > 0) {
            for (BluetoothDevice device : paired) {
                if (device.getName().equals("HC-06")) {
                    mDevice = device;
                }
            }
            if(mDevice!=null) {
                ParcelUuid[] ids = mDevice.getUuids();
                for (ParcelUuid uuid1 : ids) {
                    uuid = uuid1.getUuid();
                }
                try {
                    mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                    mSocket.connect();
                    Log.d("Bluetooth", "conn");
                    if (mSocket.isConnected()) {
                        String msg = "Awaz Dooooo";
                        try {
                            InputStream mInputStream = mSocket.getInputStream();
                            OutputStream outputStream = mSocket.getOutputStream();
                            Log.e("Bluetooth", "Sending Message");
                            outputStream.write(msg.getBytes());
                            int i;
                            while ((i = mInputStream.read()) != -1) {
                                Log.e("TAG", i + "");
                            }
                            Log.e("Bluetooth", "Message Sent");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.d("Bluetooth", "not found socket");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
