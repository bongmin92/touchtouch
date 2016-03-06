package org.telegram.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Contact;
import org.telegram.ui.Contactemoji;
import org.telegram.ui.DBContactEmojiHelper;
import org.telegram.ui.DBContactHelper;
import org.telegram.ui.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotiService extends Service implements Runnable {

    NotificationManager notiManager;
    Vibrator vibrator;
    final static int MyNoti = 0;

    public void onCreate() {

        super.onCreate();

        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Thread myThread = new Thread(this);
        myThread.start();


    }

    public void run() {
        while(true) {
            try {
                Log.i("popup", "알림이 옵니다");
                //  handler.sendEmptyMessageDelayed(0, 0);
                Thread.sleep(900000); //1000초
            } catch (Exception ex) {
                Log.e("popup", ex.toString());
            }
        }
        //Toast.makeText(this, "5초후에 알림이 올 예정입니다", 0).show();
    }

    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            //notification 객체 생성(상단바에 보여질 아이콘, 메세지, 도착시간 정의)

            Notification noti = new Notification(R.drawable.logo1, "", System.currentTimeMillis());  //간단 메세지 //알림창에 띄울 아이콘 //도착시간

            noti.defaults = Notification.DEFAULT_SOUND;

            noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;   //알림 소리를 한번만 내도록
            noti.flags = Notification.FLAG_AUTO_CANCEL;  //확인하면 자동으로 알림이 제거 되도록

            Intent intent = new Intent(NotiService.this, NotiActivity.class);   //사용자가 알람을 확인하고 클릭했을때 새로운 액티비티를 시작할 인텐트 객체
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    //새로운 태스크(Task) 상에서 실행되도록(보통은 태스크1에 쌓이지만 태스크2를 만들어서 전혀 다른 실행으로 관리한다)

            PendingIntent pendingI = PendingIntent.getActivity(NotiService.this, 0, intent, 0);  //인텐트 객체를 포장해서 전달할 인텐트 전달자 객체
            noti.setLatestEventInfo(NotiService.this, "[TouchTouch]", "당신의 기분을 선택해 주세요", pendingI);     //상단바를 드래그 했을때 보여질 내용 정의하기

            notiManager.notify(MyNoti, noti);     //알림창 띄우기(알림이 여러개일수도 있으니 알림을 구별할 상수값, 여러개라면 상수값을 달리 줘야 한다.)
            vibrator.vibrate(500); //0.5초 동안 진동
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
