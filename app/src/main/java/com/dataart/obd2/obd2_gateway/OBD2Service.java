package com.dataart.obd2.obd2_gateway;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dataart.android.devicehive.network.DeviceHiveApiService;
import com.dataart.obd2.MainActivity;
import com.dataart.obd2.R;

import timber.log.Timber;

/**
 * Created by idyuzheva
 */
public class OBD2Service extends Service {

    private final static String TAG = OBD2Service.class.getSimpleName();

    public final static String ACTION_BT_PERMISSION_REQUEST = TAG
            .concat("ACTION_BT_PERMISSION_REQUEST");

    private final static int LE_NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private BroadcastReceiver mReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private OBD2Gateway mObd2Gateway;

    public OBD2Service() {
        super();
    }

    public static void start(final Context context) {
        context.startService(new Intent(context, OBD2Service.class));
    }

    public static void stop(final Context context) {
        context.stopService(new Intent(context, OBD2Service.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            send(ACTION_BT_PERMISSION_REQUEST);
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        registerReceiver(getBtStateReceiver(), new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mObd2Gateway = new OBD2Gateway(this) {
            @Override
            public void updateState(String text) {
                notifyNewState(text);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("Service.onStartCommand");
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothServer.scanStart();
//        }

        setNotification();
        mObd2Gateway.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("Service.onDestroy");
        mObd2Gateway.stop();
        stopService(new Intent(this, DeviceHiveApiService.class));
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mNotificationManager.cancel(LE_NOTIFICATION_ID);
        super.onDestroy();
        Log.d(TAG, "OBD2Service was destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("Service.onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.d("Service.onUnbind");
        return super.onUnbind(intent);
    }

    private void notifyNewState(final String text) {
        Timber.d("Service.onUnbind");
        mBuilder.setContentText(text);
        mNotificationManager.notify(LE_NOTIFICATION_ID, mBuilder.build());
    }

    private BroadcastReceiver getBtStateReceiver() {
        if (mReceiver == null) {
            mReceiver = new BluetoothStateReceiver() {
                @Override
                protected void onBluetoothOff() {
                    notifyNewState(getString(R.string.notification_bt_off));
                    Toast.makeText(OBD2Service.this, getString(R.string.notification_bt_off),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onBluetoothOn() {
//                    mBluetoothServer.scanStart();
                    notifyNewState(getString(R.string.notification_disconnected));
                }
            };
        }
        return mReceiver;
    }

    private void send(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void setNotification() {
        final Intent resultIntent = new Intent(this, MainActivity.class);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.notification_disconnected))
                .setContentTitle(getString(R.string.device_hive))
                .setSmallIcon(R.drawable.ic_le_service)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(resultPendingIntent);

        mNotificationManager.notify(LE_NOTIFICATION_ID, mBuilder.build());
    }

}
