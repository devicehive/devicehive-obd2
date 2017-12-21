package com.dataart.obd2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dataart.obd2.devicehive.DevicePreferences;
import com.dataart.obd2.obd2_gateway.OBD2Service;

import java.util.Objects;
import java.util.Set;

import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;
    private EditText serverUrlEditText;
    private EditText gatewayIdEditText;
    private EditText jwtAccessTokenEditText;
    private TextView hintText;
    private Button serviceButton;
    private Button restartServiceButton;
    private Spinner btDevicesSpinner;
    private DevicePreferences prefs;
    private boolean isServiceStarted;
    private final View.OnClickListener restartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!validateValues()) {
                return;
            }
            if (!isDevicesEnabled()) {
                Toast.makeText(MainActivity.this, "There is no paired devices were found", Toast.LENGTH_SHORT).show();
                return;
            }
            saveValues();
            OBD2Service.stop(MainActivity.this);
            OBD2Service.start(MainActivity.this);
            restartServiceButton.setVisibility(View.GONE);
            serviceButton.setVisibility(View.VISIBLE);
            onServiceRunning();
            hintText.setVisibility(View.GONE);

        }
    };
    private final TextView.OnEditorActionListener changeListener = (textView, actionId, keyEvent) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
            onDataChanged();
        }
        return false;
    };
    private final AdapterView.OnItemSelectedListener btItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            onDataChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private final TextWatcher changeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            onDataChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        Timber.plant(new Timber.DebugTree());

        init();
    }

    private void fatalDialog(int message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.unsupported)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .create().show();
    }

    private void init() {

        if (!isBluetoothSupported()) {
            fatalDialog(R.string.error_message_bt_not_supported);
            return;
        }

        prefs = new DevicePreferences();

        serverUrlEditText = findViewById(R.id.server_url_edit);
        gatewayIdEditText = findViewById(R.id.settings_gateway_id);
        jwtAccessTokenEditText = findViewById(R.id.jwtRefreshToken_edit);
        hintText = findViewById(R.id.hintText);
        btDevicesSpinner = findViewById(R.id.bt_list);

        resetValues();

        serviceButton = findViewById(R.id.service_button);
        serviceButton.setOnClickListener(view -> startService());

        restartServiceButton = findViewById(R.id.save_button);
        //noinspection ConstantConditions
        restartServiceButton.setOnClickListener(restartClickListener);

        serverUrlEditText.setOnEditorActionListener(changeListener);
        serverUrlEditText.addTextChangedListener(changeWatcher);

        gatewayIdEditText.setOnEditorActionListener(changeListener);
        gatewayIdEditText.addTextChangedListener(changeWatcher);

        jwtAccessTokenEditText.setOnEditorActionListener(changeListener);
        jwtAccessTokenEditText.addTextChangedListener(changeWatcher);

        btDevicesSpinner.setOnItemSelectedListener(btItemSelectedListener);

        if (isServiceRunning()) {
            onServiceRunning();
        }
    }

    private boolean isBluetoothSupported() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Timber.e(getString(R.string.bt_unable_init));
                return false;
            }
        }
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Timber.e(getString(R.string.bt_unable_get_btm));
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBondedDevices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isServiceRunning() {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (Objects.equals(OBD2Service.class.getName(), service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startService() {
        if (!isServiceStarted) {
            if (!validateValues()) {
                return;
            }
            if (!isDevicesEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.unsupported)
                        .setMessage(R.string.error_message_bt_empty_list)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .create().show();
                return;
            }
            saveValues();
            onServiceRunning();
            OBD2Service.start(MainActivity.this);
        } else {
            onServiceStopped();
            OBD2Service.stop(MainActivity.this);
        }
    }

    private void onServiceRunning() {
        isServiceStarted = true;
        serviceButton.setText(R.string.button_stop);
    }

    private void onServiceStopped() {
        isServiceStarted = false;
        serviceButton.setText(R.string.button_start);
    }

    private boolean isRestartRequired() {
        final String newUrl = serverUrlEditText.getText().toString();
        final String newGatewayId = gatewayIdEditText.getText().toString();
        final String newAccessKey = jwtAccessTokenEditText.getText().toString();
        final Object newMac = btDevicesSpinner.getSelectedItem();
        return !(Objects.equals(prefs.getServerUrl(), newUrl) &&
                Objects.equals(prefs.getGatewayId(), newGatewayId) &&
                Objects.equals(prefs.getJwtRefreshToken(), newAccessKey) &&
                (newMac != null && newMac.toString().endsWith(prefs.getOBD2Mac())));
    }

    private void onDataChanged() {
        if (isServiceStarted && isRestartRequired()) {
            hintText.setVisibility(View.VISIBLE);
            restartServiceButton.setVisibility(View.VISIBLE);
            serviceButton.setVisibility(View.GONE);
        } else {
            hintText.setVisibility(View.GONE);
            restartServiceButton.setVisibility(View.GONE);
            serviceButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateBondedDevices() {
        String obd2mac = prefs.getOBD2Mac();
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item);
        btDevicesSpinner.setAdapter(listAdapter);

        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                listAdapter.add(device.getName() + "  " + device.getAddress());
                if (device.getAddress().equals(obd2mac)) {
                    btDevicesSpinner.setSelection(listAdapter.getCount() - 1);
                }
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    private boolean isDevicesEnabled() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices().size() > 0;
    }

    private void resetValues() {
        String serverUrl = prefs.getServerUrl();
        serverUrlEditText.setText(
                TextUtils.isEmpty(serverUrl)
                        ? getString(R.string.default_server_url)
                        : serverUrl
        );

        String gatewayId = prefs.getGatewayId();
        gatewayIdEditText.setText(
                TextUtils.isEmpty(gatewayId)
                        ? getString(R.string.default_gateway_id)
                        : gatewayId
        );

        String accessKey = prefs.getJwtRefreshToken();
        jwtAccessTokenEditText.setText(
                TextUtils.isEmpty(accessKey)
                        ? ""
                        : accessKey
        );

        updateBondedDevices();
    }

    private void resetErrors() {
        serverUrlEditText.setError(null);
        gatewayIdEditText.setError(null);
        jwtAccessTokenEditText.setError(null);
    }

    private boolean validateValues() {
        resetErrors();

        String serverUrl = serverUrlEditText.getText().toString().trim();
        String gatewayId = gatewayIdEditText.getText().toString().trim();
        String jwtRefreshToken = jwtAccessTokenEditText.getText().toString().trim();

        if (TextUtils.isEmpty(serverUrl)) {
            serverUrlEditText.setError(getString(R.string.error_message_empty_server_url));
            serverUrlEditText.requestFocus();
        } else if (TextUtils.isEmpty(gatewayId)) {
            gatewayIdEditText.setError(getString(R.string.error_message_empty_gateway_id));
            gatewayIdEditText.requestFocus();
        } else if (TextUtils.isEmpty(jwtRefreshToken)) {
            jwtAccessTokenEditText.setError(getString(R.string.error_message_empty_jwt_refresh_token));
            jwtAccessTokenEditText.requestFocus();
        } else {
            return true;
        }

        return false;
    }

    private void saveValues() {
        final String serverUrl = serverUrlEditText.getText().toString().trim();
        final String gatewayId = gatewayIdEditText.getText().toString().trim();
        final String accessKey = jwtAccessTokenEditText.getText().toString().trim();
        final Object obd2mac = btDevicesSpinner.getSelectedItem();

        prefs.setJwtRefreshTokenSync(accessKey);
        prefs.setServerUrlSync(serverUrl);
        prefs.setGatewayIdSync(gatewayId);
        if (obd2mac != null) {
            final String mac = obd2mac.toString();
            prefs.setOBD2MacSync(mac.substring(mac.lastIndexOf(" ") + 1));
        }
    }

    private boolean isFieldsEmpty() {
        String serverUrl = serverUrlEditText.getText().toString();
        String gatewayId = gatewayIdEditText.getText().toString();
        String accessKey = jwtAccessTokenEditText.getText().toString();
        return TextUtils.isEmpty(serverUrl) || TextUtils.isEmpty(gatewayId) || TextUtils.isEmpty(accessKey);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void alertSdkVersionMismatch(final Runnable runnable) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sdk_version_warning_title)
                .setMessage(R.string.sdk_version_warning)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> runnable.run())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
