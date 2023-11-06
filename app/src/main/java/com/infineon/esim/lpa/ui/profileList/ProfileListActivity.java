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
package com.infineon.esim.lpa.ui.profileList;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infineon.esim.lpa.BuildConfig;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.data.Preferences;
import com.infineon.esim.lpa.ui.euiccDetails.EuiccDetailsActivity;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.ui.preference.PreferenceActivity;
import com.infineon.esim.lpa.ui.scanBarcode.ScanBarcodeActivity;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.lpa.util.android.EventObserver;
import com.infineon.esim.lpa.util.android.NetworkStatus;
import com.infineon.esim.lpa.util.android.WifiStatus;
import com.infineon.esim.util.Log;

final public class ProfileListActivity extends AppCompatActivity {
    private static final String TAG = ProfileListActivity.class.getName();

    private Boolean allowBackButtonPress = true;

    private AlertDialog progressDialog;

    private ProfileListViewModel viewModel;

    // region Lifecycle Management

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.debug(TAG, "Created activity.");

        Log.debug(TAG, "Setting live data observer.");
        // Get the ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileListViewModel.class);
        viewModel.getEuiccNameLiveData().observe(this, euiccNameObserver);
        viewModel.getActionStatusLiveData().observe(this, actionStatusObserver);
        viewModel.getError().observe(this, errorEventObserver);

        // Attach the UI
        attachUi();

