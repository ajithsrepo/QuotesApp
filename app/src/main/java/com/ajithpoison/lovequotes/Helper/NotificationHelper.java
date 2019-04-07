package com.ajithpoison.lovequotes.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.ajithpoison.lovequotes.R;

public class NotificationHelper extends ContextWrapper {

    public static final String CHANNEL_ID = "channel";
    public static final String CHANNEL_NAME = "Notify App";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

         channel.setDescription(("just to test"));
         channel.enableLights(false);
         channel.enableVibration(true);
         channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

         getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if(manager == null) {
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    //public method for api26 notification

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title,
                                                String message,
                                                Uri sound,
                                                PendingIntent pendingIntent,
                                                boolean isClicked) {
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_icon)
                .setSound(sound)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(isClicked);
    }

}
