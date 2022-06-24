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
package com.infineon.esim.lpa.ui.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.es9plus.TlsUtil;
import com.infineon.esim.util.Log;

import java.util.List;

public class PreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = PreferenceFragment.class.getName();

    private PreferenceViewModel viewModel;

    // region Lifecycle management

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PreferenceActivity activity = (PreferenceActivity) requireActivity();

        viewModel.getEuiccListLiveData().observe(activity, euiccListObserver);
        viewModel.getCurrentEuiccLiveData().observe(activity, currentEuiccObserver);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        viewModel = new ViewModelProvider(this).get(PreferenceViewModel.class);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        CheckBoxPreference checkBoxPreferenceTrustTestCi = findPreference(getString(R.string.pref_key_trust_gsma_test_ci));
        if(checkBoxPreferenceTrustTestCi != null) {
            checkBoxPreferenceTrustTestCi.setOnPreferenceChangeListener(trustTestCiChangeListener);
        }
    }

    // endregion

    // region UI manipulation

    public void updateEuiccList() {
        List<String> euiccNames = viewModel.getEuiccList();
        String currentEuicc = viewModel.getCurrentEuicc();

        Log.debug(TAG, "Euicc list: current eUICC: " + currentEuicc);
        Log.debug(TAG, "Euicc list: " + euiccNames);

        ListPreference readerPref = findPreference(Application.getStringResource(R.string.pref_key_select_se));
        if(readerPref != null) {
            readerPref.setEntries(euiccNames.toArray(new String[0]));
            readerPref.setEntryValues(euiccNames.toArray(new String[0]));
            if((currentEuicc != null) && euiccNames.contains(currentEuicc)) {
                readerPref.setValue(currentEuicc);
            }
        }
    }

    final Preference.OnPreferenceChangeListener trustTestCiChangeListener = (preference, newValue) -> {
        Boolean trustTestCi = (Boolean) newValue;

        TlsUtil.setTrustLevel(trustTestCi);

        return true;
    };

    final Observer<List<String>> euiccListObserver = euiccList -> {
        Log.debug(TAG, "Observed change in eUICC list: " + euiccList);
        updateEuiccList();
    };

    final Observer<String> currentEuiccObserver = euiccName -> {
        Log.debug(TAG, "Observed change in eUICC: " + euiccName);
        updateEuiccList();
    };

    // endregion
}