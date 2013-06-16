/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.messenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

import net.openfiresecurity.data.PreferenceHelper;
import net.openfiresecurity.messenger.push.ServerUtilities;

import static net.openfiresecurity.messenger.push.CommonUtilities.SENDER_ID;
import static net.openfiresecurity.messenger.push.CommonUtilities.displayMessage;

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        ServerUtilities.register(context, MainView.nameOwn, MainView.emailOwn,
                registrationId);
    }

    /**
     * Method called on device un registred
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     */
    @Override
    protected void onMessage(Context context, Intent intent) {

        String title = intent.getExtras().getString("title");

        String content = intent.getExtras().getString("content");
        displayMessage(context, content, title);
        generateNotification(context, content, title);
    }

    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(Context context, int total) {
    }

    /**
     * Method called on Error
     */
    @Override
    public void onError(Context context, String errorId) {
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
    private static void generateNotification(Context context, String message,
                                             String title) {
        PreferenceHelper prefs = new PreferenceHelper(context);

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new Notification(icon, message, when);

        Intent notificationIntent = new Intent(context, MainView.class);
        notificationIntent.putExtra("contact", title);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        if (prefs.getBoolean("notificationSound")) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }

        // Vibrate if vibrate is enabled
        if (prefs.getBoolean("notificationVibrate")) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        // Sound
        // notification.sound = Uri.parse("android.resource://" +
        // context.getPackageName() + "your_sound_file_name.mp3");

        notificationManager.notify(0, notification);

    }
}
