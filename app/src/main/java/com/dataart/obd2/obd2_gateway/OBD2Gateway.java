package com.dataart.obd2.obd2_gateway;

import android.content.Context;

import com.dataart.android.devicehive.device.CommandResult;
import com.dataart.obd2.R;
import com.dataart.obd2.devicehive.DevicePreferences;
import com.github.devicehive.client.model.CommandFilter;
import com.github.devicehive.client.model.DHResponse;
import com.github.devicehive.client.model.DeviceCommandsCallback;
import com.github.devicehive.client.model.FailureData;
import com.github.devicehive.client.model.Parameter;
import com.github.devicehive.client.service.Device;
import com.github.devicehive.client.service.DeviceCommand;
import com.github.devicehive.client.service.DeviceHive;
import com.github.devicehive.rest.model.JsonStringWrapper;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.exceptions.ResponseException;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Nikolay Khabarov on 8/8/16.
 */

public abstract class OBD2Gateway {
    private final static String TAG = OBD2Gateway.class.getSimpleName();
    private Context mContext;
    private OBD2Reader mObd2Reader;
    private DeviceHive deviceHive;
    private Device device;
    PublishSubject<DeviceCommand> source;

    public OBD2Gateway(Context context) {
        mContext = context;
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
                Method[] methods = OBD2Data.class.getDeclaredMethods();

                //Full info data from obd
                HashMap<String, Object> map = new HashMap<>();

                for (Method method : methods) {
                    String name = method.getName();
                    if (name.startsWith("get")) {
                        Object obj = null;
                        try {
                            obj = method.invoke(data);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        if (obj != null) {
                            map.put(name.substring(3), obj);
                        }
                    }
                }

                device.sendNotification("obd2", Collections.singletonList(
                        new Parameter("parameters", new Gson().toJson(map))));
            }
        };
    }

    public void start() {
        final DevicePreferences prefs = new DevicePreferences();
        deviceHive = DeviceHive.getInstance().init(prefs.getServerUrl(),
                "");

        DHResponse<Device> deviceDHResponse = deviceHive.getDevice(prefs.getGatewayId());

        mObd2Reader.start();
    }

    public void stop() {
        if (source != null) {
            source.onComplete();
        }
        mObd2Reader.stop();
    }

    public void subscribeOnCommands() {
        source = PublishSubject.create();
        source.subscribeOn(Schedulers.io())
                .subscribe(getCommandObservable());
    }


    private Observer<DeviceCommand> getCommandObservable() {
        return new Observer<DeviceCommand>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe");
                new Thread(() -> {
                    DevicePreferences prefs = new DevicePreferences();
                    DHResponse<Device> deviceDHResponse = deviceHive.getDevice(prefs.getGatewayId());
                    if (!deviceDHResponse.isSuccessful()) {
                        return;
                    }
                    Device device = deviceDHResponse.getData();
                    CommandFilter commandFilter = new CommandFilter();
                    device.subscribeCommands(commandFilter, new DeviceCommandsCallback() {
                        @Override
                        public void onSuccess(List<DeviceCommand> list) {
                            DeviceCommand command = list.get(0);
                            onNext(command);
                        }

                        @Override
                        public void onFail(FailureData failureData) {
                            onError(new Throwable(failureData.getMessage()));
                        }
                    });
                }).start();
            }

            @Override
            public void onNext(DeviceCommand command) {
                String status = CommandResult.STATUS_FAILED;
                String result = "";

                final String name = command.getCommandName();
                if (name.equalsIgnoreCase("GetTroubleCodes")) {
                    TroubleCodesCommand troubleCodesCommand = new TroubleCodesCommand();
                    if (mObd2Reader.runCommand(troubleCodesCommand)) {
                        String codes = troubleCodesCommand.getFormattedResult();
                        if (codes != null) {
                            final String codeArray[] = codes.split("\n");
                            command.setStatus(CommandResult.STATUS_COMLETED);
                            command.setResult(new JsonStringWrapper(new Gson().toJson(codeArray)));
                            command.updateCommand();
                        } else {
                            status = CommandResult.STATUS_FAILED;
                            result = "Failed to read codes";
                        }
                    } else {
                        status = CommandResult.STATUS_FAILED;
                        result = "Failed to run troubleCodesCommand";
                    }
                } else if (name.equalsIgnoreCase("RunCommand")) {
                    JsonStringWrapper wrapperParams = command.getParameters();
                    OBD2Params params = new Gson().fromJson(wrapperParams.getJsonString(), OBD2Params.class);

                    final String mode = (params != null) ? params.getMode() : null;
                    final String pid = (params != null) ? params.getPid() : null;
                    if (mode == null || pid == null) {
                        status = CommandResult.STATUS_FAILED;
                        result = "Please specify mode and pid parameters";
                    } else {
                        ObdRawCommand obdCommand = new ObdRawCommand(mode + " " + pid);
                        boolean commandRes = true;
                        try {
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
                command.setStatus(status);
                command.setResult(new JsonStringWrapper(result));
                command.updateCommand();

            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };


    }


    public abstract void updateState(String text);
}
