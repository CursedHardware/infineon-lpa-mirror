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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.infineon.esim.lpa.data.DataModel;
import com.infineon.esim.lpa.data.Preferences;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.android.OneTimeEvent;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PreferenceViewModel extends ViewModel {
    private static final String TAG = PreferenceViewModel.class.getName();

    private final DataModel dataModel;

    public PreferenceViewModel() {
        this.dataModel = DataModel.getInstance();
    }

    public LiveData<AsyncActionStatus> getActionStatus() {
        return dataModel.getAsyncActionStatusLiveData();
    }

    public LiveData<OneTimeEvent<Error>> getErrorEvent() {
        return dataModel.getErrorEventLiveData();
    }

    public LiveData<List<String>> getEuiccListLiveData() {
        return dataModel.getEuiccListLiveData();
    }

    public LiveData<String> getCurrentEuiccLiveData() {
        return dataModel.getCurrentEuiccLiveData();
    }

    public List<String> getEuiccList() {
        List<String> euiccList = new ArrayList<>();
        LiveData<List<String>> euiccListLiveData = getEuiccListLiveData();
        if(euiccListLiveData.getValue() != null) {
            euiccList.addAll(euiccListLiveData.getValue());
        }

        return euiccList;
    }

    public String getCurrentEuicc() {
        LiveData<String> currentEuiccLiveData = getCurrentEuiccLiveData();
        return currentEuiccLiveData.getValue();
    }

    public void savePreferences() {
        Log.debug(TAG, "Saving preferences.");
        if(Preferences.havePreferencesChanged()) {
            Log.debug(TAG, "Preferences have changed. Select new eUICC: " + Preferences.getEuiccName());
            dataModel.selectEuicc(Preferences.getEuiccName());
        }
    }
}
