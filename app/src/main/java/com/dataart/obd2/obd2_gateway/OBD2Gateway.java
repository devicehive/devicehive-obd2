package com.dataart.obd2.obd2_gateway;

import android.content.Context;
import android.util.Log;

import com.dataart.android.devicehive.Command;
import com.dataart.android.devicehive.Notification;
import com.dataart.android.devicehive.device.CommandResult;
import com.dataart.android.devicehive.device.future.SimpleCallableFuture;
import com.dataart.obd2.R;
import com.dataart.obd2.devicehive.DeviceHive;
import com.dataart.obd2.devicehive.DevicePreferences;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by Nikolay Khabarov on 8/8/16.
 */

public abstract class OBD2Gateway {
    private final static String TAG = OBD2Gateway.class.getSimpleName();
    private Context mContext;
    private OBD2Reader mObd2Reader;
    private DeviceHive mDeviceHive;

    public OBD2Gateway(Context context) {
        mContext = context;
        mDeviceHive = DeviceHive.newInstance(mContext);
        final DevicePreferences prefs = new DevicePreferences();
        mObd2Reader = new OBD2Reader(prefs.getOBD2Mac()) {
            @Override
            protected void statusCallback(Status status) {
                switch (status) {
                    case STATUS_DISCONNECTED:
                        updateState(mContext.getString(R.string.notification_disconnected));
                        break;
                    case STATUS_BLUETOOTH_CONNECTING:
                        updateState(mContext.getString(R.string.notification_bluetooth_connecting));
                        break;
                    case STATUS_OBD2_CONNECTING:
                        updateState(mContext.getString(R.string.notification_obd2_connecting));
                        break;
                    case STATUS_OBD2_LOOPING_DATA:
                        updateState(mContext.getString(R.string.notification_looping_data));
                        break;
                }
            }

            @Override
            protected void dataCallback(OBD2Data data) {
                mDeviceHive.sendNotification(new Notification("obd2", new Gson().toJson(data)));
            }
        };
    }

    public void start() {
        final DevicePreferences prefs = new DevicePreferences();
        mDeviceHive.setApiEnpointUrl(prefs.getServerUrl());

        mDeviceHive.setCommandListener(commandListener);
        if (!mDeviceHive.isRegistered()) {
            mDeviceHive.registerDevice();
        }
        mDeviceHive.startProcessingCommands();

        mObd2Reader.start();
    }

    public void stop() {
        mObd2Reader.stop();
        mDeviceHive.removeCommandListener();
        mDeviceHive.stopProcessingCommands();
    }

    private final DeviceHive.CommandListener commandListener = new DeviceHive.CommandListener() {
        @Override
        public SimpleCallableFuture<CommandResult> onDeviceReceivedCommand(Command command) {
            String status = CommandResult.STATUS_FAILED;
            String result = "";

            final String name = command.getCommand();
            if (name.equalsIgnoreCase("GetTroubleCodes")) {

            } else if (name.equalsIgnoreCase("RunCommand")) {
                final HashMap<String, Object> params = (HashMap<String, Object>) command.getParameters();
            } else {
                result = new Gson().toJson(mContext.getString(R.string.unknown_commnad));
            }
            return new SimpleCallableFuture<>(new CommandResult(status, result));
        }
    };

    public abstract void updateState(String text);
}
