package com.example.anik.tripmate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import static com.example.anik.tripmate.ChatActivity.ac;

/**
 * Created by anik on 3/6/18.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("uid");
        String name = remoteMessage.getData().get("name");
        String type = remoteMessage.getData().get("type");
        boolean flag2 = false;

        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils mNotificationUtils;
            mNotificationUtils = new NotificationUtils(this);
            Notification.Builder mBuilder;

            if (type.equals("request")) {
                mBuilder = mNotificationUtils.
                        getAndroidChannelNotification("New Friend Request from "+name, notificationBody);
            } else if (type.equals("accepted")) {
                mBuilder = mNotificationUtils.
                        getAndroidChannelNotification("Friend Request Accepted", notificationBody);
            }
            else if(type.equals("pinpost")) {
                mBuilder = mNotificationUtils.
                        getAndroidChannelNotification("New Pin Post from "+name, notificationBody);
            } else {
                flag2 = true;
                mBuilder = mNotificationUtils.getAndroidChannelNotification("New message from " + name, notificationBody);
            }

            mBuilder.setAutoCancel(true);
            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("uid", from_user_id);
            resultIntent.putExtra("name", name);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);


            // Sets an ID for the notification
            int mNotificationId = (int) System.currentTimeMillis();

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            try {
                if (mNotifyMgr.areNotificationsEnabled()) {
                    try {
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), sound);
                        r.play();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Builds the notification and issues it.
            try {
                if(!ChatActivity.isAlive)
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            NotificationCompat.Builder mBuilder;

            if(type.equals("request")) {
                mBuilder =
                        new NotificationCompat.Builder(this, "com.example.anik.anikschatapp.ANDROID")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setSound(sound)
                                .setContentTitle("New Friend Request")
                                .setContentText(notificationBody);
            }

            else if(type.equals("accepted")){
                mBuilder =
                        new NotificationCompat.Builder(this, "com.example.anik.anikschatapp.ANDROID")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setSound(sound)
                                .setContentTitle("Friend Request Accepted")
                                .setContentText(notificationBody);

            }

            else if(type.equals("pinpost")) {
                mBuilder =
                        new NotificationCompat.Builder(this, "com.example.anik.anikschatapp.ANDROID")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setSound(sound)
                                .setContentTitle("New Pin Post from event "+name)
                                .setContentText(notificationBody);
            }
            else {
                flag2 = true;
                mBuilder = new NotificationCompat.Builder(this, "com.example.anik.anikschatapp.ANDROID")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setSound(sound)
                                    .setContentTitle("New message from "+name)
                                    .setContentText(notificationBody);
            }

            mBuilder.setAutoCancel(true);
            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("uid", from_user_id);
            resultIntent.putExtra("name", name);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);


            // Sets an ID for the notification
            int mNotificationId = (int)System.currentTimeMillis();

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            try {
                //if (!flag2)
                  //  mNotifyMgr.notify(mNotificationId, mBuilder.build());

                //else {
                  //  if(ac.isDestroyed() || ac.isFinishing()) {
                    //    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    //}
                    if(!ChatActivity.isAlive) {
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}