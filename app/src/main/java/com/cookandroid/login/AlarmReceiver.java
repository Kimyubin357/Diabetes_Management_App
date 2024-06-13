package com.cookandroid.login;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "201821079";
    private static final String CHANNEL_NAME = "YAKSSOK";

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationId = intent.getStringExtra("id");
        String text = intent.getStringExtra("drug");

        Log.e(TAG, "약번호 넘어오자: " + notificationId);
        Log.e(TAG, "약이름 넘어오자: " + text);

        Intent mainIntent = new Intent(context, SetAlarm.class);
        mainIntent.putExtra("cancelId", notificationId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, Integer.parseInt(notificationId), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_drug_icon)
                .setContentTitle("약꾹")
                .setContentText(text + "을(를) 복용할 시간이에요:)")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(context, "오레오 이상", Toast.LENGTH_SHORT).show();
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("매일 정해진 시간에 알림합니다.");
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My:Tag");
        wakeLock.acquire(5000);
        notificationManager.notify(Integer.parseInt(notificationId), builder.build());
    }
}
