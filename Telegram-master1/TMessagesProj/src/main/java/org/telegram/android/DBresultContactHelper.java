package org.telegram.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.ui.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 411A on 2015-02-23.
 */
public class DBresultContactHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "UseContactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DAY = "day";
    private static final String KEY_CALLR = "callr";
    private static final String KEY_CALLS = "calls";
    private static final String KEY_SMSR = "smsr";
    private static final String KEY_SMSS = "smss";
    private static final String KEY_MSGR = "msgr";
    private static final String KEY_MSGS = "msgs";
    private static final String KEY_USE = "use";
    private static final String KEY_EMOJI = "emoji";
    private static final String KEY_EMONUM = "emonum";

    public DBresultContactHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MONTH + " TEXT," + KEY_DAY + " TEXT," +KEY_CALLR + " TEXT," + KEY_CALLS+ " TEXT,"+KEY_SMSR + " TEXT," +KEY_SMSS + " TEXT," + KEY_MSGR + " TEXT," +KEY_MSGS + " TEXT," +KEY_USE + " TEXT," +KEY_EMOJI+ " TEXT," + KEY_EMONUM+" TEXT"+ ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        // Create tables again
        onCreate(db);
    }

    // 새로운 contact 추가
    public void addContact(resultContact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MONTH, contact.getMonth()); // Name 필드명
        values.put(KEY_DAY, contact.getDay()); // Name 필드명
        values.put(KEY_CALLR, contact.getcallr());
        values.put(KEY_CALLS, contact.getcalls());
        values.put(KEY_SMSR, contact.getsmsr());
        values.put(KEY_SMSS, contact.getsmss());
        values.put(KEY_MSGR, contact.getmsgr());
        values.put(KEY_MSGS, contact.getmsgs());
        values.put(KEY_USE, contact.getUse());
        values.put(KEY_EMOJI, contact.getEmoji());
        values.put(KEY_EMONUM, contact.getEmonum());

        // 새로운 Row 추가
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // 연결종료
    }

    // 아이디에 해당하는 contact 가져오기
    public resultContact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{KEY_ID, KEY_MONTH,KEY_DAY,KEY_CALLR, KEY_CALLS,KEY_SMSR ,KEY_SMSS,KEY_MSGR,KEY_MSGS ,KEY_USE,KEY_EMOJI,KEY_EMONUM},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        resultContact contact = new resultContact(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),Integer.parseInt(cursor.getString(2)), Integer.parseInt(cursor.getString(3)),Integer.parseInt(cursor.getString(4)),Integer.parseInt(cursor.getString(5)),Integer.parseInt(cursor.getString(6)),Integer.parseInt(cursor.getString(7)),Integer.parseInt(cursor.getString(8)),cursor.getDouble(9),Integer.parseInt(cursor.getString(10)),cursor.getDouble(11));

        return contact;
    }

    // 모든 contact 리스트 가져오기
    public List<resultContact> getAllContacts() {
        List<resultContact> contactList = new ArrayList<resultContact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                resultContact contact = new resultContact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setMonth(Integer.parseInt(cursor.getString(1)));
                contact.setDay(Integer.parseInt(cursor.getString(2)));
                contact.setcallr(Integer.parseInt(cursor.getString(3)));
                contact.setcalls(Integer.parseInt(cursor.getString(4)));
                contact.setsmsr(Integer.parseInt(cursor.getString(5)));
                contact.setsmss(Integer.parseInt(cursor.getString(6)));
                contact.setmsgr(Integer.parseInt(cursor.getString(7)));
                contact.setmsgs(Integer.parseInt(cursor.getString(8)));
                contact.setUse(cursor.getDouble(9));
                contact.setEmoji(Integer.parseInt(cursor.getString(10)));
                contact.setEmonum(cursor.getDouble(11));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        return contactList;
    }

    // 가져온 contact 숫자 가져오기
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // contact 업데이트
    public int updateContact(resultContact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MONTH, contact.getMonth());
        values.put(KEY_DAY, contact.getDay());
        values.put(KEY_USE, contact.getUse());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
    }

    // contact 삭제하기
    public void deleteContact(resultContact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
        db.close();
    }
    
}
