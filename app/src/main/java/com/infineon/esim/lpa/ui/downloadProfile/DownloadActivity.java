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
package com.infineon.esim.lpa.ui.downloadProfile;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.ActivationCode;
import com.infineon.esim.lpa.core.dtos.enums.CancelSessionReasons;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.core.dtos.result.remote.AuthenticateResult;
import com.infineon.esim.lpa.core.dtos.result.remote.CancelSessionResult;
import com.infineon.esim.lpa.core.dtos.result.remote.DownloadResult;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.lpa.util.android.EventObserver;
import com.infineon.esim.util.Log;

final public class DownloadActivity extends AppCompatActivity {
    private static final String TAG = DownloadActivity.class.getName();

    private DownloadViewModel viewModel;

    private ActivationCode activationCode;
    private AuthenticateResult authenticateResult;
    private DownloadResult downloadResult;

    private AlertDialog progressDialog;
    private ProgressBar progressBar;
    private Boolean allowBackButtonPress;
    private TextView textViewHeading;
    private TextView textViewBody;
    private TextView textViewDownloadInfo;
    private TextView textViewErrorInfo;
    private TextView textViewConfirmationCode;
    private EditText editTextConfirmationCode;
    private Button buttonRight;
    private Button buttonLeft;

    // region Lifecycle management

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve the data passed using caller's intent

        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.TIRAMISU) {
            this.activationCode = getIntent().getParcelableExtra(Application.INTENT_EXTRA_ACTIVATION_CODE, ActivationCode.class);
        } else {
            this.activationCode = getIntent().getParcelableExtra(Application.INTENT_EXTRA_ACTIVATION_CODE);
        }

        this.authenticateResult = null;
        this.downloadResult = null;

        viewModel = new ViewModelProvider(this).get(DownloadViewModel.class);
        viewModel.getActionStatus().observe(this, actionStatusObserver);
        viewModel.getError().observe(this, errorEventObserver);

        // Attach the UI
        attachUi();

        // Start the profile download
        startProfileDownload();
    }

    @Override
    public void onBackPressed() {
        if (!allowBackButtonPress) {
            DialogHelper.showErrorDialog(this,
                    R.string.error_back_press_disabled_heading,
                    R.string.error_back_press_disabled_body,
                    false);
        } else {
            super.onBackPressed();
        }
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

    // endregion

    // region UI Manipulation

    private void attachUi() {
        setContentView(R.layout.activity_download_profile);
        setTitle(getString(R.string.scan_qr_title) + " - " + viewModel.getEuiccName());

        progressBar = findViewById(R.id.progress);
        textViewHeading = findViewById(R.id.text_heading);
        textViewBody = findViewById(R.id.text_body);
        textViewDownloadInfo = findViewById(R.id.textViewDownloadInfo);
        textViewErrorInfo = findViewById(R.id.text_error_info);
        buttonLeft = findViewById(R.id.button_left);
        buttonRight = findViewById(R.id.button_right);
        textViewConfirmationCode = findViewById(R.id.text_confirmation_code_title);
        editTextConfirmationCode = findViewById(R.id.edit_confirmation_code);
        editTextConfirmationCode.setOnEditorActionListener(textViewDoneActionListener);
    }

    private void setAuthenticateScreen() {
        hideLeftButton();
        hideRightButton();
        hideDownloadInfoText();
        hideErrorInfoText();
        hideConfirmationCodeText();
        setTextViewItems(R.string.download_profile_authenticating_heading, R.string.download_profile_authenticating_body);
        showProgressBar();
        disallowBackButtonPress();
    }

    private void setAuthenticateConfirmationScreen(AuthenticateResult authenticateResult) {
        ProfileMetadata newProfile = authenticateResult.getProfileMetadata();
        Boolean isCcRequired = authenticateResult.getCcRequired();

        String headingStr = String.format(getString(R.string.download_profile_use_provider_heading), newProfile.getProvider());
        String bodyStr = String.format(getString(R.string.download_profile_use_provider_body), newProfile.getProvider());

        allowBackButtonPress();
        hideProgressBar();
        hideDownloadInfoText();
        setTextViewItems(headingStr, bodyStr);
        if (isCcRequired) showConfirmationCodeEntry();
        showCancelButton();
        showStartButton();
    }

    private void setErrorScreen(String errorDetails) {
        allowBackButtonPress();
        hideProgressBar();
        hideDownloadInfoText();
        hideConfirmationCodeText();
        hideTextViewItems();
        showErrorInfoText(errorDetails);
        hideLeftButton();
        showDoneButton();
    }

    private void setDownloadScreen(String providerName) {
        String headerStr = getString(R.string.download_profile_downloading_heading);
        String bodyStr = String.format(getString(R.string.download_profile_downloading_body), providerName);

        disallowBackButtonPress();
        hideRightButton();
        hideLeftButton();
        hideDownloadInfoText();
        hideErrorInfoText();
        hideConfirmationCodeText();
        setTextViewItems(headerStr, bodyStr);
        showProgressBar();
    }

    private void setFinalConfirmationScreen(ProfileMetadata profileMetadata) {
        String headerStr = getString(R.string.download_profile_download_complete_heading);
        String bodyStr = String.format(getString(R.string.download_profile_download_complete_body),
                profileMetadata.getProvider(), profileMetadata.getProvider());

        hideDownloadInfoText();
        hideProgressBar();
        setTextViewItems(headerStr, bodyStr);

        showEnableNewProfileButton();
        showKeepCurrentProfileButton();
    }

    private void setTextViewItems(String headingText, String bodyText) {
        textViewHeading.setText(headingText);
        textViewHeading.setVisibility(View.VISIBLE);
        textViewBody.setText(bodyText);
        textViewBody.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("SameParameterValue")
    private void setTextViewItems(int headingTextResId, int bodyTextResId) {
        textViewHeading.setText(headingTextResId);
        textViewHeading.setVisibility(View.VISIBLE);
        textViewBody.setText(bodyTextResId);
        textViewBody.setVisibility(View.VISIBLE);
    }

    private void hideTextViewItems() {
        textViewHeading.setVisibility(View.GONE);
        textViewBody.setVisibility(View.GONE);
    }

    private void allowBackButtonPress() {
        allowBackButtonPress = true;
    }

    private void disallowBackButtonPress() {
        allowBackButtonPress = false;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showStartButton() {
        buttonRight.setOnClickListener(startButtonClickListener);
        buttonRight.setText(R.string.generic_button_start_text);
        buttonRight.setVisibility(View.VISIBLE);
    }

    private void showDoneButton() {
        buttonRight.setOnClickListener(doneButtonClickListener);
        buttonRight.setText(R.string.generic_button_done_text);
        buttonRight.setVisibility(View.VISIBLE);
    }

    private void showEnableNewProfileButton() {
        buttonLeft.setOnClickListener(enableNewProfileButtonClickListener);
        buttonLeft.setText(R.string.download_profile_button_enable_new_profile);
        buttonLeft.setVisibility(View.VISIBLE);
    }

    private void showKeepCurrentProfileButton() {
        buttonRight.setOnClickListener(doneButtonClickListener);
        buttonRight.setText(R.string.download_profile_button_keep_current_profile);
        buttonRight.setVisibility(View.VISIBLE);
    }

    private void showCancelButton() {
        buttonLeft.setOnClickListener(cancelButtonClickListener);
        buttonLeft.setText(R.string.generic_button_cancel_text);
        buttonLeft.setVisibility(View.VISIBLE);
    }

    private void hideLeftButton() {
        buttonLeft.setText("");
        buttonLeft.setVisibility(View.GONE);
    }

    private void hideRightButton() {
        buttonRight.setText("");
        buttonRight.setVisibility(View.GONE);
    }

    private void hideDownloadInfoText() {
        textViewDownloadInfo.setText("");
        textViewDownloadInfo.setVisibility(View.GONE);
    }

    private void showErrorInfoText(String errorDetails) {
        textViewErrorInfo.setText(errorDetails);
        textViewErrorInfo.setVisibility(View.VISIBLE);
    }

    private void hideErrorInfoText() {
        textViewErrorInfo.setText("");
        textViewErrorInfo.setVisibility(View.GONE);
    }

    private void showConfirmationCodeEntry() {
        textViewConfirmationCode.setVisibility(View.VISIBLE);
        editTextConfirmationCode.setVisibility(View.VISIBLE);
    }

    private void hideConfirmationCodeText() {
        textViewConfirmationCode.setVisibility(View.GONE);
        editTextConfirmationCode.setVisibility(View.GONE);
    }

    // endregion

    // region Observer and Listener

    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed that action status changed: " + actionStatus.getActionStatus());

        // Dismiss the last dialog if there is any
        dismissProgressDialog();

        switch (actionStatus.getActionStatus()) {
            case AUTHENTICATE_DOWNLOAD_STARTED:
            case AUTHENTICATE_DOWNLOAD_FINISHED:
                authenticateResult = viewModel.getAuthenticateDownloadResult();
                if(authenticateResult != null) {
                    if(authenticateResult.getSuccess()) {
                        setAuthenticateConfirmationScreen(authenticateResult);
                    } else {
                        setErrorScreen(authenticateResult.getErrorDetails());
                    }
                }
                break;
            case DOWNLOAD_PROFILE_STARTED:
            case DOWNLOAD_PROFILE_FINISHED:
                downloadResult = viewModel.getDownloadResult();
                if(downloadResult != null) {
                    if(downloadResult.getSuccess() && authenticateResult.getSuccess()) {
                        setFinalConfirmationScreen(authenticateResult.getProfileMetadata());
                    } else {
                        setErrorScreen(downloadResult.getErrorDetails());
                    }
                }
                break;
            case ENABLE_PROFILE_STARTED:
                Log.debug(TAG, "Show progress dialog for enabling profile started.");
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_switching_profile);
                disallowBackButtonPress();
            case ENABLE_PROFILE_FINISHED:
                finish();
                break;
            case CANCEL_SESSION_STARTED:
                Log.debug(TAG, "Show progress dialog for cancelling session started.");
                progressDialog = DialogHelper.showProgressDialog(this, R.string.action_cancel_session_heading);
                break;
            case CANCEL_SESSION_FINISHED:
                Log.debug(TAG, "Show progress dialog for cancelling session finished.");
                CancelSessionResult cancelSessionResult = viewModel.getCancelSessionResult();
                if(cancelSessionResult != null) {
                    if (cancelSessionResult.getSuccess()) {
                        finish();
                    } else {
                        setErrorScreen(cancelSessionResult.getErrorDetails());
                    }
                }
                break;
            default:
                // nothing
        }
    };

    private void dismissProgressDialog() {
        Log.debug(TAG, "Dismiss progress dialog.");

        if (progressDialog != null) progressDialog.dismiss();
    }

    final EventObserver<Error> errorEventObserver = new EventObserver<>(error -> {
        Log.debug(TAG, "Observed that error happened during loading: " + error);
        DialogHelper.showErrorDialog(this, error, true);
    });

    private final TextView.OnEditorActionListener textViewDoneActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // Hide keyboard
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = getCurrentFocus();
            if((inputManager != null) && (currentFocus != null)) {
                inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            startDownload();

            return true;
        }
        return false;
    };

    private final View.OnClickListener startButtonClickListener = v -> startDownload();

    private final View.OnClickListener cancelButtonClickListener = v -> cancelSession();

    private final View.OnClickListener doneButtonClickListener = v -> finish();

    private final View.OnClickListener enableNewProfileButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if((authenticateResult != null) && authenticateResult.getSuccess()) {
                viewModel.enableProfile(authenticateResult.getProfileMetadata());
            }
        }
    };

    // endregion

    // region UI Logic

    private void startProfileDownload() {
        if (activationCode.isValid()) {
            authenticateResult = null;
            downloadResult = null;

            // Start the authentication process
            startAuthenticate();
        } else {
            Log.error(TAG,"Invalid activation code: \"" + activationCode + "\".");
            hideProgressBar();

            DialogHelper.showErrorDialog(this,
                    R.string.error_invalid_authentication_code_heading,
                    R.string.error_invalid_authentication_ode_body,
                    true);
        }
    }

    private void startAuthenticate() {
        setAuthenticateScreen();

        viewModel.authenticate(activationCode);
    }

    private void startDownload() {
        if(authenticateResult != null) {
            String providerName = authenticateResult.getProfileMetadata().getProvider();
            Boolean isCcRequired = authenticateResult.getCcRequired();

            // Prepare the UI for download
            setDownloadScreen(providerName);

            // Get confirmation code if needed
            String confirmationCode = null;
            if(isCcRequired && (editTextConfirmationCode != null)) {
                Log.debug(TAG,"Confirmation code required.");
                confirmationCode = editTextConfirmationCode.getText().toString();
                Log.debug(TAG,"Confirmation code: " + confirmationCode);
            } else {
                Log.debug(TAG,"No confirmation code required.");
            }

            // Start the download
            viewModel.downloadProfile(confirmationCode);
        } else {
            DialogHelper.showGenericErrorDialog(this);
        }
    }

    private void cancelSession() {
        Log.debug(TAG,"Cancel profile download session.");
        viewModel.cancelSession(CancelSessionReasons.END_USER_REJECTION);
    }

    // endregion
}