        // Initialize USB reader if app has been started from USB attach event
        UsbDevice usbDevice;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            usbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice.class);
        } else {
            usbDevice = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        }

        if(usbDevice != null) {
            viewModel.connectUSBEuiccInterface();
        }
    }

    @Override
    protected void onPause() {
        Log.debug(TAG, "Pausing activity.");

        // Stop observing errors when activity is not visible
        viewModel.getError().removeObserver(errorEventObserver);

        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.debug(TAG, "Resuming activity.");

        // Start observing errors when activity is visible again
        viewModel.getError().observe(this, errorEventObserver);

        // Initialize freshly attached USB reader
        viewModel.selectFreshlyAttachedUsbReader();

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.debug(TAG, "Destroying activity.");

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Only allow back button press when not busy
        if (allowBackButtonPress) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_profile_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Use if instead of switch because of this warning:
        // Resource IDs will be non-final in Android Gradle Plugin version 5.0, avoid using them in
        // switch case statements
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            // Open settings intent
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        } else if(id == R.id.action_app_version) {
            // Show app version popup
            showAppVersionPopup();
            return true;
        } else if(id == R.id.action_license_info) {
            // Start license info intent
            showOpenSourceLicenseActivity();
            return true;
        } else if(id == R.id.action_euicc_info) {
            // Start eUICC info intent
            startActivity(new Intent(this, EuiccDetailsActivity.class));
            return true;
        } else if(id == R.id.action_clear_notifications) {
            // Clear all eUICC notifications
            viewModel.clearAllNotifications();
            return true;
        } else if(id == R.id.action_refresh_esims) {
            // Start eUICC info intent
            viewModel.refreshEuiccs();
            return true;
        } else if(id == R.id.action_refresh_profile_list) {
            // Start eUICC info intent
            viewModel.refreshProfileList();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // endregion

    // region UI manipulation

    private void attachUi() {
        Log.debug(TAG, "Attaching UI.");
        setContentView(R.layout.activity_profile_list);
        setSupportActionBar(findViewById(R.id.toolbar));

        FloatingActionButton fab = findViewById(R.id.button_add_profile);
        fab.setOnClickListener(floatingButtonOnClickListener);

        setProfileListFragment();
    }

    private void setProfileListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.profile_list_placeholder, new ProfileListFragment());
        fragmentTransaction.commit();
    }

    private void setNoReaderFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.profile_list_placeholder, new NoReaderFragment());
        fragmentTransaction.commit();
    }

    private void allowBackButtonPress() {
        allowBackButtonPress = true;
    }

    private void disallowBackButtonPress() {
        allowBackButtonPress = false;
    }

    private void showOpenSourceLicenseActivity() {
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    private void showAppVersionPopup() {
        String body = String.format(getString(R.string.app_version_body),
                BuildConfig.APPLICATION_ID,
                BuildConfig.BUILD_TYPE,
                BuildConfig.VERSION_NAME);

        Log.debug(TAG,"Showing app version info: " + body);
        DialogHelper.showErrorDialog(this,
                R.string.menu_item_app_version,
                body,
                false);
    }

    // endregion

    // region Listener and observer
    final View.OnClickListener floatingButtonOnClickListener = (view) -> {
        if(NetworkStatus.isNetworkAvailable() && WifiStatus.isWifiEnabled()) {
            startActivity(new Intent(ProfileListActivity.this, ScanBarcodeActivity.class));
        } else {
            if (!WifiStatus.isWifiEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_wifi_disabled_heading)
                        .setMessage(R.string.error_wifi_disabled_body)
                        .setCancelable(true)
                        .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss())
                        .setNeutralButton(R.string.error_wifi_disabled_positive_button, (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                        .show();
            } else {
                if (!NetworkStatus.isNetworkAvailable()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.error_wifi_not_connected_heading)
                            .setMessage(R.string.error_wifi_not_connected_body)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss())
                            .setNeutralButton(R.string.error_wifi_not_connected_positive_button, (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                            .show();
                }
            }
        }
    };



    final Observer<String> euiccNameObserver = euiccName -> {

        if(euiccName.equals(Preferences.getNoEuiccName())) {
            Log.error(TAG, "No eSIM reader available!");

            setTitle(getString(R.string.app_name) + " - " + "No eSIM available!");

            setNoReaderFragment();
        } else {
            Log.debug(TAG, "Observed change in eUICC name: " + euiccName);

            setTitle(getString(R.string.app_name) + " - " + euiccName);

            setProfileListFragment();
        }
    };

    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed that action status changed: " + actionStatus.getActionStatus());

        // Dismiss the last dialog if there is any
        dismissProgressDialog();

        switch (actionStatus.getActionStatus()) {
            case REFRESHING_EUICC_LIST_STARTED: {
                progressDialog = DialogHelper.showProgressDialog(this, R.string.pref_progress_refreshing_euicc_list);
                disallowBackButtonPress();
                break;
            }
            case OPENING_EUICC_CONNECTION_STARTED: {
                String euiccName = (String) actionStatus.getExtras();
                if(euiccName != null) {
                    String body = String.format(getString(R.string.action_opening_reader), euiccName);
                    progressDialog = DialogHelper.showProgressDialog(this, body);
                    disallowBackButtonPress();
                }
                break;
            }
            case GET_PROFILE_LIST_STARTED: {
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_getting_profiles);
                disallowBackButtonPress();
                break;
            }
            case ENABLE_PROFILE_STARTED: {
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_switching_profile);
                disallowBackButtonPress();
                break;
            }
            case CLEAR_ALL_NOTIFICATIONS_STARTED: {
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_clearing_notifications);
                disallowBackButtonPress();
                break;
            }
            case REFRESHING_EUICC_LIST_FINISHED:
            case OPENING_EUICC_CONNECTION_FINISHED:
            case GET_PROFILE_LIST_FINISHED:
            case ENABLE_PROFILE_FINISHED:
            case CLEAR_ALL_NOTIFICATIONS_FINISHED: {
                allowBackButtonPress();
                break;
            }
            default:
                // nothing
        }
    };

    private void dismissProgressDialog() {
        if (progressDialog != null) progressDialog.dismiss();
    }

    final EventObserver<Error> errorEventObserver = new EventObserver<>(error -> {
        Log.debug(TAG, "Observed that error happened during loading: " + error);
        dismissProgressDialog();
        DialogHelper.showErrorDialog(this, error, false);
        allowBackButtonPress();
    });
}
