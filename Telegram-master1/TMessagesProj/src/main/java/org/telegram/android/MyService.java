package org.telegram.android;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service implements Runnable{
    public MyService() {
    }
    public static final String TAG ="MyService";

    private int count =0;
    private BroadcastReceiver mReceiver;
    public static volatile Context applicationContext = null;

    public void onCreate(){
        super.onCreate();

        Thread myThread = new Thread(this);
        myThread.start();
    }
    public void run(){
        while(true) {
            try {
                Log.i(TAG, "my service called #" + count);
                count++;

                Thread.sleep(5000);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

}
