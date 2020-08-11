package com.android2.taxidrivershelpeachother.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android2.taxidrivershelpeachother.controller.NotificationService;

public class NotificationReceiver2 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);
        service.putExtras(intent.getExtras());
        context.startForegroundService(service);
    }
}
