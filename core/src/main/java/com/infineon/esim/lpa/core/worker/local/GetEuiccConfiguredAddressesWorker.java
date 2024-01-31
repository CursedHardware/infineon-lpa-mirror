package com.infineon.esim.lpa.core.worker.local;

import com.gsma.sgp.messages.rspdefinitions.EuiccConfiguredAddressesResponse;
import com.infineon.esim.lpa.core.es10.Es10Interface;
import com.infineon.esim.util.Log;

public class GetEuiccConfiguredAddressesWorker {
    private static final String TAG = GetEuiccConfiguredAddressesWorker.class.getName();

    private final Es10Interface es10Interface;

    public GetEuiccConfiguredAddressesWorker(Es10Interface es10Interface) {
        this.es10Interface = es10Interface;
    }

    public String getEuiccConfiguredAddresses() throws Exception {
        Log.debug(TAG, "Getting the EuiccConfiguredAddresses of the eUICC...");

        EuiccConfiguredAddressesResponse euiccConfiguredAddressesResponse = es10Interface.es10a_getEuiccConfiguredAddresses();

        return euiccConfiguredAddressesResponse.toString();
    }
}
