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
package com.infineon.esim.lpa.ui.profileDetails;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.data.Preferences;
import com.infineon.esim.lpa.ui.dialog.ConfirmationDialog;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.ui.generic.ProfileIcons;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.lpa.util.android.EventObserver;
import com.infineon.esim.util.Log;

final public class ProfileDetailsActivity extends AppCompatActivity {
    private static final String TAG = ProfileDetailsActivity.class.getName();

    private ProfileDetailsViewModel viewModel;

    private AlertDialog progressDialog;
    private ProfileMetadata profileMetadata;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        Log.debug(TAG,"Creating activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        // retrieve the data passed using caller's intent
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.TIRAMISU) {
            this.profileMetadata = getIntent().getParcelableExtra(Application.INTENT_EXTRA_PROFILE_METADATA, ProfileMetadata.class);
        } else {
            this.profileMetadata = getIntent().getParcelableExtra(Application.INTENT_EXTRA_PROFILE_METADATA);
        }

        if(profileMetadata == null) {
            DialogHelper.showGenericErrorDialog(this, true);
        }

        attachUi();

        // display a back button in action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Log.debug(TAG,"Setting up view model.");
        // Get the ViewModel
        viewModel= new ViewModelProvider(this).get(ProfileDetailsViewModel.class);
        viewModel.getActionStatus().observe(this, actionStatusObserver);
        viewModel.getErrorEvent().observe(this, errorEventObserver);
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

    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG,"Observed that action status changed: " + actionStatus.getActionStatus());

        // Dismiss the last dialog if there is any
        dismissProgressDialog();

        switch (actionStatus.getActionStatus()) {
            case DELETE_PROFILE_STARTED:
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_deleting_profile);
                break;
            case ENABLE_PROFILE_STARTED:
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_switching_profile);
                break;
            case DISABLE_PROFILE_STARTED:
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_disabling_profile);
                break;
            case ENABLE_PROFILE_FINISHED:
                profileMetadata.setEnabled(true);
                setProfileStatus(true);
                break;
            case DISABLE_PROFILE_FINISHED:
                profileMetadata.setEnabled(false);
                setProfileStatus(false);
                break;
            case DELETE_PROFILE_FINISHED:
            case OPENING_EUICC_CONNECTION_STARTED:
            case OPENING_EUICC_CONNECTION_FINISHED:
                finish();
                break;
            default:
                // nothing
                break;
        }
    };

    final EventObserver<Error> errorEventObserver = new EventObserver<>(error -> {
        Log.debug(TAG, "Observed that error happened during loading: " + error);

        // Dismiss running progress dialog if there is any
        dismissProgressDialog();
        // Show error dialog
        DialogHelper.showErrorDialog(this,error,true);
    });

    // handle the action bar's back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // required for action bar's back button
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return true;
    }

    private void attachUi() {
        setTitle(getString(R.string.profile_details_title));

        ImageView imageViewProfileIcon = findViewById(R.id.imageViewProfileIcon);
        ImageView imageViewEditNicknameIcon = findViewById(R.id.imageViewNicknameEditIcon);
        TextView textViewIccidBody = findViewById(R.id.textViewIccidBody);
        TextView textViewClassBody = findViewById(R.id.textViewClassBody);
        TextView textViewProviderBody = findViewById(R.id.textViewProviderBody);
        TextView textViewNicknameBody = findViewById(R.id.textViewNicknameBody);

        imageViewEditNicknameIcon.setOnClickListener(editNicknameOnClickListener);

        if(profileMetadata.getIcon() != null) {
            imageViewProfileIcon.setImageIcon(profileMetadata.getIcon());
        } else {
            imageViewProfileIcon.setImageResource(ProfileIcons.lookupProfileImage(profileMetadata.getName()));
        }

        String iccidUserString = ProfileMetadata.formatIccidUserString((profileMetadata.getIccid()));
        String profileclassString = ProfileMetadata.formatProfileClassString((profileMetadata.getProfileclass()));
        textViewIccidBody.setText(iccidUserString);
        textViewProviderBody.setText(profileMetadata.getProvider());
        textViewNicknameBody.setText(profileMetadata.getNickname());
        textViewClassBody.setText(profileclassString);

        setProfileStatus(profileMetadata.isEnabled());
    }

    private void setProfileStatus(boolean isEnabled) {
        TextView textViewStatusBody = findViewById(R.id.textViewStatusBody);
        Button buttonEnableDisable = findViewById(R.id.button_enable_disable);
        Button buttonDelete = findViewById(R.id.button_delete);

        buttonEnableDisable.setOnClickListener(enableDisableButtonOnClickListener);
        buttonDelete.setOnClickListener(deleteButtonOnClickListener);

        if (isEnabled) {
            textViewStatusBody.setText(R.string.profile_details_status_enabled);
            buttonEnableDisable.setText(R.string.profile_details_button_disable_text);
        } else {
            textViewStatusBody.setText(R.string.profile_details_status_disabled);
            buttonEnableDisable.setText(R.string.profile_details_button_enable_text);
        }

        if (Preferences.getKeepActiveProfile() && isEnabled) {
            buttonEnableDisable.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener editNicknameOnClickListener =
            v -> DialogHelper.showEditTextDialog(this,
                    getString(R.string.profile_details_edit_profile_nickname_dialog_title),
                    profileMetadata.getNickname(),
                    new DialogHelper.TextReturnCallback() {
                        @Override
                        public void onReturn(String newNickname) {
                            Log.debug(TAG, "New nickname: " + newNickname);
                            TextView textViewNicknameBody = findViewById(R.id.textViewNicknameBody);
                            textViewNicknameBody.setText(newNickname);

                            profileMetadata.setNickname(newNickname);

                            viewModel.setNickname(profileMetadata);
                        }
    });

    private final View.OnClickListener enableDisableButtonOnClickListener = v -> {
        if (profileMetadata.isEnabled()) {
            viewModel.disableProfile(profileMetadata);
        } else {
            viewModel.enableProfile(profileMetadata);
        }
    };

    private final View.OnClickListener deleteButtonOnClickListener = v -> {
        Log.debug(TAG,"Delete button clicked.");
        ConfirmationDialog.showDeleteProfileConfirmationDialog(this, profileMetadata);
    };

    private void dismissProgressDialog() {
        if(progressDialog != null) progressDialog.dismiss();
    }
}
