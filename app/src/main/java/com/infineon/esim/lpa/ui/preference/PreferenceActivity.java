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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.lpa.util.android.EventObserver;
import com.infineon.esim.util.Log;

public class PreferenceActivity extends AppCompatActivity {
    private static final String TAG = PreferenceActivity.class.getName();

    private PreferenceViewModel viewModel;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.debug(TAG, "Creating preference activity.");
        super.onCreate(savedInstanceState);

        this.viewModel = new ViewModelProvider(this).get(PreferenceViewModel.class);
        viewModel.getActionStatus().observe(this, actionStatusObserver);
        viewModel.getErrorEvent().observe(this, errorEventObserver);

        showSettings();
    }

    @Override
    protected void onPause() {
        Log.debug(TAG, "Pausing activity.");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.debug(TAG, "Resuming activity.");
        super.onResume();
    }

    @Override
    public void onStop() {
        // App crashes on Oppo Reno Z when starting with Android studio if not used...
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.debug(TAG, "Destroying preference activity.");
        viewModel.savePreferences();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.debug(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PreferenceFragment())
                .commit();
    }

    public void dismissProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    final DialogInterface.OnClickListener onCancelListener = (dialog, which) -> showSettings();

    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed change in action status: " + actionStatus.getActionStatus());

        // Dismiss progress dialog if there is any
        dismissProgressDialog();

        switch (actionStatus.getActionStatus()) {
            case REFRESHING_EUICC_LIST_STARTED:
                Log.debug(TAG, "Show progress dialog for refreshing the eUICC list.");
                progressDialog = DialogHelper.showProgressDialog(this, R.string.pref_progress_refreshing_euicc_list);
                break;
            case REFRESHING_EUICC_LIST_FINISHED:
                Log.debug(TAG, "Refreshing eUICC list finished.");
                break;
            case CONNECTING_INTERFACE_STARTED:
                Log.debug(TAG, "Connecting eUICC interface started.");
                String body = String.format(getString(R.string.pref_progress_connecting_reader), actionStatus.getExtras());
                progressDialog = DialogHelper.showCancelableProgressDialog(this, body, onCancelListener);
                break;
            case DISCONNECTING_INTERFACE_STARTED:
                Log.debug(TAG, "Disconnecting reader started.");
                progressDialog = DialogHelper.showProgressDialog(this, R.string.pref_progress_disconnecting_reader);
                break;
            case DISCONNECTING_INTERFACE_FINISHED:
                Log.debug(TAG, "Disconnecting reader finished.");
                break;
            default:
        }
    };

    final EventObserver<Error> errorEventObserver = new EventObserver<>(error -> {
        // Dismiss progress dialog if there is any
        dismissProgressDialog();

        Log.debug(TAG, "Observed that error happened during loading: " + error);
        DialogHelper.showErrorDialog(this, error, false);
    });
}
