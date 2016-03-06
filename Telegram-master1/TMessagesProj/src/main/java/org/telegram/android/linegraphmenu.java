package org.telegram.android;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import org.telegram.messenger.R;

public class linegraphmenu extends ActivityGroup {

    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linegraphmenu);

        tabHost = (TabHost)findViewById(R.id.tabHost);

        // TabHost 를 findViewById 로 생성한 후 Tab 추가 전에 꼭 실행
        tabHost.setup(getLocalActivityManager());

        tabHost.addTab(tabHost.newTabSpec("Tab_RESIZE")
                .setIndicator("7일")
                .setContent(new Intent(this, linegraphActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("Tab_ROTATE")
                .setIndicator("보름")
                .setContent(new Intent(this, linegraphActivity15.class)));

        tabHost.addTab(tabHost.newTabSpec("Tab_WATERMARK")
                .setIndicator("5주")
                .setContent(new Intent(this, linegraphActivityweek.class)));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_linegraphmenu, menu);
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
