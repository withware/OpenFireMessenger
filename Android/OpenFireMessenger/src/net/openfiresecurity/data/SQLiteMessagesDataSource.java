/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteMessagesDataSource {

    // Database fields

    private SQLiteDatabase database;
    private MySQLiteMessagesHelper dbHelper;

    private String[] allColumns = {MySQLiteMessagesHelper.COLUMN_ID,
            MySQLiteMessagesHelper.COLUMN_CONTENT,
            MySQLiteMessagesHelper.COLUMN_EMAIL};

    public SQLiteMessagesDataSource(Context context) {
        dbHelper = new MySQLiteMessagesHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public int getId(String content, String email) {
        Cursor c = database.rawQuery("SELECT _id FROM "
                + MySQLiteMessagesHelper.TABLE_MESSAGES
                + " WHERE TRIM(content) = '" + content.trim()
                + "' AND TRIM(email) = '" + email.trim() + "';", null);
        c.moveToFirst();
        int id = c.getInt(c.getColumnIndex("_id"));
        c.close();
        return id;
    }

    public String getEmail(int id) {
        Cursor c = database.rawQuery("SELECT email FROM "
                + MySQLiteMessagesHelper.TABLE_MESSAGES
                + " WHERE TRIM(_id) = '" + id + "'", null);
        c.moveToFirst();

        String email = c.getString(c.getColumnIndex("email"));
        c.close();
        return email;
    }

    public String getContent(int id) {
        Cursor c = database.rawQuery("SELECT name FROM "
                + MySQLiteMessagesHelper.TABLE_MESSAGES
                + " WHERE TRIM(_id) = '" + id + "'", null);
        c.moveToFirst();

        String name = c.getString(c.getColumnIndex("content"));
        c.close();
        return name;
    }

    public SQLiteMessages createMessage(String content, String email) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteMessagesHelper.COLUMN_CONTENT, content);
        values.put(MySQLiteMessagesHelper.COLUMN_EMAIL, email);
        long insertId = database.insert(MySQLiteMessagesHelper.TABLE_MESSAGES,
                null, values);
        Cursor cursor = database.query(MySQLiteMessagesHelper.TABLE_MESSAGES,
                allColumns,
                MySQLiteMessagesHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();

        SQLiteMessages newContact = cursorToContacts(cursor);
        cursor.close();
        return newContact;
    }

    public void deleteMessage(SQLiteMessages message) {
        long id = message.getId();
        database.delete(MySQLiteMessagesHelper.TABLE_MESSAGES,
                MySQLiteMessagesHelper.COLUMN_ID + " = " + id, null);
    }

    public List<SQLiteMessages> getAllMessages(String email) {

        List<SQLiteMessages> messages = new ArrayList<SQLiteMessages>();

        Cursor cursor = database.query(MySQLiteMessagesHelper.TABLE_MESSAGES,
                allColumns, "email like '" + email + "'", null, null, null,
                null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            SQLiteMessages message = cursorToContacts(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return messages;
    }

    private SQLiteMessages cursorToContacts(Cursor cursor) {

        SQLiteMessages message = new SQLiteMessages();
        message.setId(cursor.getLong(0));
        message.setContent(cursor.getString(1));
        message.setEmail(cursor.getString(2));
        return message;
    }

}
