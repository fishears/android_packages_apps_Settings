/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class UKSM extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/uksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";    
    private static final String KSM_CPU_PREF = "pref_uksm_cpu";    
    private static final String KSM_CPU_FILE = "/sys/kernel/mm/uksm/cpu_governor";

    private CheckBoxPreference mKSMPref;    
    private ListPreference mKSMcpu;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.uksm);

            PreferenceScreen prefSet = getPreferenceScreen();
            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);
            mKSMcpu = (ListPreference) prefSet.findPreference(KSM_CPU_PREF);

            if (Utils.fileExists(KSM_RUN_FILE)) {
                mKSMPref.setChecked(KSM_PREF_ENABLED.equals(Utils.fileReadOneLine(KSM_RUN_FILE)));
            } else {
                prefSet.removePreference(mKSMPref);
            }
            
            if (Utils.fileExists(KSM_CPU_FILE)) {
		String input = Utils.fileReadOneLine(KSM_CPU_FILE);
		Pattern p = Pattern.compile("\\[(.*?)\\]");
		Matcher m = p.matcher(input);
		String active = "";
		while(m.find()) {
		  active = m.group(1);
		}
		
                mKSMcpu.setValue(active);
                mKSMcpu.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mKSMcpu);
            }
            if (!mKSMPref.isChecked())
		mKSMcpu.setEnabled(false);
	    else
		mKSMcpu.setEnabled(true);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mKSMPref) {
            Utils.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            if (!mKSMPref.isChecked())
		mKSMcpu.setEnabled(false);
	    else
		mKSMcpu.setEnabled(true);
            return true;
        }

        return false;
    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	
        if (preference == mKSMcpu) {
	    if (newValue != null) {
		if (Utils.fileExists(KSM_CPU_FILE)) {
		    Utils.fileWriteOneLine(KSM_CPU_FILE, (String) newValue);
		    return true;
		}
	    }
	}
        return false;
    }
}
