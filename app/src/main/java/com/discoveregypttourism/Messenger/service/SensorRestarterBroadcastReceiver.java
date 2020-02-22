package com.discoveregypttourism.Messenger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.discoveregypttourism.Services.NotificationServices;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, NotificationServices.class));;
    }
}