package com.rzm.exceptionhandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rzm on 2017/8/10.
 * 抓取未经捕获的异常
 * setSavePath指定目录
 */
public class UncaughtCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String CRASH_FILE_NAME = "crash_file_name";
    private static volatile UncaughtCrashHandler mInstance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
    private File savePath;


    private UncaughtCrashHandler() {
    }

    public static UncaughtCrashHandler getInstance() {
        if (mInstance == null) {
            synchronized (UncaughtCrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new UncaughtCrashHandler();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        Thread.currentThread().setUncaughtExceptionHandler(this);
        mDefaultUncaughtExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler();
        if (savePath == null){
            savePath = new File(context.getCacheDir().getAbsolutePath()+File.separator+"UncaughtCrash");
        }
    }

    /**
     * 必须是目录，而不是文件
     * @param savePath
     * @return
     */
    public UncaughtCrashHandler setSavePath(File savePath) {
        if (savePath == null){
            throw new NullPointerException("path is null");
        }
        if(savePath.isFile()){
            throw new IllegalArgumentException("save path cannot be a file,should pass a directory");
        }
        this.savePath = savePath;
        return this;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String crashFileName = saveToPath(throwable);
        cacheCrashFile(crashFileName);
        mDefaultUncaughtExceptionHandler.uncaughtException(thread, throwable);
    }

    private void cacheCrashFile(String crashFileName) {
        SharedPreferences preferences = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        preferences.edit().putString(CRASH_FILE_NAME, crashFileName).commit();
    }

    public File getCrashFile(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("crash", Context.MODE_PRIVATE);
        String string = preferences.getString(CRASH_FILE_NAME, null);
        if (string != null)
            return new File(string);
        else
            return null;
    }

    private String saveToPath(Throwable throwable) {
        HashMap<String, String> simpleInfoMap = obtainSimpleInfo(mContext);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : simpleInfoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }
        String exceptionInfo = botainExceptionInfo(throwable);
        sb.append(exceptionInfo);

        String fileName = null;
        try {
            if (!savePath.exists()){
                savePath.mkdirs();
            }
            fileName = savePath.toString() + File.separator + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt";
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private String getAssignTime(String data) {
        DateFormat format = new SimpleDateFormat(data);
        long currentTimeMillis = System.currentTimeMillis();
        return format.format(currentTimeMillis);
    }

    /**
     * 删除一个文件或文件夹
     * @param dir
     * @return
     */
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile())
                    files[i].delete();
            }
        }else{
            dir.delete();
        }
        return true;
    }

    private String botainExceptionInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        throwable.printStackTrace(writer);
        writer.close();
        return stringWriter.toString();
    }

    private HashMap<String, String> obtainSimpleInfo(Context mContext) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            map.put("versionName", packageInfo.versionName);
            map.put("versionCode", packageInfo.versionCode + "");
            map.put("MODEL", Build.MODEL);
            map.put("SDK_INT", Build.VERSION.SDK_INT + "");
            map.put("PRODUCT", Build.PRODUCT);
            map.put("MOBILDE_INFO", getMoBileInfo());

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    private String getMoBileInfo() {
        StringBuffer buffer = new StringBuffer();
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                buffer.append(name + "=" + value);
                buffer.append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
