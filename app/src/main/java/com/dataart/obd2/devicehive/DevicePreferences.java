package com.dataart.obd2.devicehive;

import android.content.Context;
import android.content.SharedPreferences;

import com.dataart.obd2.OBD2Application;

public class DevicePreferences {

    private final static String NAMESPACE = "devicehive.";

    private final static String KEY_SERVER_URL = NAMESPACE
            .concat(".KEY_SERVER_URL");

    private final static String KEY_GATEWAY_ID = NAMESPACE
            .concat(".KEY_GATEWAY_ID");

    private final static String JWT_REFRESH_TOKEN = NAMESPACE
            .concat(".KEY_JWT_REFRESH_TOKEN");

    private final static String KEY_OBD2MAC= NAMESPACE
            .concat(".OBD2_MAC");

    private final Context context;

    private final SharedPreferences preferences;

    public DevicePreferences() {

        this.context = OBD2Application.getApplication();
        this.preferences = context.getSharedPreferences(
                context.getPackageName() + "_devicehiveprefs",
                Context.MODE_PRIVATE);
    }

    public String getServerUrl() {
        return preferences.getString(KEY_SERVER_URL, null);
    }

    public String getGatewayId() {
        return preferences.getString(KEY_GATEWAY_ID, null);
    }

    public String getOBD2Mac() {
        return preferences.getString(KEY_OBD2MAC, null);
    }

    public void setServerUrlSync(String serverUrl) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.commit();
    }

    public void setGatewayIdSync(String gatewayId) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_GATEWAY_ID, gatewayId);
        editor.commit();
    }

    public String getJwtRefreshToken() {
        return preferences.getString(JWT_REFRESH_TOKEN, null);
    }


    public void setJwtRefreshTokenSync(String jwtRefreshToken) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(JWT_REFRESH_TOKEN, jwtRefreshToken);
        editor.commit();
    }

    public void setOBD2MacSync(String mac) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_OBD2MAC, mac);
        editor.commit();
    }

}
