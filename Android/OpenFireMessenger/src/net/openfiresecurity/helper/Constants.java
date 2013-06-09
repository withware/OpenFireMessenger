/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.helper;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

@SuppressLint("SdCardPath")
public class Constants {

    // Updater
    @NotNull
    public static final String fileName = "OpenFireMessenger_";
    @NotNull
    public static final String urls = "http://android.openfire-security.net/files/apps/messenger/";
    @NotNull
    public static final String URL = "http://android.openfire-security.net/files/apps/";
    @NotNull
    public static final String versionFile = "messenger.version";
    public static final boolean DEBUG = true;

    //
    public static final String TAG = "OpenFireMessenger";

    // Sender
    // public static final
    public static final String MSGURL = "http://192.168.2.103/messenger/";
    public static final String EXCHANGER = "exchanger.php";
    public static final String USER = "user.php";
    public static final String GETFILE = "getfile.php";
    public static final String TESTHTML = "test.html";
    public static final String SIGNUP = "?signup";
    public static final String LOGIN = "?login";
}
