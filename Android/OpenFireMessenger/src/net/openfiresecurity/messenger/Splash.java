/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.messenger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import net.openfiresecurity.auth.AuthenticatorActivity;

public class Splash extends Activity {

    // Accounts
    public Account[] accounts;

    int REQ = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        startService(new Intent(Splash.this, MainService.class));

        accounts = AccountManager.get(getBaseContext()).getAccountsByType(
                "net.openfiresecurity.messenger");
        if (accounts.length > 0) {
            if (AccountManager.get(Splash.this)
                    .getUserData(accounts[0], "hash").isEmpty()) {
                startActivityForResult(new Intent(Splash.this,
                        AuthenticatorActivity.class), REQ);
            }
            Intent i = new Intent(Splash.this, MainView.class);
            startActivity(i);
            finish();
        } else {
            startActivityForResult(new Intent(Splash.this,
                    AuthenticatorActivity.class), REQ);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == REQ) && (resultCode == RESULT_OK)) {
            accounts = AccountManager.get(getBaseContext()).getAccountsByType(
                    "net.openfiresecurity.messenger");
            Intent i = new Intent(Splash.this, MainView.class);
            startActivity(i);
        }
        finish();
    }
}