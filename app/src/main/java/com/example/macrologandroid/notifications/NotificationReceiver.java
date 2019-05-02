package com.example.macrologandroid.notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = intent.getParcelableExtra("NOTIFICATION");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        notificationManager.notify(notificationId, notification);
    }
}
