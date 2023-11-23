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

package com.infineon.esim.lpa.euicc.identive;

import android.content.Context;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.euicc.base.EuiccConnection;
import com.infineon.esim.lpa.euicc.base.EuiccInterface;
import com.infineon.esim.lpa.euicc.base.EuiccInterfaceStatusChangeHandler;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final public class IdentiveEuiccInterface implements EuiccInterface {
    private static final String TAG = IdentiveEuiccInterface.class.getName();

    public static final String INTERFACE_TAG = "USB";

    public static final List<String> READER_NAMES = new ArrayList<>(Arrays.asList(
            "SCR3500 A Contact Reader",
            "Identive CLOUD 4700 F Dual Interface Reader",
            "Identiv uTrust 4701 F Dual Interface Reader",
            "CLOUD 2700 R Smart Card Reader"
    ));

    private final EuiccInterfaceStatusChangeHandler euiccInterfaceStatusChangeHandler;
    private final IdentiveService identiveService;
    private final List<String> euiccNames;

    private EuiccConnection euiccConnection;

    public IdentiveEuiccInterface(Context context, EuiccInterfaceStatusChangeHandler euiccInterfaceStatusChangeHandler) {
        Log.debug(TAG, "Constructor of IdentiveReader.");

        this.identiveService = new IdentiveService(context);
        this.euiccNames = new ArrayList<>();

        this.euiccInterfaceStatusChangeHandler = euiccInterfaceStatusChangeHandler;

        // Create BroadcastReceiver for USB attached/detached events
        IdentiveConnectionBroadcastReceiver identiveConnectionBroadcastReceiver = new IdentiveConnectionBroadcastReceiver(Application.getAppContext(), onDisconnectCallback);
        identiveConnectionBroadcastReceiver.registerReceiver();
    }

    @Override
    public String getTag() {
        return INTERFACE_TAG;
    }

    @Override
    public boolean isAvailable() {
        boolean isAvailable = IdentiveConnectionBroadcastReceiver.isDeviceAttached();

        Log.debug(TAG, "Checking if Identive eUICC interface is available: " + isAvailable);

        return isAvailable;
    }

    @Override
    public boolean isInterfaceConnected() {
        boolean isConnected = false;
        if(isAvailable()) {
            isConnected = identiveService.isConnected();
        }
        Log.debug(TAG, "Is Identive interface connected: " + isConnected);
        return isConnected;
    }

    @Override
    public boolean connectInterface() throws Exception {
        Log.debug(TAG, "Connecting Identive interface.");
        identiveService.connect();

        if(identiveService.isConnected()) {
            euiccInterfaceStatusChangeHandler.onEuiccInterfaceConnected(INTERFACE_TAG);
        }

        return identiveService.isConnected();
    }

    @Override
    public boolean disconnectInterface() throws Exception {
        Log.debug(TAG, "Disconnecting Identive interface.");

        if(euiccConnection != null) {
            euiccConnection.close();
            euiccConnection = null;
        }

        identiveService.disconnect();

        euiccNames.clear();

        return !identiveService.isConnected();
    }

    @Override
    public List<String> refreshEuiccNames() {
        euiccNames.clear();

        if(isInterfaceConnected()) {
            euiccNames.addAll(identiveService.refreshEuiccNames());
        }

        return euiccNames;
    }

    @Override
    public synchronized List<String> getEuiccNames() {
        return euiccNames;
    }

    @Override
    public EuiccConnection getEuiccConnection(String euiccName) throws Exception {

        if(isNotYetOpen(euiccName)) {
            // Close the old eUICC connection if it is with another eUICC
            if(euiccConnection != null) {
                euiccConnection.close();
            }

            // Open new eUICC connection
            euiccConnection = identiveService.openEuiccConnection(euiccName);
        }

        return euiccConnection;
    }

    private boolean isNotYetOpen(String euiccName) {
        if(euiccConnection == null) {
            return true;
        } else {
            return !euiccConnection.getEuiccName().equals(euiccName);
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final IdentiveConnectionBroadcastReceiver.OnDisconnectCallback onDisconnectCallback = new IdentiveConnectionBroadcastReceiver.OnDisconnectCallback() {
        @Override
        public void onDisconnect() {
            Log.debug(TAG, "Identive reader has been disconnected.");

            try {
                disconnectInterface();
            } catch (Exception e) {
                Log.error(TAG, "Catched exception during disconnecting interface.", e);
            }

            euiccInterfaceStatusChangeHandler.onEuiccInterfaceDisconnected(INTERFACE_TAG);
        }
    };
}
