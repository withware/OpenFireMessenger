/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import net.openfiresecurity.data.SQLiteContacts;
import net.openfiresecurity.messenger.MainService;
import net.openfiresecurity.messenger.MainView;

import java.util.ArrayList;
import java.util.List;

public class Contacts extends ListFragment implements OnItemLongClickListener {

    private String[] id, email;
    public List<String> idList, emailList;
    MainView main;
    ContactsAdapter adapter;
    List<SQLiteContacts> allContacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        idList = new ArrayList<String>();
        emailList = new ArrayList<String>();

        getContacts();

        /** Creating our custom adapter to set data in listview */
        adapter = new ContactsAdapter(main, id);

        /** Setting the array adapter to the listview */
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setLongClickable(true);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id1) {
        super.onListItemClick(l, v, position, id1);
        main.changeContact(email[position]);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
        Log.d("Contacts", "Long Click on: " + email[arg2]);
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        main = ((MainView) getActivity());

    }

    /**
     * Refreshes the contacts list
     */
    public void refreshList() {
        getContacts();
        adapter.updateContacts(id);
    }

    private void getContacts() {
        allContacts = MainService.getAllContacts();
        SQLiteContacts[] contacts = allContacts
                .toArray(new SQLiteContacts[allContacts.size()]);
        idList.clear();
        emailList.clear();
        for (SQLiteContacts c : contacts) {
            idList.add(c.getId() + "");
            emailList.add(c.getEmail());
        }
        id = idList.toArray(new String[idList.size()]);
        email = emailList.toArray(new String[emailList.size()]);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}