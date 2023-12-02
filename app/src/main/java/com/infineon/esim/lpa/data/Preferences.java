/*
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". INFINEON
 * TECHNOLOGIES MAKES NO OTHER WARRANTY OF ANY KIND,WHETHER EXPRESS,IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. INFINEON TECHNOLOGIES
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * INFINEON TECHNOLOGIES SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 *
 * (C)Copyright INFINEON TECHNOLOGIES All rights reserved
 */
package com.infineon.esim.lpa.data;

import android.content.SharedPreferences;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.euicc.EuiccConnectionSettings;
import com.infineon.esim.util.Log;

final public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = Preferences.class.getName();

    private static Preferences instance;

    private static boolean preferencesHaveChanged;

    private Preferences() {
        preferencesHaveChanged = false;
        Application.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public static void initializeInstance() {
        if(instance == null) {
            instance = new Preferences();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.debug(TAG,"Preference \"" + key + "\" has changed.");

        String euiccNameKey = Application.getStringResource(R.string.pref_key_select_se);
        if(key.equals(euiccNameKey)) {
            String euiccName = Application.getSharedPreferences().getString(key, getNoEuiccName());
            Log.debug(TAG,"New value: " + euiccName);
        }
        preferencesHaveChanged = true;
    }

    public static boolean havePreferencesChanged() {
        boolean feedback = preferencesHaveChanged;
        preferencesHaveChanged = false;
        return feedback;
    }

    public static String getNoEuiccName() {
        return Application.getStringResource(R.string.pref_def_value_select_se);
    }

    public static void setEuiccName(String euiccName) {
        String euiccNameForPreferences = euiccName;
        if(euiccName == null) {
            euiccNameForPreferences = getNoEuiccName();
        }
        Log.debug(TAG, "Setting new reader name in preference holder: \"" + euiccNameForPreferences + "\".");

        // Store the new reader name
        String key = Application.getStringResource(R.string.pref_key_select_se);
        Application.getSharedPreferences()
                .edit()
                .putString(key, euiccName)
                .apply();
    }

    public static String getEuiccName() {
        String key = Application.getStringResource(R.string.pref_key_select_se);

        String euiccName = Application.getSharedPreferences().getString(key, getNoEuiccName());

        Log.debug(TAG,"Getting eUICC name from preferences: " + euiccName);

        return euiccName;
    }

    public static Boolean getKeepActiveProfile() {
        String key = Application.getStringResource(R.string.pref_key_keep_active_profile);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_keep_active_profile));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static EuiccConnectionSettings getReaderSettings() {
        return new EuiccConnectionSettings(getShallSendTerminalCapabilityCommand(),
                getShallOpenLogicalChannel(),
                getProfileInitializationTime());
    }

    private static int getProfileInitializationTime() {
        String key = Application.getStringResource(R.string.pref_key_euicc_profile_init_time);
        String defaultValue = Application.getStringResource(R.string.pref_def_value_euicc_profile_init_time);
        return Integer.parseInt(Application.getSharedPreferences().getString(key, defaultValue));
    }

    private static Boolean getShallSendTerminalCapabilityCommand() {
        String key = Application.getStringResource(R.string.pref_key_sim_slot_needs_term_cap_cmd);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_simslots_need_term_cap_cmd));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }


    private static boolean getShallOpenLogicalChannel() {
        String key = Application.getStringResource(R.string.pref_key_open_logical_channel);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_open_logical_channel));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }


    public static Boolean getTrustGsmaTestCi() {
        String key = Application.getStringResource(R.string.pref_key_trust_gsma_test_ci);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_trust_gsma_test_ci));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static Boolean getRetryWhenFail() {
        String key = Application.getStringResource(R.string.pref_key_retry_when_fail);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_retry_when_fail));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static Boolean getAntiRetrySpam() {
        String key = Application.getStringResource(R.string.pref_key_anti_retry_spam);
        boolean defaultValue = Boolean.parseBoolean(Application.getStringResource(R.string.pref_def_value_anti_retry_spam));
        return Application.getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static void setPermissionFinallyDenied(String permission) {
        String key = "com.infineon.esim.lpa.pref." + permission;
        Application.getSharedPreferences()
                .edit()
                .putBoolean(key, true)
                .apply();
    }

    public static Boolean getPermissionFinallyDenied(String permission) {
        String key = "com.infineon.esim.lpa.pref." + permission;
        return Application.getSharedPreferences().getBoolean(key, false);
    }
}
