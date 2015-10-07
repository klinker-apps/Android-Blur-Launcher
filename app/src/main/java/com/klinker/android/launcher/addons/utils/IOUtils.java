package com.klinker.android.launcher.addons.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.klinker.android.launcher.launcher3.LauncherProvider;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Map;

public class IOUtils {

    private static final String TAG = "IOUtils";

    public static boolean loadSharedPreferencesFromFile(File src, Context context) {
        boolean res = false;
        ObjectInputStream input = null;

        try {
            if (!src.getParentFile().exists()) {
                src.getParentFile().mkdirs();
                src.createNewFile();
            }

            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();

            prefEdit.clear();

            @SuppressWarnings("unchecked")
            Map<String, ?> entries = (Map<String, ?>) input.readObject();

            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean) {
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                } else if (v instanceof Float) {
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                } else if (v instanceof Integer) {
                    prefEdit.putInt(key, ((Integer) v).intValue());
                } else if (v instanceof Long) {
                    prefEdit.putLong(key, ((Long) v).longValue());
                } else if (v instanceof String) {
                    prefEdit.putString(key, ((String) v));
                }
            }

            prefEdit.commit();

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {

            }
        }

        restoreDatabases();

        return res;
    }

    public static boolean saveSharedPreferencesToFile(File dst, Context context) {
        boolean res = false;
        ObjectOutputStream output = null;

        try {
            if (!dst.getParentFile().exists()) {
                dst.getParentFile().mkdirs();
                dst.createNewFile();
            }

            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

            output.writeObject(pref.getAll());

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (Exception e) {

            }
        }

        backupDatabases();

        return res;
    }

    private static final String[] DATABASES = {
            "launcher.db",
            "stats.db"
    };

    public static void backupDatabases() {
        File sd = new File(Environment.getExternalStorageDirectory() + "/Blur/");
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {
            for (String tableName : DATABASES) {
                String currentDBPath = "//data//com.klinker.android.launcher//databases//" + tableName;
                String backupDBPath = tableName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                Log.v(TAG, "DB Source: " + currentDB.getPath() + "\n" + "Destination: " + backupDB.getPath());

                if (currentDB.exists()) {
                    try {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    } catch (Exception e) {
                        Log.e(TAG, "logging error", e);
                    }
                }
            }
        }
    }

    public static void restoreDatabases() {
        File sd = new File(Environment.getExternalStorageDirectory() + "/Blur/");
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {
            for (String tableName : DATABASES) {
                String currentDBPath = "//data//com.klinker.android.launcher//databases//" + tableName;
                String backupDBPath = tableName;
                File currentDB = new File(sd, backupDBPath);
                File backupDB = new File(data, currentDBPath);

                Log.v("IOUtils", "DB Source: " + currentDB.getPath() + "\n" + "Destination: " + backupDB.getPath());

                if (currentDB.exists()) {
                    try {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    } catch (Exception e) {
                        Log.e(TAG, "logging error", e);
                    }
                }
            }
        }
    }

}
