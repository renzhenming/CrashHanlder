package com.rzm.exceptioncrashhandler;

import android.app.Application;
import com.rzm.exceptionhandler.UncaughtCrashHandler;

import java.io.File;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UncaughtCrashHandler.getInstance()
                .setSavePath(new File(getCacheDir().getAbsolutePath()+File.separator+"aaaaa"))
                .init(this);
    }
}
