package com.ajithpoison.lovequotes.BroadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.ajithpoison.lovequotes.Helper.NotificationHelper;
import com.ajithpoison.lovequotes.Home;
import com.ajithpoison.lovequotes.R;

public class AlarmReceiver extends BroadcastReceiver {
    int sparklingHeartEmoji = 0x1F496;
    int coupleHeartEmoji = 	0x1F491;

    String title =  getEmojiByUnicode(sparklingHeartEmoji) + " Send a nice quote to your loved one! " + getEmojiByUnicode(coupleHeartEmoji);
    String message = "Make your loved one feel SPECIAL right away!";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, Home.class);
        context.startService(startServiceIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotificationAPI26(context);
        }
        else {
            sendNotification(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationAPI26(Context context) {

        NotificationHelper helper;
        Notification.Builder builder;

        Intent intent = new Intent(context, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        helper = new NotificationHelper(context);

        builder = helper.getNotification(title, message, sound, pendingIntent, true);

        helper.getManager().notify(1, builder.build());
    }

    private void sendNotification(Context context) {

        Intent intent = new Intent(context,Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(sound)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(1,builder.build());
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}
