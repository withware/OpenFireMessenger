/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.openfiresecurity.data.PreferenceHelper;
import net.openfiresecurity.data.SQLiteMessages;
import net.openfiresecurity.helper.MessageSender;
import net.openfiresecurity.messenger.MainService;
import net.openfiresecurity.messenger.MainView;
import net.openfiresecurity.messenger.R;

import java.util.ArrayList;
import java.util.List;

public class Chat extends Fragment implements OnClickListener,
        OnLongClickListener {

    /* Delaying sending for 2 seconds */
    private Handler mHandler = new Handler();
    boolean canSend = true;

    static TextView tvChatUser;
    private String email = "";

    EditText etChatMessage;
    ImageView ivChatSend;
    ListView lvChatMessages;
    Activity context;
    PreferenceHelper prefs;
    ArrayAdapter<String> adapter1;
    List<String> msg = new ArrayList<String>();

    public void changeContact(String email) {
        this.email = email;
        tvChatUser.setText(this.email);
        MainView.mViewPager.setCurrentItem(1);
        refreshList();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        etChatMessage = (EditText) view.findViewById(R.id.etChatMessage);
        etChatMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (canSend) {
                    if (!content.trim().isEmpty()) {
                        ivChatSend.setVisibility(View.VISIBLE);
                    } else {
                        ivChatSend.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        });

        tvChatUser = (TextView) view.findViewById(R.id.tvChatUser);
        ivChatSend = (ImageView) view.findViewById(R.id.ivChatSendEnabled);
        ivChatSend.setVisibility(View.INVISIBLE);
        ivChatSend.setOnClickListener(this);
        ivChatSend.setOnLongClickListener(this);
        lvChatMessages = (ListView) view.findViewById(R.id.lvChatMessages);
        adapter1 = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, msg);
        lvChatMessages.setAdapter(adapter1);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        prefs = new PreferenceHelper(context);
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.ivChatSendEnabled:
                if (!(email.trim().isEmpty())) {
                    ivChatSend.setVisibility(View.INVISIBLE);
                    canSend = false;
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.postDelayed(mUpdateTimeTask, 2000);

                    String message = etChatMessage.getText().toString();
                    MainService.createMessage("<-- " + message, email);
                    new MessageSender().execute(
                            AccountManager.get(context).getUserData(
                                    MainView.account, "email"),
                            email,
                            message,
                            AccountManager.get(context).getUserData(
                                    MainView.account, "hash"));
                    etChatMessage.setText("");
                } else {
                    MainView.mViewPager.setCurrentItem(0);
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            canSend = true;
            if (!etChatMessage.getText().toString().trim().isEmpty()) {
                ivChatSend.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * Refreshes the Chat Window
     */
    public void refreshList() {
        msg.clear();
        List<SQLiteMessages> allMessages = MainService.getAllMessages(email);
        SQLiteMessages[] msg1 = allMessages
                .toArray(new SQLiteMessages[allMessages.size()]);
        for (SQLiteMessages msg2 : msg1) {
            msg.add(msg2.getContent());
        }
        adapter1.notifyDataSetChanged();
    }
}