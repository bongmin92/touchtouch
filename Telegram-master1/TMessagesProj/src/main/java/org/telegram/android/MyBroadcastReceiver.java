package org.telegram.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.DBContactEmojiHelper;
import org.telegram.ui.DBContactHelper;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static long starttime;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Intent i = new Intent(context, ChangeEmojiService.class);
            context.startService(i);
            //Intent intent=new Intent(this, ChangeEmojiService.class);
            //startService(intent);
            //Intent i = new Intent(this, ChangeEmojiService.class);
            //this.startService(i);
            //Log.i("call history", Long.toString(starttime));
        }
    }
}
