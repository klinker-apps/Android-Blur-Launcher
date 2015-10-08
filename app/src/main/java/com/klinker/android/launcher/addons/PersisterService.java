package com.klinker.android.launcher.addons;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.klinker.android.launcher.R;


public class PersisterService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 21) {
            final Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_home)
                            .setContentTitle(getResources().getString(R.string.application_name))
                            .setContentText(getResources().getString(R.string.launcher_is_running))
                            .setOngoing(true)
                            .setPriority(Notification.PRIORITY_MIN)
                            .setWhen(0);

            startForeground(1, mBuilder.build());
        } else {
            final NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.color.transparent)
                            .setContentTitle(getResources().getString(R.string.application_name))
                            .setContentText(getResources().getString(R.string.launcher_is_running))
                            .setOngoing(true)
                            .setPriority(Notification.PRIORITY_MIN)
                            .setWhen(0)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_stat_home));

            startForeground(1, mBuilder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

}
