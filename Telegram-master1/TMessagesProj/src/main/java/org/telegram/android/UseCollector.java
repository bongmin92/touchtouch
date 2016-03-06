package org.telegram.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;
import org.telegram.ui.DBContactEmojiHelper;
import org.telegram.ui.DBContactHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UseCollector extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_collector);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        try {
            List<resultContact> resultcontacts = ChangeEmojiService.usedb.getAllContacts();
            for (resultContact cn : resultcontacts) {
                Log.i("resultContact", Integer.toString(cn.getMonth()) +"//"+ Integer.toString(cn.getDay()) +"//"+ Double.toString(cn.getUse()));
                adapter.add(cn.toString());
            }

        } catch (Exception e) {
            Log.i("ERROR", "사용량평균 디비 비었음");
        }

        // ListView 가져오기
        ListView listView = (ListView) findViewById(R.id.listview);

        // ListView에 각각의 아이템표시를 제어하는 Adapter를 설정
        listView.setAdapter(adapter);

        // xml파일에 있는 버튼을 id값으로 불러와서 사용 , 버튼 텍스트 변경
        Button bt1 = (Button) findViewById(R.id.usebutton);

        Button bt2 = (Button) findViewById(R.id.emotionbutton);
        // 버튼 클릭 이벤트 처리
        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        // 클릭 될 버튼2개의 id값을 스위치구문으로 분기
        switch (v.getId()) {

            // 첫번쨰버튼 눌렷을 때
            case R.id.usebutton:

                //페이지 이동 구문
                Intent intetn1 = new Intent(this, Graphmenu.class);
                startActivity(intetn1);

                break;

            // 두번째 버튼 눌렷을 때
            case R.id.emotionbutton:
                //페이지 이동 구문
                Intent intetn2 = new Intent(this, linegraphmenu.class);
                startActivity(intetn2);

                break;

        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_use_collector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
