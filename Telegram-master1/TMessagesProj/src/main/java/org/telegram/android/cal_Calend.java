
package org.telegram.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

import java.util.Calendar;

public class cal_Calend extends Activity {
    public String[] sWeek = {"SUN","MON","TUE","WED","THU","FRI","SAT"};

    Calendar calendar;
    int dayCount;
    static int[] iDate;
    static int[] sltDate  = new int[3];

    public static final int YEAR=0;
    public static final int MONTH=1;
    public static final int I_SDAY=0;
    public static final int I_CDAY=1;
    public static final int I_TODAY=2;

    private LinearLayout layout, layout2;
    private TextView CalendarView;
    private TextView buttonPre, buttonNext;

    private cal_CalView calendarView;

    private int calBackColor;
    private int calTextColor;
    private int calGridLineColor1;
    private int calGridLineColor2;
    private int calSelectColor;

    public static DBresultContactHelper caldb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        calendar = Calendar.getInstance();


        if (sltDate[YEAR] == 0){
            sltDate[YEAR] = calendar.get(Calendar.YEAR);
            sltDate[MONTH] = calendar.get(Calendar.MONTH);
            sltDate[I_TODAY] = calendar.get(Calendar.DATE);
        }

        setDate();

        layout=new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
        setLayout(layout, LinearLayout.HORIZONTAL, params);

        layout2=new LinearLayout(this);
        setLayout(layout2, LinearLayout.VERTICAL , null);

        CalendarView = new TextView(this);
        CalendarView.setTextSize(16);
        LinearLayout.LayoutParams tv2Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        setTextView(CalendarView, sltDate[YEAR]+"."+ (sltDate[MONTH]+1),
                Color.argb(255,0,0,0) , Gravity.CENTER ,tv2Params);


        buttonPre = new TextView(this);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
        setTextView(buttonPre, "<<", Color.argb(255,0,0,0) , Gravity.CENTER ,tvParams);

        buttonPre.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                calenminus();
                setDate();
                CalendarView.setText(""+sltDate[YEAR]+"."+(sltDate[MONTH]+1));
                calendarView.invalidate();
            }
        });

        buttonNext = new TextView(this);
        setTextView(buttonNext, ">>", Color.argb(255,0,0,0) , Gravity.CENTER ,tvParams);

        buttonNext.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                calenplus();
                setDate();
                CalendarView.setText(""+sltDate[YEAR]+"."+(sltDate[MONTH]+1));
                calendarView.invalidate();
            }
        });

        layout.addView(buttonPre);
        layout.addView(CalendarView);
        layout.addView(buttonNext);

        calBackColor = getResources().getColor(R.color.cal_background);
        calTextColor = getResources().getColor(R.color.cal_foreground);
        calGridLineColor1 = getResources().getColor(R.color.cal_hilite);
        calGridLineColor2 = getResources().getColor(R.color.cal_light);
        calSelectColor = getResources().getColor(R.color.cal_selected);

        calendarView = new cal_CalView(this);

        calendarView.setTextSize(6);
        calendarView.setBackColor(calBackColor);
        calendarView.settextColor(calTextColor);
        calendarView.setGridLineColor1(calGridLineColor1);
        calendarView.setGridLineColor2(calGridLineColor2);
        calendarView.setSelectColor(calSelectColor);

        layout2.addView(layout);
        layout2.addView(calendarView);

        setContentView(layout2);

    }

    public void calenplus() {
        sltDate[MONTH]++;
        if ( sltDate[MONTH] == 12) {
            sltDate[MONTH] = 0;
            sltDate[YEAR]++;
        }
    }

    public void calenminus() {
        sltDate[MONTH]--;
        if ( sltDate[MONTH] == -1) {
            sltDate[MONTH] = 11;
            sltDate[YEAR]--;
        }
    }

    public void setDate(){
		/*
		if( sltDate[MONTH] == 12)
			sltDate[MONTH] = 0;
		if ( sltDate[MONTH] == -1)
			sltDate[MONTH] = 11;
		 */
        calendar.set(sltDate[YEAR],sltDate[MONTH],1);
        //calendar.set(sltDate[YEAR],sltDate[MONTH],1);
        iDate = new int[3];

        dayCount=0;
        for( int i =1; i <= calendar.getActualMaximum(Calendar.DATE); i++)
            dayCount++;


        iDate[I_CDAY] = dayCount;

        iDate[I_SDAY] = calendar.get( Calendar.DAY_OF_WEEK ) -1;
        iDate[I_TODAY] = sltDate[I_TODAY];
        //showToast(iDate[I_TODAY] ,""+ calendar.get(Calendar.DATE));
        //showToast(sltDate[YEAR],""+iDate[I_SDAY]);
    }


    protected void showToast(int x, String  y) {
        Toast toast = Toast.makeText(this,x +"    "+ y, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setLayout(LinearLayout layout , int a , LinearLayout.LayoutParams params) {
        layout.setBackgroundColor(getResources().getColor(R.color.cal_background));
        layout.setOrientation(a);
        layout.setGravity(Gravity.CENTER);
    }

    private void setTextView( TextView view, String text, int color, int gravity, LinearLayout.LayoutParams params) {
        view.setText(""+text);
        view.setTextColor(color);
        view.setBackgroundColor(getResources().getColor(R.color.cal_background));
        view.setGravity(gravity);
        view.setLayoutParams(params);
    }

    public int getDateYear() {
        return sltDate[YEAR];
    }

    public int getDateMonth() {
        return sltDate[MONTH]+1;
    }

    public int getDateWeek( int y, int m, int d){
        int temp = 0;
        Calendar calTemp = Calendar.getInstance();
        calTemp.set(y,m,d);

        temp=calTemp.get( Calendar.DAY_OF_WEEK )-1;

        return temp;
    }

    public void startIntent( int year, int mon, int day, int week ) {
        Intent temp = null;

    }

    public String spaceFull(int a) {
        String temp =""+a;
        if(temp.length() < 2)
            temp = "0" + a;
        return temp;
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