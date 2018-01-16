package com.dataart.obd2;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by alrybakov
 */

public class OBD2Application extends MultiDexApplication {

    private static OBD2Application application;


    public static OBD2Application getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        application = this;
    }

}
