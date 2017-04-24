package com.texton.texton;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by allenwixted on 13/04/2017.
 */

public class TimerService extends Service {


    private Handler handler;
    private Runnable runnable = null;
    private long boostTimer = 900000;
    private int[] boostValues = new int[] {15,30,45,60,120};
    private SharedPreferences sp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        //final SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        handler = new Handler();

        boostTimer = boostValues[sp.getInt("boostSelection", 0)] * 60 * 1000;
                     runnable = new Runnable() {
                        @Override
                        public void run() {
                            if(boostTimer/1000 > 0){
                                Log.d("RUN", String.valueOf(boostTimer/1000) + " Time chosen: " + sp.getInt("boostSelection", 0));
                                //re-run every second
                                boostTimer = boostTimer - 1000;
                                handler.postDelayed(this, 1000);
                            } else {
                                sp.edit().putBoolean("boostToggle", false).apply();
                                sp.edit().putBoolean("heatToggle", false).apply();
                                Log.d("DONE", String.valueOf(boostTimer/1000));
                                showNotification();
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Text On")
                        .setContentText("Boost mode is finished");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
