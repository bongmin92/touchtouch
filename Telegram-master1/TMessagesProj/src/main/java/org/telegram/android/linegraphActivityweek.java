package org.telegram.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.telegram.messenger.R;

import java.util.List;

public class linegraphActivityweek extends Activity {
    //public static int[] emojis = new int[5];
    public double[] values = new double[5];
    String[] horlabels = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        /*int iv = 0;
        try {
            List<resultContact> resultcontacts = ChangeEmojiService.usedb.getAllContacts();
            int ic = 0;
            for (resultContact cn : resultcontacts) {
                Log.i("resultContact", Integer.toString(cn.getEmoji()));

                horlabels[iv] = Integer.toString(cn.getDay());
                values[iv] = cn.getEmoji();
                iv++;
            }

        } catch (Exception e) {
            Log.i("ERROR", "사용량평균 디비 비었음");
        }*/

        double[] values = new double[]{100, 200, 300, 500, 500};
        String[] verlabels = new String[]{"좋음", "우울"};
        //horlabels = new String[]{"9", "10", "11", "12", "13", "14", "15"};

        horlabels = new String[]{"5주전", "4주전", "3주전", "2주전","지난주"};
        linegraphViewweek graphView = new linegraphViewweek(this, values, "", horlabels, verlabels, linegraphView.BAR);
        setContentView(graphView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
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
