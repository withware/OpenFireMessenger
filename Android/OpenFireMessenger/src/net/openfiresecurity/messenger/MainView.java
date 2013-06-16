/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import net.openfiresecurity.data.PreferenceHelper;
import net.openfiresecurity.fragments.Chat;
import net.openfiresecurity.fragments.Contacts;
import net.openfiresecurity.fragments.PagerAdapter;
import net.openfiresecurity.helper.AlertDialogManager;
import net.openfiresecurity.helper.Constants;
import net.openfiresecurity.messenger.push.ConnectionDetector;
import net.openfiresecurity.messenger.push.ServerUtilities;

import java.util.HashMap;

import static net.openfiresecurity.messenger.MainService.chat;
import static net.openfiresecurity.messenger.push.CommonUtilities.SENDER_ID;

public class MainView extends FragmentActivity implements
        TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

    private TabHost mTabHost;
    public static ViewPager mViewPager;

    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainView.TabInfo>();
    private PagerAdapter mPagerAdapter;

    private static Context currentInstance;

    PreferenceHelper prefs;

    // Asyntask

    AsyncTask<Void, Void, Void> mRegisterTask;

    // Alert dialog manager

    AlertDialogManager alert = new AlertDialogManager();

    // Accounts
    public static Account account;

    // Connection detector

    ConnectionDetector cd;

    // UPDATE
    private DownloadManager mgr;
    private Request req;

    public static String nameOwn;
    public static String emailOwn;

    private class TabInfo {
        private String tag;
        @SuppressWarnings("unused")
        private Class<?> clss;
        @SuppressWarnings("unused")
        private Bundle args;
        @SuppressWarnings("unused")
        private Fragment fragment;

        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
            clss = clazz;
            this.args = args;
        }

    }

    class TabFactory implements TabContentFactory {

        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {

            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    public static void makeToast(String msg) {
        Toast.makeText(currentInstance, msg, Toast.LENGTH_LONG).show();
    }

    public void displayResult(String res) {
    }

    public void receiveMessages(String res) {
    }

    @Override
    public void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_mainview);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar_default);
        currentInstance = MainView.this;
        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            alert.showAlertDialog(MainView.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
        }
        prefs = new PreferenceHelper(currentInstance);
        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        // Accounts
        account = AccountManager.get(getBaseContext()).getAccountsByType(
                "net.openfiresecurity.messenger")[0];

        registerGCM();

        setupTitleBar();

        initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        intialiseViewPager();

        if (prefs.getBoolean("autoupdate")) {
            new CheckVersion(MainView.this).execute();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("contact")) {
                Log.d("MainView", "Has Extra Contact!");
                changeContact(intent.getExtras().getString("contact").trim());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupTitleBar() {
        // Titlebar Elements

        ImageView ibTitleCancel = (ImageView) findViewById(R.id.ibTitleCancel);

        ImageView ibTitleSettings = (ImageView) findViewById(R.id.ibTitleSettings);
        ibTitleCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ibTitleSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainView.this,
                        net.openfiresecurity.data.Preferences.class));
            }
        });

        ImageView ibTitleAddContact = (ImageView) findViewById(R.id.ibTitleAddContact);
        ibTitleAddContact.setVisibility(View.VISIBLE);
        ibTitleAddContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater li = LayoutInflater.from(MainView.this);

                View promptsView = li
                        .inflate(R.layout.prompt_add_contact, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainView.this);

                alertDialogBuilder.setView(promptsView);

                final EditText userEmail = (EditText) promptsView
                        .findViewById(R.id.etPromptEmail);

                final EditText userName = (EditText) promptsView
                        .findViewById(R.id.etPromptName);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                        MainService.createContact(userEmail
                                                .getText().toString(), userName
                                                .getText().toString());
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                alertDialogBuilder.create().show();
            }
        });
        // END
    }

    private void registerGCM() {

        nameOwn = AccountManager.get(getBaseContext()).getUserData(account,
                "user");
        emailOwn = AccountManager.get(getBaseContext()).getUserData(account,
                "email");

        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        // GCMRegistrar.checkManifest(this);

        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            if (GCMRegistrar.isRegisteredOnServer(this)) {
            } else {

                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        ServerUtilities.register(context, nameOwn, emailOwn,
                                regId);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag());
        super.onSaveInstanceState(outState);
    }

    private void intialiseViewPager() {

        mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(),
                MainService.getFragments());
        mViewPager = (ViewPager) super.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
    }

    private void initialiseTabHost(Bundle args) {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        TabInfo tabInfo = null;
        MainView.AddTab(this, mTabHost, mTabHost.newTabSpec("Contacts")
                .setIndicator("Contacts"), (tabInfo = new TabInfo("Contacts",
                Contacts.class, args)));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        MainView.AddTab(this, mTabHost, mTabHost.newTabSpec("Chat")
                .setIndicator("Chat"), (tabInfo = new TabInfo("Chat",
                Chat.class, args)));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        mTabHost.setOnTabChangedListener(this);

    }

    private static void AddTab(MainView activity, TabHost tabHost,
                               TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    @Override
    public void onTabChanged(String tag) {
        // TabInfo newTab = this.mapTabInfo.get(tag);
        int pos = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(pos);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mTabHost.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /* FRAGMENT CHAT */
    public void changeContact(String email) {
        chat.changeContact(email);
    }

	/* UPDATE */

    private String getVersionNumber() {
        int version = -1;
        try {
            version = getPackageManager().getPackageInfo(
                    "net.openfiresecurity.messenger", 0).versionCode;
        } catch (Exception e) {
        }
        return ("" + version);
    }

    public void update(final String result) {
        try {

            String version = getVersionNumber();
            if (Integer.parseInt(version) < Integer.parseInt(result)) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Update Available!").setMessage(
                        "A new Update is available!\nUpdate now?\n\n(Version Code "
                                + result + ")");
                dialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @SuppressLint("NewApi")
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {

                                String appname = "OpenFireMessenger.apk";
                                req = new Request(Uri.parse(Constants.urls
                                        + appname));

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                }
                                req.setDescription("Updating!");
                                req.setTitle(appname);
                                req.setMimeType("application/vnd.android.package-archive");
                                req.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        appname);

                                mgr.enqueue(req);
                                makeToast("Downloading!");
                            }
                        });
                dialog.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                dialog.show();

            }
        } catch (Exception exc) {
            Log.d("MESSENGER", exc.getLocalizedMessage());
        }
    }
    /* END UPDATE */
}
