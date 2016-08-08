package com.dataart.obd2.obd2_gateway;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Nikolay Khabarov on 8/5/16.
 */

public abstract class OBD2Reader implements Runnable{
    private final static int READ_INTERVAL = 1000;
    private final static int READ_ERROR_INTERVAL = 5000;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean isStarted = false;
    private String mDeviceMac;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mSocket = null;
    private boolean mObd2Init = false;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ObdResetCommand mObdResetCommand = new ObdResetCommand();
    private EchoOffCommand mEchoOffCommand = new EchoOffCommand();
    private LineFeedOffCommand mLineFeedOffCommand = new LineFeedOffCommand();
    private SelectProtocolCommand mSelectProtocolCommand = new SelectProtocolCommand(ObdProtocols.AUTO);

    final Handler mHandler = new Handler();

    public enum Status {
        STATUS_DISCONNECTED,
        STATUS_BLUETOOTH_CONNECTING,
        STATUS_OBD2_CONNECTING,
        STATUS_OBD2_LOOPING_DATA
    }

    public OBD2Reader(String mac) {
        mDeviceMac = mac;
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = btAdapter.getRemoteDevice(mDeviceMac);
    }

    public synchronized void start() {
        if (isStarted == false) {
            mHandler.postDelayed(this, READ_INTERVAL);
            isStarted = true;
            statusCallback(Status.STATUS_OBD2_CONNECTING);
        }
    }

    public synchronized void stop() {
        isStarted = false;
        mHandler.removeCallbacks(this);
    }

    private synchronized void nextIteration(boolean success) {
        if (!isStarted) {
            return;
        }
        mHandler.postDelayed(this, success ? READ_INTERVAL : READ_ERROR_INTERVAL);
    }

    private void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
            statusCallback(Status.STATUS_DISCONNECTED);
        }
    }

    private boolean ensureConnected() {
        if (mSocket == null) {
            statusCallback(Status.STATUS_BLUETOOTH_CONNECTING);
            try {
                mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                mSocket.connect();
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                OBD2Data.cleanIgnoredCommands();
            } catch (IOException e) {
                if (!(e instanceof IOException)){
                    e.printStackTrace();
                }
                closeSocket();
                return false;
            }
            mObd2Init = false;
            statusCallback(Status.STATUS_OBD2_CONNECTING);
        }

        if (!mObd2Init) {
            try {
                mObdResetCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "ObdResetCommand " + mObdResetCommand.getResult());
                mEchoOffCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "EchoOffCommand " + mEchoOffCommand.getResult());
                mLineFeedOffCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "LineFeedOffCommand " + mLineFeedOffCommand.getResult());
                mSelectProtocolCommand.run(mInputStream,  mOutputStream);
                Log.i("tag", "SelectProtocolCommand " + mSelectProtocolCommand.getResult());
            } catch (IOException e) {
                e.printStackTrace();
                closeSocket();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mObd2Init = true;
            statusCallback(Status.STATUS_OBD2_LOOPING_DATA);
        }
        return true;
    }

    private boolean iteration() {
        if (!ensureConnected()) {
            return false;
        }

        try {
            dataCallback(OBD2Data.readCurrentData(mInputStream, mOutputStream));
        } catch (IOException e) {
            e.printStackTrace();
            closeSocket();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        nextIteration(iteration());
    }

    public synchronized boolean runCommand(ObdCommand command) {
        if (!ensureConnected()) {
            return false;
        }
        try {
            command.run(mInputStream, mOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            closeSocket();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    protected abstract void statusCallback(Status status);
    protected abstract void dataCallback(OBD2Data data);
}
