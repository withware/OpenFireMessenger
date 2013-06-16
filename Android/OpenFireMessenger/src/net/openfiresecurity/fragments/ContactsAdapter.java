/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.openfiresecurity.messenger.MainService;
import net.openfiresecurity.messenger.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private String[] names, emails;

    private List<String> namesList = new ArrayList<String>();
    private List<String> emailsList = new ArrayList<String>();

    public ContactsAdapter(Context context, String[] ids) {
        super(context, R.layout.list_contact, ids);
        this.context = context;
        for (String s : ids) {
            namesList.add(MainService.contactsDB.getName(Integer.parseInt(s)));
            emailsList
                    .add(MainService.contactsDB.getEmail(Integer.parseInt(s)));
        }
        names = namesList.toArray(new String[namesList.size()]);
        emails = emailsList.toArray(new String[emailsList.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_contact, parent, false);

        TextView contactName = (TextView) rowView
                .findViewById(R.id.tvContactName);

        TextView contactLastMessage = (TextView) rowView
                .findViewById(R.id.tvLastMessage);

        @SuppressWarnings("unused")
        ImageView contactPicture = (ImageView) rowView
                .findViewById(R.id.ivContactPicture);
        contactName.setText(names[position]);
        contactLastMessage.setText(emails[position]);
        // Change the icon for Windows and iPhone
        // String s = values[position];
        // if (s.startsWith("iPhone")) {
        // contactPicture.setImageResource(R.drawable.no);
        // } else {
        // contactPicture.setImageResource(R.drawable.ok);
        // }

        return rowView;
    }

    public void updateContacts(String[] ids) {
        namesList.clear();
        emailsList.clear();
        for (String s : ids) {
            namesList.add(MainService.contactsDB.getName(Integer.parseInt(s)));
            emailsList
                    .add(MainService.contactsDB.getEmail(Integer.parseInt(s)));
        }
        names = namesList.toArray(new String[namesList.size()]);
        emails = emailsList.toArray(new String[emailsList.size()]);
        notifyDataSetChanged();
    }
}
