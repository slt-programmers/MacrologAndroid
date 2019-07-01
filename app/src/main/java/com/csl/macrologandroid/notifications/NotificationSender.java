package com.csl.macrologandroid.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.csl.macrologandroid.R;

import java.util.Calendar;

public class NotificationSender {

    public static void initNotificationSending(Context context) {
        createNotificationChannel(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar morningCal = Calendar.getInstance();
        morningCal.set(Calendar.HOUR_OF_DAY, 10);
        morningCal.set(Calendar.MINUTE, 0);

        Calendar eveningCal = Calendar.getInstance();
        eveningCal.set(Calendar.HOUR_OF_DAY, 19);
        eveningCal.set(Calendar.MINUTE, 0);

        Calendar now = Calendar.getInstance();
        if (now.after(morningCal)) {
            morningCal.add(Calendar.HOUR_OF_DAY, 24);
        }
        if (now.after(eveningCal)) {
            eveningCal.add(Calendar.HOUR_OF_DAY, 24);
        }

        // Intent to launch future notification
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingNotification = PendingIntent.getBroadcast(context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent secondPendingNotification = PendingIntent.getBroadcast(context, 2, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, morningCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotification);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, eveningCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, secondPendingNotification);
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = "Reminders to log your meals";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.notification_channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
