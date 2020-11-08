package com.android2.taxidrivershelpeachother.controller;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.android2.taxidrivershelpeachother.view.NotificationReceiver2;

import java.util.ArrayList;
import java.util.List;

public class NotificationService extends Service {
    private LogicHandler logicHandler;
    private boolean availableToTakeShuttles;
    private String notificationTopic;
    private Intent parentIntent;
    private Context context;
    private Location prevLocation, currLocation;
    private SharedPreferences settings;
    private long timeBetweenNotifications = 1;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.parentIntent = intent;
        this.context = getApplicationContext();
        job();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        settings = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_03";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Taxi Drivers Help Each Other",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("New Shuttle request")
                    .setContentText("").build();

            startForeground(3, notification);
        }
    }

    private void job() {
        Intent intent = new Intent(context, NotificationReceiver2.class);

        if(parentIntent != null) {
            intent.putExtras(parentIntent.getExtras());
        }

        availableToTakeShuttles = settings.getBoolean("isAvailable", false);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(availableToTakeShuttles) {
            logicHandler = new LogicHandler(context);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5*1000, pendingIntent);
            logicHandler.startLocation();
        }
        else{
            context.stopService(new Intent(context.getApplicationContext(), NotificationService.class));
        }
    }
}
