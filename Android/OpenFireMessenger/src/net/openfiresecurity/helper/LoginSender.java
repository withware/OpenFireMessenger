/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import net.openfiresecurity.helper.CustomMultiPartEntity.ProgressListener;
import net.openfiresecurity.messenger.Menu;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginSender extends AsyncTask<String, Integer, String> {

    private final Menu menu;
    @NotNull
    private final ProgressDialog pd;
    private long totalSize;

    /**
     * Constructer of the Uploader, takes the main activity as param, so we can
     * display a progress dialog.
     *
     * @param context Our Mainactivity
     */
    public LoginSender(Menu context) {
        menu = context;
        pd = new ProgressDialog(menu);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("Logging in...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {
        @NotNull String url = Constants.MSGURL + Constants.USER + Constants.LOGIN;
        String user = params[0];
        String pass = params[1];

        @NotNull
        HttpClient httpClient = new DefaultHttpClient();
        @NotNull
        HttpContext httpContext = new BasicHttpContext();
        @NotNull
        HttpPost httpPost = new HttpPost(url);
        try {
            @NotNull
            CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE,
                    new ProgressListener() {
                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            multipartContent.addPart("username", new StringBody(user));
            multipartContent.addPart("password", new StringBody(pass));

            totalSize = multipartContent.getContentLength();
            httpPost.setEntity(multipartContent);
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost,
                        httpContext);
                return (entityToString(httpResponse.getEntity()));
            } catch (Exception exc) {
                // signup.ErrorDialog(exc.getLocalizedMessage());
                return (exc.getMessage());
            }
        } catch (Exception excp) {
            // signup.ErrorDialog(excp.getLocalizedMessage());
            return (excp.getMessage());
        }
    }

    /**
     * Converts HttpEntities into readable text.
     *
     * @param entity Entity, which should get converted to a String.
     */
    @NotNull
    String entityToString(@NotNull HttpEntity entity) {
        @Nullable
        InputStream is = null;
        @Nullable
        StringBuilder str = null;
        try {
            is = entity.getContent();
            @NotNull
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));
            str = new StringBuilder();

            @Nullable
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, e.getMessage());
            }
        }
        return str.toString();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        pd.setProgress((progress[0]));
    }

    @Override
    protected void onPostExecute(String result) {
        pd.dismiss();
        menu.displayResult(result);
    }
}
