/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.messenger;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;

import net.openfiresecurity.data.SQLiteContacts;
import net.openfiresecurity.data.SQLiteContactsDataSource;
import net.openfiresecurity.data.SQLiteMessages;
import net.openfiresecurity.data.SQLiteMessagesDataSource;
import net.openfiresecurity.fragments.Chat;
import net.openfiresecurity.fragments.Contacts;

import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;

public class MainService extends Service {

    /* MainView + Fragments */
    public static MainView main;
    public static Chat chat;
    public static Contacts contacts;
    public static List<Fragment> fragments;

    /* Database */
    public static SQLiteMessagesDataSource messagesDB;
    public static SQLiteContactsDataSource contactsDB;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

		/* Database */
        setupDatabases();

		/* Fragments */
        fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, Contacts.class.getName()));
        fragments.add(Fragment.instantiate(this, Chat.class.getName()));
        Log.d("SERVICE", "Fragments: " + fragments.size());
        contacts = (Contacts) fragments.get(0);
        chat = (Chat) fragments.get(1);

        return START_STICKY;
    }

    /**
     * Opens the Databases
     */
    private void setupDatabases() {
        contactsDB = new SQLiteContactsDataSource(MainService.this);
        contactsDB.open();
        messagesDB = new SQLiteMessagesDataSource(MainService.this);
        messagesDB.open();
    }

    public static void createContact(String email, String name) {
        contactsDB.createContact(email, name);
        contacts.refreshList();
    }

    @SuppressWarnings("deprecation")
    public static void createMessage(String content, String email) {
        messagesDB.createMessage(URLDecoder.decode(content), email);
        chat.refreshList();
    }

    public static List<SQLiteMessages> getAllMessages(String email) {
        return messagesDB.getAllMessages(email);
    }

    public static List<SQLiteContacts> getAllContacts() {
        return contactsDB.getAllContacts();
    }

    public static List<Fragment> getFragments() {
        return MainService.fragments;
    }
}
