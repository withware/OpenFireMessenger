/*
 * Copyright (c) 2013. Alexander Martinz @ OpenFire Security
 */

package net.openfiresecurity.data;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.openfiresecurity.messenger.R;

public class Preferences extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // add the prefernces.xml layout
        addPreferencesFromResource(R.xml.prefs);

        // get the specified preferences using the key declared in
        // preferences.xml
        // @NotNull
        // ListPreference dataPref = (ListPreference) findPreference("date");

        // get the description from the selected item
        // dataPref.setSummary(dataPref.getEntry());

        // when the user choose other item the description changes too with the
        // selected item
        // dataPref.setOnPreferenceChangeListener(new
        // Preference.OnPreferenceChangeListener() {
        // @Override
        // public boolean onPreferenceChange(@NotNull Preference preference,
        // @NotNull Object o) {
        // preference.setSummary(o.toString());
        // return true;
        // }
        // });
    }
}