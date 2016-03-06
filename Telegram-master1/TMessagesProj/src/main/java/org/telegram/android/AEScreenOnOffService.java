package org.telegram.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class AEScreenOnOffService extends Service {
    BroadcastReceiver mReceiver=null;
    Long start = 0L;
    Long sumresult = 0L;
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();

        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean screenOn = false;

        try{
            // Get ON/OFF values sent from receiver ( AEScreenOnOffReceiver.java )
            screenOn = intent.getBooleanExtra("screen_state", false);
        }catch(Exception e){}
        
        if (!screenOn) {
            if(start ==0) {
                start = System.currentTimeMillis() / 1000;
                Log.i("Screen ON", Long.toString(System.currentTimeMillis() / 1000));
            }
            //Toast.makeText(getBaseContext(), "Screen ON ", Toast.LENGTH_SHORT).show();
        } else {
            if(start != 0) {
                Long end = System.currentTimeMillis() / 1000;
                Log.i("Screen OFF", Long.toString(System.currentTimeMillis() / 1000));
                sumresult += end - start;
                Log.i("Screen 켜있는시간", Long.toString(sumresult) + "초");
                start = 0L;
            }
            //Toast.makeText(getBaseContext(), "Screen OFF ", Toast.LENGTH_SHORT).show();
        }
        
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i("ScreenOnOff", "Service  distroy");
        if(mReceiver!=null)
            unregisterReceiver(mReceiver);
    }
}
