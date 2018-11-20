package com.example.anik.tripmate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by anik on 3/6/18.
 */

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.ANDROID";
    public static final String IOS_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.IOS";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    public static final String IOS_CHANNEL_NAME = "IOS CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        // create android channel
        NotificationChannel androidChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        }
        // Sets whether notifications posted to this channel should display notification lights
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel.enableLights(true);
        }
        // Sets whether notification posted to this channel should vibrate.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel.enableVibration(true);
        }
        // Sets the notification light color for notifications posted to this channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel.setLightColor(Color.GREEN);
        }
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getManager().createNotificationChannel(androidChannel);
        }

        // create ios channel
        NotificationChannel iosChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iosChannel = new NotificationChannel(IOS_CHANNEL_ID,
                    IOS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iosChannel.enableLights(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iosChannel.enableVibration(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iosChannel.setLightColor(Color.GRAY);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iosChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getManager().createNotificationChannel(iosChannel);
        }
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true);
        }
        else
            return null;
    }
}
