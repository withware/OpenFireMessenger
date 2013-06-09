package net.openfiresecurity.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import net.openfiresecurity.helper.SignUpSender;

import org.jetbrains.annotations.NotNull;

public class SignUp extends Activity {

	Button bSignup;
	EditText etUser, etPass, etPassConfirm, etEmail;
	CheckBox cbAgree;
	Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		res = getResources();

		etUser = (EditText) findViewById(R.id.etUsername);
		etPass = (EditText) findViewById(R.id.etPassword);
		etPassConfirm = (EditText) findViewById(R.id.etPasswordConfirm);
		etEmail = (EditText) findViewById(R.id.etEmail);

		bSignup = (Button) findViewById(R.id.bFinishSignUp);
		bSignup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (true) { // cbAgree.isChecked()
					if (etPass.getText().toString()
							.equals(etPassConfirm.getText().toString())) {
						new SignUpSender(SignUp.this).execute(new String[] {
								etUser.getText().toString(),
								etPass.getText().toString(),
								etEmail.getText().toString() });
					} else {
						Toast.makeText(SignUp.this, "Passwords dont match!",
								Toast.LENGTH_LONG).show();
					}
				}
			}

		});
	}

	/**
	 * Gets the Result of the SignUp and does actions depending on what it
	 * returns.
	 * 
	 * @param result
	 *            The Statuscode, returned by the PHP Server Backend. <br>
	 * <br>
	 *            <p>
	 *            <strong>List of Statuscodes:</strong><br>
	 *            0 - Success<br>
	 *            201 - Username taken<br>
	 *            202 - Password weak<br>
	 *            203 - Email taken<br>
	 *            </p>
	 */
	public void displayResult(String result) {
		int statuscode = 0;
		try {
			statuscode = Integer.parseInt(result);
		} catch (Exception exc) {
			showInfoDialog("Unknown Error occured!",
					res.getString(R.string.error), null, true);
			return;
		}
		switch (statuscode) {
		case 0:
			showInfoDialog("Successfully signed up!",
					res.getString(R.string.success),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}, false);
			break;
		case 201:
			showInfoDialog("Username already taken!",
					res.getString(R.string.error), null, true);
			break;
		case 202:
			showInfoDialog("Password is too weak!",
					res.getString(R.string.error), null, true);
			break;
		case 203:
			showInfoDialog("Email already taken!",
					res.getString(R.string.error), null, true);
			break;
		default:
			showInfoDialog("Unknown Error occured!",
					res.getString(R.string.error), null, true);
			break;
		}
	}

	public void showInfoDialog(String msg, String title,
			DialogInterface.OnClickListener click, boolean dismiss) {
		@NotNull
		AlertDialog.Builder dialog = new AlertDialog.Builder(SignUp.this);
		dialog.setTitle(title);
		dialog.setMessage(msg);
		dialog.setNeutralButton("Ok", click);
		dialog.setCancelable(dismiss);
		dialog.show();
	}
}
