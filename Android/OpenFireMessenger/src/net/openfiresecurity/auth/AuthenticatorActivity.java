/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.openfiresecurity.data.PreferenceHelper;
import net.openfiresecurity.messenger.R;

import static net.openfiresecurity.auth.AccountGeneral.sServerAuthenticate;

/**
 * The Authenticator activity.
 * <p/>
 * Called by the Authenticator and in charge of identifing the user.
 * <p/>
 * It sends back to the Authenticator the result.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    PreferenceHelper prefs;

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ACCOUNT_TYPE = "net.openfiresecurity.messenger";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        mAccountManager = AccountManager.get(getBaseContext());
        prefs = new PreferenceHelper(this);
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null) {
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
        }

        if (accountName != null) {
            ((TextView) findViewById(R.id.accountName)).setText(accountName);
        }

        findViewById(R.id.submit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submit();
                    }
                });
        findViewById(R.id.signUp).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent signup = new Intent(getBaseContext(),
                                SignUpActivity.class);
                        startActivityForResult(signup, REQ_SIGNUP);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created
        // an account
        if ((requestCode == REQ_SIGNUP) && (resultCode == RESULT_OK)) {
            finishLogin(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void submit() {

        final String userName = ((TextView) findViewById(R.id.accountName))
                .getText().toString();
        final String userEmail = ((TextView) findViewById(R.id.accountEmail))
                .getText().toString();
        final String userPass = ((TextView) findViewById(R.id.accountPassword))
                .getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        new AsyncTask<String, Void, Intent>() {

            @Override
            protected Intent doInBackground(String... params) {

                String authtoken = null;
                Bundle data = new Bundle();
                try {
                    authtoken = sServerAuthenticate.userSignIn(userName,
                            userEmail, userPass);

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString("email", userEmail);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                    data.putString(PARAM_USER_PASS, userPass);
                    data.putBoolean(ARG_IS_ADDING_NEW_ACCOUNT, true);

                } catch (Exception e) {
                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
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
                    finishLogin(intent);
                }
            }
        }.execute();
    }

    private void finishLogin(Intent intent) {
        String accountName = intent
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String accountEmail = intent.getStringExtra("email");
        final Account account = new Account(accountEmail, ACCOUNT_TYPE);

        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        String authtokenType = mAuthTokenType;

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, true)) {

            Bundle data = new Bundle();
            data.putString("user", accountName);
            data.putString("pass", accountPassword);
            data.putString("email", accountEmail);
            data.putString("hash", authtoken);

            // Creating the account on the device and setting the auth token we
            // got
            // (Not setting the auth token will cause another call to the server
            // to authenticate the user)
            mAccountManager
                    .addAccountExplicitly(account, accountPassword, data);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (prefs.getString("hash").isEmpty()) {
                System.exit(0);
            }
        }
    }
}
