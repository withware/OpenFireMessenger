/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.receivers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.openfiresecurity.data.PreferenceHelper;
import net.openfiresecurity.helper.WakeLocker;
import net.openfiresecurity.messenger.MainService;

import static net.openfiresecurity.messenger.push.CommonUtilities.EXTRA_EMAIL;
import static net.openfiresecurity.messenger.push.CommonUtilities.EXTRA_MESSAGE;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean found = false;

        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(
                    service.service.getClassName())) {
                found = true;
                break;
            }
        }

        if (!found) {
            context.startService(new Intent(context, MainService.class));
        }
        String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
        String email = intent.getExtras().getString(EXTRA_EMAIL);
        boolean wakelock = new PreferenceHelper(context)
                .getBoolean("notificationWakelock");
        if (wakelock) {
            WakeLocker.acquire(context.getApplicationContext());
        }
        MainService.createMessage("--> " + newMessage, email);
        if (wakelock) {
            WakeLocker.release();
        }
    }
}