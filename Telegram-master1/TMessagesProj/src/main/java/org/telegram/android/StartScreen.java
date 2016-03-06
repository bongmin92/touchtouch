package org.telegram.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class StartScreen extends Activity {

    Handler h;
    Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5, bitmap6;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start_screen);


        bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.sample1);
        FileOutputStream out2 = null;
        try{
            out2 = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/sosad.png");
            bitmap2.compress(Bitmap.CompressFormat.PNG, 50, out2);
            //Toast.makeText(StartScreen.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.sample2);
        out2 = null;
        try{
            out2 = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/sad.png");
            bitmap3.compress(Bitmap.CompressFormat.PNG, 50, out2);
            //Toast.makeText(StartScreen.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.sample3);
        out2 = null;
        try{
            out2 = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/soso.png");
            bitmap4.compress(Bitmap.CompressFormat.PNG, 50, out2);
            //Toast.makeText(StartScreen.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        bitmap5 = BitmapFactory.decodeResource(getResources(), R.drawable.sample4);
        out2 = null;
        try{
            out2 = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/happy.png");
            bitmap5.compress(Bitmap.CompressFormat.PNG, 50, out2);
            //Toast.makeText(StartScreen.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        bitmap6 = BitmapFactory.decodeResource(getResources(), R.drawable.sample5);
        out2 = null;
        try{
            out2 = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/sohappy.png");
            bitmap6.compress(Bitmap.CompressFormat.PNG, 50, out2);
            //Toast.makeText(StartScreen.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        Intent i = new Intent(this, ChangeEmojiService.class);
        this.startService(i);

        Intent in = new Intent(this, NotiService.class);
        this.startService(in);

        //Intent i = new Intent();
        //i.setAction("org.telegram.android.ChangeEmojiService");
        //startService(i);
        
        Intent iscreen = new Intent();
        iscreen.setAction("org.telegram.android.AEScreenOnOffService");
        startService(iscreen);

        h = new Handler();
        h.postDelayed(irun, 4000);
    }

    Runnable irun = new Runnable() {
        public void run() {
            Intent i = new Intent(StartScreen.this, LaunchActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        h.removeCallbacks(irun);
    }
}
