package com.dataart.obd2;

import android.app.Application;

/**
 * Created by alrybakov
 */

public class OBD2Application extends Application {

    private static OBD2Application application;


    public static OBD2Application getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
    }

}
