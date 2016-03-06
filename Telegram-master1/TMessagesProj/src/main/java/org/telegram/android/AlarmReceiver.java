package org.telegram.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.telegram.messenger.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "12시", Toast.LENGTH_SHORT).show();
        Log.i("rhkdus", "10초마다 발생");
        ChangeEmojiService.dbinputcheck = 1;
    }

}
