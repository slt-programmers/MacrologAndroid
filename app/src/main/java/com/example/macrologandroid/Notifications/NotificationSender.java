package com.example.macrologandroid.Notifications;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.R;

import java.util.Calendar;

public class NotificationSender {


    public static void initNotificationSending(Activity activity) {
        createNotificationChannel(activity);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);

        // Intent to open MainActivity on tapping on navigation
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, activity.getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentTitle("Macrolog")
                .setContentText("Have you logged your meals today?")
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Intent to launch future notification
        Intent notificationIntent = new Intent(activity, NotificationReceiver.class);
        notificationIntent.putExtra("NOTIFICATION", builder.build());
        notificationIntent.putExtra("NOTIFICATION_ID", 0);
        PendingIntent pendingNotification = PendingIntent.getBroadcast(activity, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotification);
    }


    private static void createNotificationChannel(Activity activity) {
        CharSequence name = activity.getString(R.string.notification_channel_name);
        String description = "Reminders to log your meals";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(activity.getString(R.string.notification_channel_id), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
