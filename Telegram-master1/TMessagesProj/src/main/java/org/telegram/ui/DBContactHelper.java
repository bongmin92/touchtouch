package org.telegram.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 411A on 2015-02-23.
 */
public class DBContactHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "DBContactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "DBcontact";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TIME = "time";

    public DBContactHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TYPE + " TEXT," + KEY_TIME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        Log.i("database create", "create success");
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
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, contact.getType()); // Name 필드명
        values.put(KEY_TIME, contact.getTime()); // Name 필드명

        // 새로운 Row 추가
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // 연결종료
    }

    // 아이디에 해당하는 contact 가져오기
    public Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{KEY_ID, KEY_TYPE, KEY_TIME},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)), cursor.getLong(2));

        return contact;
    }

    // 모든 contact 리스트 가져오기
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setType(Integer.parseInt(cursor.getString(1)));
                contact.setTime(cursor.getLong(2));
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
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, contact.getType());
        values.put(KEY_TIME, contact.getTime());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
    }

    // contact 삭제하기
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
        db.close();
    }
    
}
