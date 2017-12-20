package com.dataart.obd2.devicehive;

import android.os.Build;

public class DeviceHiveOld {

    private static final String TAG = "AndroidOBD2";


    private static void getTestDeviceData() {
//         Network network = new Network("AndroidOBD", "");
//        final DeviceClass deviceClass = new DeviceClass("Android OBD2 Device", BuildConfig.VERSION_NAME);
//
//        return new DeviceData(
//                new DevicePreferences().getGatewayId(),
//                "582c2008-cbb6-4b1a-8cf1-7cec1388db9f",
//                getDeviceName(),
//                DeviceData.DEVICE_STATUS_ONLINE,
//                deviceClass);
    }

    public static String getDeviceName() {
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        return model.startsWith(manufacturer) ? model : manufacturer + " " + model;
    }


}
