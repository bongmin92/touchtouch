package org.telegram.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.telegram.android.DBresultContactHelper;
import org.telegram.messenger.R;

import java.util.List;


public class GraphActivity extends Activity {
    public static int[] emojis;
    public static double[] values =  new double[7] ;
    public static String[] horlabels = new String[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

/*
        int[] values = new int[]{100, 150, 500, 300, 200};
        String[] verlabels = new String[]{"HIGH", "", "LOW"};
        String[] horlabels = new String[]{"평균", "3/9", "3/8", "3/7","3/6"};
        GraphView graphView = new GraphView(this, values,"", horlabels, verlabels, GraphView.BAR);
        setContentView(graphView);
        */



        try {
            List<resultContact> contacts = ChangeEmojiService.usedb.getAllContacts();
            int ic = 0;
            for (resultContact cn : contacts) {
                if(ic <7) {
                    Log.i("graphContact222", Integer.toString(cn.getDay()));

                    horlabels[ic] = Integer.toString(cn.getDay());
                    values[ic] = cn.getUse();

                    ic++;
                }
            }

        } catch (Exception e) {
            Log.i("ERROR", "사용량평균 디비 비었음");
        }

        emojis = new int[]{5,4,2,2,4,3,1};
        //double[] values = new double[]{100, 100, 100, 300, 500, 500, 500};
        String[] verlabels = new String[]{"많음", "적음"};
        //horlabels = new String[]{"9", "10", "11", "12", "13", "14", "15"};
        GraphView graphView = new GraphView(this, values, "", horlabels, verlabels, GraphView.BAR);
        setContentView(graphView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
