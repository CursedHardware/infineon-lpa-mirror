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

package com.infineon.esim.lpa.euicc;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.infineon.esim.lpa.data.Preferences;
import com.infineon.esim.lpa.data.StatusAndEventHandler;
import com.infineon.esim.lpa.euicc.base.EuiccConnection;
import com.infineon.esim.lpa.euicc.base.EuiccConnectionConsumer;
import com.infineon.esim.lpa.euicc.base.EuiccInterface;
import com.infineon.esim.lpa.euicc.base.EuiccInterfaceStatusChangeHandler;
import com.infineon.esim.lpa.euicc.base.task.ConnectInterfaceTask;
import com.infineon.esim.lpa.euicc.base.task.DisconnectReaderTask;
import com.infineon.esim.lpa.euicc.base.task.RefreshEuiccNamesTask;
import com.infineon.esim.lpa.euicc.base.task.SwitchEuiccTask;
import com.infineon.esim.lpa.euicc.usbreader.USBReaderEuiccInterface;
import com.infineon.esim.lpa.euicc.se.SeEuiccInterface;
import com.infineon.esim.lpa.ui.generic.ActionStatus;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.ui.generic.Error;
import com.infineon.esim.lpa.util.threading.TaskRunner;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EuiccManager implements EuiccInterfaceStatusChangeHandler {
    private static final String TAG = EuiccManager.class.getName();

    // eUICC interfaces
    private final List<EuiccInterface> euiccInterfaces;

    // eUICC connection
    private EuiccConnection currentEuiccConnection;

    // eUICCs
    private final MutableLiveData<String> currentEuicc;
    private final MutableLiveData<List<String>> euiccList;

    private final StatusAndEventHandler statusAndEventHandler;

    private EuiccConnectionConsumer euiccConnectionConsumer;

    private String switchEuiccInterface;
    private boolean enableFallbackEuicc;
    private boolean refreshEuiccRetry;

    public EuiccManager(Context context,
                        StatusAndEventHandler statusAndEventHandler) {
        Log.debug(TAG,"Constructor");

        this.switchEuiccInterface = null;

        this.currentEuicc = new MutableLiveData<>();
        this.euiccList = new MutableLiveData<>();

        this.statusAndEventHandler = statusAndEventHandler;
        this.euiccConnectionConsumer = null;

        euiccInterfaces = new ArrayList<>();
        euiccInterfaces.add(new SeEuiccInterface(context, this));
        euiccInterfaces.add(new USBReaderEuiccInterface(context, this));
    }

    public LiveData<String> getCurrentEuiccLiveData() {
        return currentEuicc;
    }

    public LiveData<List<String>> getEuiccListLiveData() {
        return euiccList;
    }

    public void setEuiccConnectionConsumer(EuiccConnectionConsumer euiccConnectionConsumer) {
        this.euiccConnectionConsumer = euiccConnectionConsumer;
    }

    private void updateEuiccConnectionOnConsumer(EuiccConnection euiccConnection) {
        Log.debug(TAG, "Updating eUICC connection on consumer.");
        if(euiccConnectionConsumer != null) {
            euiccConnectionConsumer.onEuiccConnectionUpdate(euiccConnection);
        }
    }

    public void initializeInterfaces() {
        boolean atLeastOneInterfaceIsAvailable = false;

        // Connect the SE interface
        for(EuiccInterface euiccInterface : euiccInterfaces) {
            if(euiccInterface.getTag().equals(SeEuiccInterface.INTERFACE_TAG)
            || euiccInterface.getTag().equals(USBReaderEuiccInterface.INTERFACE_TAG)) {
                if (euiccInterface.isAvailable()) {
                    atLeastOneInterfaceIsAvailable = true;
                    startConnectingEuiccInterface(euiccInterface, true);
                }
            }
        }

        if(!atLeastOneInterfaceIsAvailable) {
            selectEuicc(Preferences.getNoEuiccName());
        }
    }

    public void selectEuicc(String euiccName) {
        Log.debug(TAG, "Switch eUICC to: " + euiccName);

        if(euiccName.equals(currentEuicc.getValue())) {
            Log.debug(TAG, "eUICC already active! No switch needed!");
            return;
        }

        if(euiccName.equals(Preferences.getNoEuiccName())) {
            onEuiccConnected(euiccName, null);
        } else {
            startEuiccInitialization(euiccName);
        }
    }

    public boolean isEuiccInterfaceConnected(String interfaceTag) {
        Log.debug(TAG,"Getting if " + interfaceTag + " interface is connected.");

        try {
            return getEuiccInterfaceFromTag(interfaceTag).isInterfaceConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public void startConnectingEuiccInterface(String interfaceTag) {
        try {
            EuiccInterface euiccInterface = getEuiccInterfaceFromTag(interfaceTag);

            startConnectingEuiccInterface(euiccInterface, true);
        } catch (Exception e) {
            statusAndEventHandler.onError(new Error("Exception during connecting eUICC interface " + interfaceTag+ ".", e.getMessage()));
        }
    }

    // region eUICC tasks

    public void startConnectingEuiccInterface(EuiccInterface euiccInterface, boolean switchToDefaultEuicc) {
        Log.debug(TAG,"Connecting eUICC interface " + euiccInterface.getTag() + "...");

        TaskRunner.ExceptionHandler exceptionHandler = e -> statusAndEventHandler.onError(new Error("Exception during connecting eUICC interface" + euiccInterface.getTag() + ".", e.getMessage()));

        try {
            statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.CONNECTING_INTERFACE_STARTED).addExtras(euiccInterface.getTag()));

            if(switchToDefaultEuicc) {
                switchEuiccInterface = euiccInterface.getTag();
            }

            new TaskRunner().executeAsync(new ConnectInterfaceTask(euiccInterface),
                    ignore -> {}, // Do nothing and wait for onConnected
                    exceptionHandler);
        } catch (Exception e) {
            exceptionHandler.onException(e);
        }
    }

    public void startDisconnectingInterface(String interfaceTag) {
        Log.debug(TAG,"Disconnecting interface " + interfaceTag + "...");

        TaskRunner.ExceptionHandler exceptionHandler = e -> statusAndEventHandler.onError(new Error("Exception during disconnecting interface" + interfaceTag + " .", e.getMessage()));

        try {
            statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.DISCONNECTING_INTERFACE_STARTED).addExtras(interfaceTag));

            EuiccInterface euiccInterface = getEuiccInterfaceFromTag(interfaceTag);
            new TaskRunner().executeAsync(new DisconnectReaderTask(euiccInterface),
                    result -> {
                        // Do nothing and wait for onDisconnected
                    },
                    exceptionHandler);
        } catch (Exception e) {
            exceptionHandler.onException(e);
        }
    }

    public void startRefreshingEuiccList() {
        Log.debug(TAG,"Refreshing eUICC list.");

        TaskRunner.ExceptionHandler exceptionHandler = e -> statusAndEventHandler.onError(new Error("Exception during refreshing eUICC list.", e.getMessage()));

        try {
            statusAndEventHandler.onStatusChange(ActionStatus.REFRESHING_EUICC_LIST_STARTED);

            new TaskRunner().executeAsync(new RefreshEuiccNamesTask(euiccInterfaces),
                    result -> {
                        statusAndEventHandler.onStatusChange(ActionStatus.REFRESHING_EUICC_LIST_FINISHED);
                        onEuiccListRefreshed(result);
                    },
                    exceptionHandler);
        } catch (Exception e) {
            exceptionHandler.onException(e);
        }
    }


    private void startEuiccInitialization(String euiccName) {
        Log.debug(TAG,"Initializing eUICC \"" + euiccName + "\" as task.");

        TaskRunner.ExceptionHandler exceptionHandler = e -> {
            statusAndEventHandler.onError(new Error("Exception during initializing eUICC", e.getMessage()));
            statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.OPENING_EUICC_CONNECTION_FINISHED));
            selectEuicc(Preferences.getNoEuiccName());
            onEuiccInterfaceDisconnected(null);
        };

        try {
            statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.OPENING_EUICC_CONNECTION_STARTED).addExtras(euiccName));
            EuiccConnection oldEuiccConnection = currentEuiccConnection;
            EuiccConnection newEuiccConnection = getEuiccConnectionFromName(euiccName);

            new TaskRunner().executeAsync(new SwitchEuiccTask(oldEuiccConnection, newEuiccConnection, Preferences.getReaderSettings()),
                    success -> {
                        statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.OPENING_EUICC_CONNECTION_FINISHED).addExtras(euiccName));

                        if (success) {
                            currentEuiccConnection = newEuiccConnection;
                            onEuiccConnected(euiccName, newEuiccConnection);
                        } else {
                            selectEuicc(Preferences.getNoEuiccName());
                        }
                    },
                    exceptionHandler);
        } catch (Exception e) {
            exceptionHandler.onException(e);
        }
    }

    // endregion

    // region eUICC connection handler

    @Override
    public void onEuiccInterfaceConnected(String interfaceTag) {
        Log.debug(TAG, "Handling connect of interface: " + interfaceTag);
        statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.CONNECTING_INTERFACE_FINISHED).addExtras(interfaceTag));

        startRefreshingEuiccList();
    }

    @Override
    public void onEuiccInterfaceDisconnected(String interfaceTag) {
        Log.debug(TAG, "Handling disconnect of interface: " + interfaceTag);
        statusAndEventHandler.onStatusChange(new AsyncActionStatus(ActionStatus.DISCONNECTING_INTERFACE_FINISHED).addExtras(interfaceTag));

        // Enable initialisation of fallback eUICC after eUICC list refresh
        enableFallbackEuicc = true;

        if (interfaceTag == null) {
            Log.error(TAG,"interfaceTAG is null");
            if (Preferences.getRetryWhenFail() && Preferences.getAntiRetrySpam()) refreshEuiccRetry = !refreshEuiccRetry;
        }

        if (interfaceTag != null | !Preferences.getAntiRetrySpam() | (Preferences.getRetryWhenFail() && refreshEuiccRetry)) {
            if (refreshEuiccRetry) Log.debug(TAG,"Retry when interfaceTAG is null");
            startRefreshingEuiccList();
        }
    }

    @Override
    public void onEuiccConnected(String euiccName, EuiccConnection euiccConnection) {
        Log.debug(TAG, "Euicc initialized: " + euiccName);

        enableFallbackEuicc = false;
        Preferences.setEuiccName(euiccName);
        currentEuicc.postValue(euiccName);

        updateEuiccConnectionOnConsumer(euiccConnection);
    }

    @Override
    public void onEuiccListRefreshed(List<String> euiccList) {
        Log.debug(TAG, "eUICC list has been refreshed: " + euiccList);
        this.euiccList.postValue(euiccList);

        if(euiccList.isEmpty()) {
            Log.verbose(TAG, "eUICC list is Empty");
            selectEuicc(Preferences.getNoEuiccName());
        } else {
            Log.verbose(TAG, "Current eUICC connection: " + currentEuicc.getValue());
            if (switchEuiccInterface != null) {
                String euiccName = getDefaultEuiccNameFromTag(switchEuiccInterface);
                Log.verbose(TAG, "Switch eUICC after refresh: " + euiccName);
                selectEuicc(euiccName);
                switchEuiccInterface = null;
            } else if (enableFallbackEuicc) {
                String euiccName = getFallbackEuicc();
                Log.debug(TAG, "Enable fallback eUICC: " + euiccName);
                selectEuicc(euiccName);
            } else if(Objects.equals(currentEuicc.getValue(), Preferences.getNoEuiccName())) {
                Log.debug(TAG, "Current SIM: " + currentEuicc.getValue());
                Log.debug(TAG, "There is no eUICC enabled but a eUICC detected. Enable the fallback eUICC.");
                String euiccName = getFallbackEuicc();
                selectEuicc(euiccName);
            }
        }
    }

    // endregion

    // Private methods

    private String getDefaultEuiccNameFromTag(String interfaceTag)  {
        String defaultEuiccName = Preferences.getNoEuiccName();

        try {
            EuiccInterface euiccInterface = getEuiccInterfaceFromTag(interfaceTag);
            List<String> euiccNames = euiccInterface.getEuiccNames();

            for (String readerName : euiccNames) {
                defaultEuiccName = readerName;
                break;
            }

        } catch (Exception e) {
            statusAndEventHandler.onError(new Error("Error getting default reader name for reader " + interfaceTag + ".", e.getMessage()));
        }

        Log.debug(TAG, "Getting default reader name for reader tag \"" + interfaceTag + "\": \"" + defaultEuiccName + "\".");
        return defaultEuiccName;
    }

    private String getFallbackEuicc() {
        Log.debug(TAG, "Searching for fallback eUICC...");
        String fallbackEuicc = Preferences.getNoEuiccName();

        for (EuiccInterface euiccInterface : euiccInterfaces) {
            if(euiccInterface.isAvailable()) {
                List<String> euiccNamesForInterface = euiccInterface.getEuiccNames();
                Log.debug(TAG, euiccInterface.getTag() + " - " + euiccNamesForInterface);

                for (String euiccNameLocal : euiccNamesForInterface) {
                    fallbackEuicc = euiccNameLocal;
                }
            }
        }

        Log.debug(TAG, "Fallback eUICC: " + fallbackEuicc);
        return fallbackEuicc;
    }

    private EuiccConnection getEuiccConnectionFromName(String euiccName) throws Exception {
        Log.debug(TAG, "Getting eUICC connection for \"" + euiccName + "\"");

        for(EuiccInterface euiccInterface : euiccInterfaces) {
            List<String> euiccNamesForInterface = euiccInterface.getEuiccNames();
            for(String euiccNameLocal : euiccNamesForInterface) {
                if(euiccNameLocal.equals(euiccName)) {
                    return euiccInterface.getEuiccConnection(euiccName);
                }
            }
        }

        Log.error(TAG, "Error: eUICC with name " + euiccName + " not found in any interface.");
        throw new Exception("Error: eUICC with name " + euiccName + " not found in any interface.");
    }

    private EuiccInterface getEuiccInterfaceFromTag(String interfaceTag) throws Exception {
        for(EuiccInterface euiccInterface : euiccInterfaces) {
            if(euiccInterface.getTag().equals(interfaceTag)) {
                return euiccInterface;
            }
        }

        statusAndEventHandler.onError(new Error("Interface not found!", "Error: interface with tag \"" + interfaceTag + "\" not found."));

        Log.error("Error: Interface with tag \"" + interfaceTag + "\" not found.");
        throw new Exception("Error: Interface with tag \"" + interfaceTag + "\" not found.");
    }
}
