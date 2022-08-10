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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.infineon.esim.lpa.core.dtos.profile.ProfileList;
import com.infineon.esim.lpa.data.DataModel;
import com.infineon.esim.lpa.euicc.identive.IdentiveConnectionBroadcastReceiver;
import com.infineon.esim.lpa.euicc.identive.IdentiveEuiccInterface;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.android.OneTimeEvent;
import com.infineon.esim.util.Log;

public class ProfileListViewModel extends ViewModel {
    private static final String TAG = ProfileListViewModel.class.getName();

    private final DataModel dataModel;

    public ProfileListViewModel() {
        this.dataModel = DataModel.getInstance();
    }

    public void selectFreshlyAttachedUsbReader() {
        // Check if USB reader really has been freshly attached
        try {
            if (IdentiveConnectionBroadcastReceiver.hasBeenFreshlyAttached()) {
                Log.debug(TAG, "USB reader is freshly attached.");
                connectIdentiveEuiccInterface();
            }
        } catch (Exception e) {
            dataModel.onError(new Error("Exception during switching to freshly attached USB reader.", e.getMessage()));
        }
    }

    public void connectIdentiveEuiccInterface() {
        Log.debug(TAG,"Connecting Identive eUICC interface...");
        dataModel.startConnectingEuiccInterface(IdentiveEuiccInterface.INTERFACE_TAG);
    }

    public LiveData<String> getEuiccNameLiveData() {
        return dataModel.getCurrentEuiccLiveData();
    }

    public LiveData<ProfileList> getProfileListLiveData() {
        return dataModel.getProfileListLiveData();
    }

    public LiveData<AsyncActionStatus> getActionStatusLiveData() {
        return dataModel.getAsyncActionStatusLiveData();
    }

    public LiveData<OneTimeEvent<Error>> getError() {
        return dataModel.getErrorEventLiveData();
    }

    public void refreshProfileList() {
        dataModel.refreshProfileList();
    }

    public void refreshEuiccs() {
        dataModel.refreshEuiccs();
    }

    public void clearAllNotifications() {
        dataModel.handleAndClearAllNotifications();
    }

}
