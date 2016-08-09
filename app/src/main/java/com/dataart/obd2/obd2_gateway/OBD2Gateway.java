package com.dataart.obd2.obd2_gateway;

import android.content.Context;

import com.dataart.android.devicehive.Command;
import com.dataart.android.devicehive.Notification;
import com.dataart.android.devicehive.device.CommandResult;
import com.dataart.android.devicehive.device.future.SimpleCallableFuture;
import com.dataart.obd2.R;
import com.dataart.obd2.devicehive.DeviceHive;
import com.dataart.obd2.devicehive.DevicePreferences;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.exceptions.ResponseException;
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
                TroubleCodesCommand troubleCodesCommand = new TroubleCodesCommand();
                if (mObd2Reader.runCommand(troubleCodesCommand)) {
                    String codes = troubleCodesCommand.getFormattedResult();
                    if (codes != null) {
                        final String codeArray[] = codes.split("\n");
                        return new SimpleCallableFuture<>(new CommandResult(
                                CommandResult.STATUS_COMLETED, new Gson().toJson(codeArray)));
                    } else {
                        status = CommandResult.STATUS_FAILED;
                        result = "Failed to read codes";
                    }
                } else {
                    status = CommandResult.STATUS_FAILED;
                    result = "Failed to run troubleCodesCommand";
                }
            } else if (name.equalsIgnoreCase("RunCommand")) {
                final HashMap<String, Object> params = (HashMap<String, Object>) command.getParameters();
                final String mode = (params != null) ? (String) params.get("mode") : null;
                final String pid = (params != null) ? (String) params.get("pid") : null;
                if (mode == null || pid == null) {
                    status = CommandResult.STATUS_FAILED;
                    result = "Please specify mode and pid parameters";
                } else {
                    ObdRawCommand obdCommand = new ObdRawCommand(mode + " " + pid);
                    boolean commandRes = true;
                    try  {
                        commandRes = mObd2Reader.runCommand(obdCommand);
                    } catch (ResponseException e) {
                        // ignore response error and send it as is
                    }
                    if (commandRes) {
                        status = CommandResult.STATUS_COMLETED;
                        result = obdCommand.getFormattedResult();
                    } else {
                        status = CommandResult.STATUS_FAILED;
                        result = "Failed to run command";
                    }
                }
            } else {
                status = CommandResult.STATUS_FAILED;
                result = mContext.getString(R.string.unknown_commnad);
            }
            return new SimpleCallableFuture<>(new CommandResult(status, new Gson().toJson(result)));
        }
    };

    public abstract void updateState(String text);
}
