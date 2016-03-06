package org.telegram.android;


import android.app.Activity;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.Components.AvatarUpdater;



/*

 * 알림창을 클릭하면 실행되는 액티비티

 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class NotiActivity  extends  Activity  {

    //알림 매니저 객체

    NotificationManager notiManager;

    public static AvatarUpdater avatarUpdater = new AvatarUpdater();

    @Override

    //protected void onCreate(Bundle savedInstanceState) {

    //   super.onCreate(savedInstanceState);

    //화면 구성하기

    //   setContentView(R.layout.noti);

    //알림 매니저 객체 얻어오기

    //notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

    //알림 제거하기

    //notiManager.cancel(MainActivity.MyNoti);



    // }

    /** Called when the activity is first created. */

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noti);

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekbar);
        final TextView seekBarValue = (TextView)findViewById(R.id.seekbarvalue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText(String.valueOf(progress));
                //값이 변할때의 동작을 설명하기


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    //버튼을 눌렀을때 액티비티 종료하기 위해


    public void push(View v){


        switch(v.getId()) {

            case R.id.rdg001:
                Log.i("", "push button");

                break;

        }
        finish();

    }



}



