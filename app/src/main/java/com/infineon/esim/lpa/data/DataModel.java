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

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.infineon.esim.lpa.core.dtos.ActivationCode;
import com.infineon.esim.lpa.core.dtos.EuiccInfo;
import com.infineon.esim.lpa.core.dtos.profile.ProfileList;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.core.dtos.result.remote.AuthenticateResult;
import com.infineon.esim.lpa.core.dtos.result.remote.CancelSessionResult;
import com.infineon.esim.lpa.core.dtos.result.remote.DownloadResult;
import com.infineon.esim.lpa.euicc.EuiccManager;
import com.infineon.esim.lpa.lpa.LocalProfileAssistant;
import com.infineon.esim.lpa.ui.generic.ActionStatus;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.android.OneTimeEvent;
import com.infineon.esim.util.Log;

import java.util.List;

public class DataModel implements StatusAndEventHandler{
    private static final String TAG = DataModel.class.getName();

    private static DataModel instance;

    private final LocalProfileAssistant lpa;
    private final EuiccManager euiccManager;

    private final MutableLiveData<AsyncActionStatus> actionStatusLiveData;
    private final MutableLiveData<OneTimeEvent<Error>> errorEventLiveData;

    private DataModel(Context context) {
        this.euiccManager = new EuiccManager(context, this);
        this.lpa = new LocalProfileAssistant(euiccManager, this);

        this.actionStatusLiveData = new MutableLiveData<>();
        this.errorEventLiveData = new MutableLiveData<>();

        euiccManager.initializeInterfaces();

        actionStatusLiveData.observeForever(actionStatusObserver);
    }

    public static void initializeInstance(Context context) {
        if(instance == null) {
            instance = new DataModel(context);
        }
    }

    public static DataModel getInstance() {
        return instance;
    }

    // observing action status
    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed that action status changed: " + actionStatus.getActionStatus());

        switch (actionStatus.getActionStatus()) {
            case ENABLE_PROFILE_FINISHED:
            case DELETE_PROFILE_FINISHED:
            case DISABLE_PROFILE_FINISHED:
            case SET_NICKNAME_FINISHED:
                refreshProfileList();
                break;
        }

    };

    // region Getter
    public LiveData<String> getCurrentEuiccLiveData() {
        return euiccManager.getCurrentEuiccLiveData();
    }

    public LiveData<List<String>> getEuiccListLiveData() {
        return euiccManager.getEuiccListLiveData();
    }

    public LiveData<ProfileList> getProfileListLiveData() {
        return lpa.getProfileListLiveData();
    }

    public LiveData<AsyncActionStatus> getAsyncActionStatusLiveData() {
        return actionStatusLiveData;
    }

    public LiveData<OneTimeEvent<Error>> getErrorEventLiveData() {
        return errorEventLiveData;
    }

    // endregion

    // region eUICC interface methods

    public void refreshEuiccs() {
        Log.debug(TAG, "Refreshing eUICC list...");
        euiccManager.startRefreshingEuiccList();
    }

    public void selectEuicc(String euiccName) {
        Log.debug(TAG, "Selecting euicc " + euiccName + "...");
        euiccManager.selectEuicc(euiccName);
    }

    public void startConnectingEuiccInterface(String interfaceTag) {
        Log.debug(TAG, "Connecting eUICC interface " + interfaceTag + "...");
        euiccManager.startConnectingEuiccInterface(interfaceTag);
    }

    @SuppressWarnings("unused")
    public void startDisconnectingReader(String interfaceTag) {
        Log.debug(TAG, "Disconnecting eUICC interface " + interfaceTag + "...");
        euiccManager.startDisconnectingInterface(interfaceTag);
    }

    @SuppressWarnings("unused")
    public Boolean isEuiccInterfaceConnected(String readerTag) {
        return euiccManager.isEuiccInterfaceConnected(readerTag);
    }

    // endregion

    // region LPA methods

    public EuiccInfo getEuiccInfo() {
        return lpa.getEuiccInfo();
    }

    public void refreshEuiccInfo() {
        lpa.refreshEuiccInfo();
    }

    public void refreshProfileList() {
        lpa.refreshProfileList();
    }

    public void enableProfile(ProfileMetadata profile) {
        lpa.enableProfile(profile);
    }

    public void disableProfile(ProfileMetadata profile) {
        lpa.disableProfile(profile);
    }

    public void deleteProfile(ProfileMetadata profile) {
        lpa.deleteProfile(profile);
    }

    public void setNickname(ProfileMetadata profile) {
        lpa.setNickname(profile);
    }

    public void handleAndClearAllNotifications() {
        lpa.handleAndClearAllNotifications();
    }

    public void authenticate(ActivationCode activationCode) {
        lpa.startAuthentication(activationCode);
    }

    public AuthenticateResult getAuthenticateResult() {
        return lpa.getAuthenticateResult();
    }

    public void downloadProfile(String confirmationCode) {
        lpa.startProfileDownload(confirmationCode);
    }

    public DownloadResult getDownloadResult() {
        return lpa.getDownloadResult();
    }

    public void cancelSession(long cancelSessionReason) {
        lpa.startCancelSession(cancelSessionReason);
    }

    public CancelSessionResult getCancelSessionResult() {
        return lpa.getCancelSessionResult();
    }

    // endregion

    // region Status and error handling

    @Override
    public void onStatusChange(AsyncActionStatus newAsyncActionStatus) {
        actionStatusLiveData.postValue(newAsyncActionStatus);
    }

    @Override
    public void onStatusChange(ActionStatus actionStatus) {
        Log.debug(TAG, "Changing action status to: " + actionStatus);
        actionStatusLiveData.postValue(new AsyncActionStatus(actionStatus));
    }

    @Override
    public void onError(Error error) {
        Log.error(TAG, error.getHeader() + ": " + error.getBody());
        triggerErrorEvent(error);
    }

    private void triggerErrorEvent(Error error) {
        Log.debug(TAG,"Setting new error event.");
        OneTimeEvent<Error> newErrorEvent = new OneTimeEvent<>(error);
        if(errorEventLiveData != null) {
            errorEventLiveData.setValue(newErrorEvent);
        }
    }

    // endregion
}
