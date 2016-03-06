/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ApplicationLoader;

public class ScreenReceiver extends BroadcastReceiver {
    private boolean screenOff;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            FileLog.e("tmessages", "screen off");
            ConnectionsManager.getInstance().setAppPaused(true, true);
            ApplicationLoader.isScreenOn = false;
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            FileLog.e("tmessages", "screen on");
            ConnectionsManager.getInstance().setAppPaused(false, true);
            ApplicationLoader.isScreenOn = true;
            screenOff = false;
        }
        Intent i = new Intent(context, AEScreenOnOffService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }
}
