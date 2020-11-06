package com.humudtech.paynama.Services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.humudtech.paynama.BaseActivity;
import com.humudtech.paynama.R;

import java.util.Map;
import java.util.Random;

public class PayNamaMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String mToken) {
        super.onNewToken(mToken);
    }
    private static final String CHANNEL_ID = "payNama";
    private static final String CHANNEL_NAME = "Pay Nama";
    PendingIntent pendingIntent;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        if (remoteMessage.getData().size() > 0) {
//            sendNotification(remoteMessage.getData());
//        }
        sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }
    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final int not_nu=generateRandom();
        pendingIntent = PendingIntent.getActivity(this,not_nu, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(body);
        bigText.setBigContentTitle(title);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setStyle(bigText)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);

        }
        notificationManager.notify(not_nu/* ID of notification */, notificationBuilder.build());
    }
    public int generateRandom(){
        Random random = new Random();
        return random.nextInt(9999 - 1000) + 1000;
    }
}
