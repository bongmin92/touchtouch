package org.telegram.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import android.util.Log;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Contact;
import org.telegram.ui.Contactemoji;
import org.telegram.ui.DBContactEmojiHelper;
import org.telegram.ui.DBContactHelper;
import org.telegram.ui.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChangeEmojiService extends Service implements Runnable {

    public static final String TAG = "MyService";
    private int count = 0;
    public static final String MESSAGE_TYPE_INBOX = "1";
    public static final String MESSAGE_TYPE_SENT = "2";
    public static final String MESSAGE_TYPE_CONVERSATIONS = "3";
    long callduration;
    int calltakecount, callsendcount, sendmcount, receivemcount, happycount, sadcount, sentmm, receivemm;
    int bigcalltakecount, bigcallsendcount, bigsendmcount, bigreceivemcount, bigsentmm, bigreceivemm;
    int bigcallsend2count, bigsend2mcount, bigsent2mm;
    double result, comptresult, bigresult, oldresultresult;
    public static DBContactHelper db;
    public static DBContactEmojiHelper dbemoji;
    public static DBresultContactHelper usedb;
    public static AvatarUpdater avatarUpdater = new AvatarUpdater();
    public static int dbinputcheck = 0;

    public void onCreate() {
        Log.d(TAG, "Service Create");
        unregisterRestartAlarm(); //등록된 알람은 제거
        super.onCreate();

        dbemoji = new DBContactEmojiHelper(ApplicationLoader.applicationContext);
        db = new DBContactHelper(ApplicationLoader.applicationContext);
        usedb = new DBresultContactHelper(ApplicationLoader.applicationContext);
        new SettingsActivity().onFragmentCreate();

        Thread myThread = new Thread(this);
        myThread.start();



        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
    }

    @Override
    public void onDestroy() {
// TODO Auto-generated method stub
        Log.d(TAG, "Service Destroy");
        registerRestartAlarm(); // 서비스가 죽을 때 알람 등록
        super.onDestroy();
    }

    void registerRestartAlarm() {
        Log.d(TAG, "registerRestartAlarm");
        Intent intent = new Intent(ChangeEmojiService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(ChangeEmojiService.this, 0, intent, 0); // 브로드케스트할 Intent
        long firstTime = SystemClock.elapsedRealtime(); // 현재 시간
        firstTime += 1 * 1000; // 10초 후에 알람이벤트 발생
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE); // 알람 서비스 등록
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
                10 * 1000, sender); // 알람이
    }

    void unregisterRestartAlarm() {
        Log.d(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(ChangeEmojiService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(ChangeEmojiService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    public void run() {
        while (true) {
            try {
                //Log.i(TAG, "my service called #" + count);
                count++;
                calltakecount = 0;
                callsendcount = 0;
                callduration = 0;
                receivemm = 0;
                sentmm = 0;
                sendmcount = 0;
                receivemcount = 0;

                bigcalltakecount = 0;
                bigcallsendcount = 0;
                bigreceivemm = 0;
                bigsentmm = 0;
                bigsendmcount = 0;
                bigreceivemcount = 0;


                bigcallsend2count = 0;
                bigsent2mm = 0;
                bigsend2mcount = 0;

                happycount = 0;
                sadcount = 0;

                callLog();
                SMSList();
                //24시간이내의 수신전화횟수 :calltakecount
                //24시간이내의 발신전화횟수 :callsendcount
                //총 통화시간 : callduration "초"

                try {
                    List<Contact> sendcontacts = db.getAllContacts();

                    for (Contact cn : sendcontacts) {
                        if (System.currentTimeMillis() / 1000 - cn.getTime() <= 10800 * 8) { //24시간
                            if (System.currentTimeMillis() / 1000 - cn.getTime() > 10800) { //4시간~ 24시간(20시간)
                                String log = "Id: " + cn.getID() + "type: " + cn.getType() + " ,Name: " + cn.getTime();
                                Log.d("Name: ", log);
                                if (cn.getType() == 1) {
                                    sendmcount++; //발신 sns수
                                } else
                                    receivemcount++; //수신 sns수
                            } else { //4시간 이내
                                String log = "Id: " + cn.getID() + "type: " + cn.getType() + " ,Name: " + cn.getTime();
                                Log.d("Name: ", log);
                                if (cn.getType() == 1)
                                    if (System.currentTimeMillis() / 1000 - cn.getTime() >= 3600 * 2) { //3시간 발신따져서 잠인지아닌지
                                        bigsendmcount++; //발신 sns수
                                    } else {
                                        bigsend2mcount++;
                                    }
                                else
                                    bigreceivemcount++; //수신 sns수
                            }
                        } else
                            break;
                    }
                } catch (Exception e) {
                    Log.i("ERROR", "메시지디비 비었음");
                }

                try {
                    List<Contactemoji> emojicontact = dbemoji.getAllContacts();
                    for (Contactemoji cn : emojicontact) {
                        if (System.currentTimeMillis() / 1000 - cn.getTime() <= 7200) { //2시간
                            String log = "Id: " + cn.getID() + "type: " + cn.getSymbol() + " ,Name: " + cn.getTime();
                            Log.d("Name: ", log);
                            if (cn.getSymbol().equals("3627933188") || cn.getSymbol().equals("3627933187") || cn.getSymbol().equals("3627933184") || cn.getSymbol().equals("3627933194") || cn.getSymbol().equals("9786") || cn.getSymbol().equals("3627933193") || cn.getSymbol().equals("3627933197") || cn.getSymbol().equals("3627933208"))
                                happycount++; //행복이모티콘 갯수
                            else if (cn.getSymbol().equals("3627933202") || cn.getSymbol().equals("3627933214") || cn.getSymbol().equals("3627933219") || cn.getSymbol().equals("3627933218") || cn.getSymbol().equals("3627933229"))
                                sadcount++; //슬픈이모티콘 갯수
                        } else
                            break;
                    }
                } catch (Exception e) {
                    Log.i("ERROR", "이모티콘디비 비었음");
                }
                Log.i("emojichange", "수신전화" + calltakecount + "발신전화" + callsendcount + " //총 통화" + callduration);
                Log.i("emojichange", "수신메시지" + receivemm + "발신메시지" + sentmm);
                Log.i("emojichange", "수신sns" + receivemcount + "발신sns" + sendmcount + "행복이모티수" + happycount + "슬픈이모티수" + sadcount);

                Log.i("emojichange", "수신전화" + bigcalltakecount + "발신전화" + Integer.toString(bigcallsendcount + bigcallsend2count));
                Log.i("emojichange", "수신메시지" + bigreceivemm + "발신메시지" + Integer.toString(bigsentmm + bigsent2mm));
                Log.i("emojichange", "수신sns" + bigreceivemcount + "발신sns" + Integer.toString(bigsendmcount + bigsend2mcount));

                result = (receivemm * 0.7 + sentmm * 1.3) + (receivemcount + sendmcount) + (calltakecount + callsendcount) * 1.2;
                bigresult = (bigreceivemm * 0.7 + (bigsentmm + bigsent2mm) * 1.3) + (bigreceivemcount + (bigsendmcount + bigsend2mcount)) + (bigcalltakecount + bigcallsendcount + bigcallsend2count) * 1.2;

                Log.i("result", "3시간사용량" + Double.toString(bigresult) + "// 21시간 사용량" + Double.toString(result));
                comptresult = bigresult * 1.3 + result * 0.7;

                setoldresult();

                if (dbinputcheck == 1)
                    registerUse(bigresult + result);

                changeavatar(comptresult, count);
                Thread.sleep(660000);

            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    public void registerUse(double use) {

        final Calendar c = Calendar.getInstance();
        int Month = c.get(Calendar.MONTH);
        int Day = c.get(Calendar.DAY_OF_MONTH);

        //usedb.addContact(new resultContact(Month, Day,  use));
        dbinputcheck = 0;
    }

    public void setoldresult() {

        double sumUse = 0;
        int i = 0;
        try {
            List<resultContact> resultcontacts = usedb.getAllContacts();
            for (resultContact cn : resultcontacts) {
                Log.i("resultContact", Integer.toString(cn.getMonth()) +"//"+ Integer.toString(cn.getDay()) +"//"+ Double.toString(cn.getUse()));
                if (i <= 7) { //2시간
                    sumUse += cn.getUse();
                    //if(cn.getUse() >0)
                    i++;
                } else
                    break;
            }
            oldresultresult = sumUse / i;

        } catch (Exception e) {
            Log.i("ERROR", "사용량평균 디비 비었음");
        }
        if (i== 0) ;
        oldresultresult = 10;
    }

    public void changeavatar(double comptresult, int count) {

        //oldresultresult = (oldresultresult * count + comptresult) / (count + 1);
        Log.i("result", "평균" + Double.toString(oldresultresult) + "현재사용량 " + Double.toString(comptresult));
        Bitmap bitmap = null;
        int sleepcount = bigsentmm + bigsent2mm + bigsendmcount + bigsend2mcount + bigcallsendcount + bigcallsend2count + bigcalltakecount;
        //Log.i("sleep", Integer.toString(sleepcount));
        if (sleepcount == 0) {
            //bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/sleep.png", null, 1280, 1280);
            Log.i("EMOJI", "잠");
        } else {

            double tempcomptresult = comptresult;
            if (happycount > bigsend2mcount * 0.2)
                comptresult += 1.5 * tempcomptresult;
            if (sadcount > bigsend2mcount * 0.2)
                comptresult -= 1.5 * tempcomptresult;
            //comptresult = comptresult + happycount * 0.1 * comptresult - sadcount * 0.1 * comptresult;

            if (comptresult < oldresultresult * 0.8) {
                bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/sosad.png", null, 1280, 1280);
                Log.i("EMOJI", "기분 많이 안좋아");
            } else if (oldresultresult * 0.8 <= comptresult && oldresultresult * 0.9 >= comptresult) {
                bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/sad.png", null, 1280, 1280);
                Log.i("EMOJI", "기분 안좋아");
            } else if (oldresultresult * 0.9 <= comptresult && oldresultresult * 1.1 >= comptresult) {
                bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/soso.png", null, 1280, 1280);
                Log.i("EMOJI", "기분 걍그래");
            } else if (oldresultresult * 1.1 <= comptresult && oldresultresult * 1.2 >= comptresult) {
                bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/happy.png", null, 1280, 1280);
                Log.i("EMOJI", "기분 행복해");
            } else if (oldresultresult * 1.2 <= comptresult) {
                bitmap = ImageLoader.loadBitmap(Environment.getExternalStorageDirectory().getPath() + "/sohappy.png", null, 1280, 1280);
                Log.i("EMOJI", "기분 많이 행복해");
            }
        }

        avatarUpdater.processBitmap(bitmap);
        Log.i("emojichange", "이모티콘바꿍?");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int callLog() {
        int count = 0;
        long duration = 0;
        String callname = "";
        String calltype = "";
        String calllog = "";
        Cursor curCallLog = getCallHistoryCursor(this);
        //Log.i(TAG, "processSend() - 1");
        //Log.i( TAG , "curCallLog: " + curCallLog.getCount());
        if (curCallLog.moveToFirst() && curCallLog.getCount() > 0) {
            while (curCallLog.isAfterLast() == false) {
                StringBuffer sb = new StringBuffer();

                long calldate = curCallLog.getLong(curCallLog.getColumnIndex(CallLog.Calls.DATE));
                long a = calldate / 1000;
                long b = System.currentTimeMillis() / 1000;
                //Log.i( TAG ,"현재시간" +Long.toString(b-a));
                //Log.i("call history", Long.toString(a) + Long.toString(b));
                if (b - a <= 10800 * 8) {
                    if (b - a > 10800) {
                        if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_INBOX)) {
                            calltype = "수신";
                            calltakecount++;
                        } else if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_SENT)) {
                            calltype = "발신";
                            callsendcount++;
                        } else if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_CONVERSATIONS)) {
                            calltype = "부재중";
                        }
                        if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.CACHED_NAME)) == null) {
                            callname = "NoName";
                        } else {
                            callname = curCallLog.getString(curCallLog
                                    .getColumnIndex(CallLog.Calls.CACHED_NAME));
                        }

                        sb.append(timeToString(calldate));
                        sb.append("\t").append(calltype);
                        sb.append("\t").append(callname);
                        sb.append("\t").append(curCallLog.getString(curCallLog.getColumnIndex(CallLog.Calls.NUMBER)));
                        duration = curCallLog.getLong(curCallLog.getColumnIndex(CallLog.Calls.DURATION));
                        sb.append(", duration=").append(Long.toString(duration));

                        //Log.i("call history", sb.toString());
                        callduration += duration;
                        count++;
                        //Log.i("callcatch", Long.toString(callduration) + "초");
                    } else {
                        if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_INBOX)) {
                            calltype = "수신";
                            bigcalltakecount++;
                        } else if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_SENT)) {
                            calltype = "발신";
                            //if (b - a >= 10800) {
                            bigcallsendcount++;
                            //} else {
                            //    bigcallsend2count++;
                            //}
                        } else if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.TYPE)).equals(MESSAGE_TYPE_CONVERSATIONS)) {
                            calltype = "부재중";
                        }
                        if (curCallLog.getString(curCallLog
                                .getColumnIndex(CallLog.Calls.CACHED_NAME)) == null) {
                            callname = "NoName";
                        } else {
                            callname = curCallLog.getString(curCallLog
                                    .getColumnIndex(CallLog.Calls.CACHED_NAME));
                        }

                        sb.append(timeToString(calldate));
                        sb.append("\t").append(calltype);
                        sb.append("\t").append(callname);
                        sb.append("\t").append(curCallLog.getString(curCallLog.getColumnIndex(CallLog.Calls.NUMBER)));
                        duration = curCallLog.getLong(curCallLog.getColumnIndex(CallLog.Calls.DURATION));
                        sb.append(", duration=").append(Long.toString(duration));

                        //Log.i("call history", sb.toString());
                        callduration += duration; //24시간이내의 통화시간
                        count++;
                        //Log.i("callcatch", Long.toString(callduration) + "초");
                    }
                } else
                    break;
                curCallLog.moveToNext();
            }
        }
        curCallLog.close();
        return count;
    }

    private String timeToString(Long time) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleFormat.format(new Date(time));
        return date;
    }

    public int SMSList() {
        Uri allMessage = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(allMessage, null, null, null, null);
        //cur = this.getContentResolver().query(allMessage, null, null, null, null);
        //count = cur.getCount();
        int count = 0;
        //Log.i(TAG, "SMS count = " + count);
        String row = "";
        String msg = "";
        String date = "";
        String protocol = "";

        while (cur.moveToNext()) {
            row = cur.getString(cur.getColumnIndex("address"));
            msg = cur.getString(cur.getColumnIndex("body"));
            date = cur.getString(cur.getColumnIndex("date"));
            long a = Long.parseLong(date) / 1000;
            long b = System.currentTimeMillis() / 1000;
            //Log.i( TAG ,"현재시간" +Long.toString(b-a));
            //Log.i( TAG ,Long.toString(a) + System.currentTimeMillis());
            //Log.i(TAG, "SMS Phone: " + row + " / Mesg: " + msg  + " / Date: " + date);
            if (b - a <= 10800 * 8) {
                if (b - a > 10800) {
                    protocol = cur.getString(cur.getColumnIndex("protocol"));
                    String type = "";
                    if (protocol == MESSAGE_TYPE_SENT) {
                        type = "sent";
                    } else if (protocol == MESSAGE_TYPE_INBOX) {
                        type = "receive";
                    } else if (protocol == MESSAGE_TYPE_CONVERSATIONS)
                        type = "conversations";
                    else if (protocol == null) {
                        type = "send";
                        sentmm++;
                    } else
                        receivemm++;

                    Log.i(TAG, "SMS Phone: " + row + " / Mesg: " + msg + " / Type: " + type + " / Date: " + date);
                    count++;
                } else {
                    protocol = cur.getString(cur.getColumnIndex("protocol"));
                    String type = "";
                    if (protocol == MESSAGE_TYPE_SENT) {
                        type = "sent";
                    } else if (protocol == MESSAGE_TYPE_INBOX) {
                        type = "receive";
                    } else if (protocol == MESSAGE_TYPE_CONVERSATIONS)
                        type = "conversations";
                    else if (protocol == null) {
                        type = "send";
                        //if (b - a >= 10800) {
                        bigsentmm++;
                        //} else {
                        //   bigsent2mm++;
                        //}
                    } else
                        bigreceivemm++;

                    //Log.i(TAG, "SMS Phone: " + row + " / Mesg: " + msg + " / Type: " + type + " / Date: " + date);
                    count++;
                }
            } else
                break;
        }
        cur.close();
        return count;
    }

    private Cursor getCallHistoryCursor(Context context) {
        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI, CALL_PROJECTION,
                null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        return cursor;
    }

    final static private String[] CALL_PROJECTION = {CallLog.Calls.TYPE,
            CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER,
            CallLog.Calls.DATE, CallLog.Calls.DURATION};
}
