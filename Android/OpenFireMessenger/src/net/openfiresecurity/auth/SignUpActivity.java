/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.auth;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.openfiresecurity.messenger.R;

import static net.openfiresecurity.auth.AccountGeneral.sServerAuthenticate;
import static net.openfiresecurity.auth.AuthenticatorActivity.ARG_ACCOUNT_TYPE;
import static net.openfiresecurity.auth.AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT;
import static net.openfiresecurity.auth.AuthenticatorActivity.KEY_ERROR_MESSAGE;
import static net.openfiresecurity.auth.AuthenticatorActivity.PARAM_USER_PASS;

public class SignUpActivity extends Activity {

    private String mAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        setContentView(R.layout.act_register);

        findViewById(R.id.alreadyMember).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        findViewById(R.id.submit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createAccount();
                    }
                });
    }

    private void createAccount() {

        // Validation!
        new AsyncTask<String, Void, Intent>() {

            String name = ((TextView) findViewById(R.id.name)).getText()
                    .toString().trim();
            String accountName = ((TextView) findViewById(R.id.accountName))
                    .getText().toString().trim();
            String accountPassword = ((TextView) findViewById(R.id.accountPassword))
                    .getText().toString().trim();

            @Override
            protected Intent doInBackground(String... params) {

                String authtoken = null;
                Bundle data = new Bundle();
                try {
                    authtoken = sServerAuthenticate.userSignUp(name,
                            accountName, accountPassword);
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE,
                            mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                    data.putString(PARAM_USER_PASS, accountPassword);
                    data.putString("email", accountName);
                    data.putBoolean(ARG_IS_ADDING_NEW_ACCOUNT, true);
                } catch (Exception e) {
                    data.putString(KEY_ERROR_MESSAGE,
                            "Error: " + e.getMessage());
                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    Toast.makeText(getBaseContext(),
                            intent.getStringExtra(KEY_ERROR_MESSAGE),
                            Toast.LENGTH_SHORT).show();
                } else {
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
