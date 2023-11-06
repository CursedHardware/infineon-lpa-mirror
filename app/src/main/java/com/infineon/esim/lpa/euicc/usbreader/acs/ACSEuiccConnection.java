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

package com.infineon.esim.lpa.euicc.usbreader.acs;


import com.infineon.esim.lpa.euicc.EuiccConnectionSettings;
import com.infineon.esim.lpa.euicc.base.EuiccConnection;
import com.infineon.esim.lpa.euicc.base.generic.ISO7816Channel;
import com.infineon.esim.util.Log;

import java.util.List;

public class ACSEuiccConnection implements EuiccConnection, ISO7816Channel.ApduTransmitter {
    private static final String TAG = ACSEuiccConnection.class.getName();

    private final String euiccName;
    private final ACSCard ACSCard;
    private final ISO7816Channel iso7816Channel;

    private EuiccConnectionSettings euiccConnectionSettings;

    public ACSEuiccConnection(ACSCard ACSCard, String euiccName) {
        this.ACSCard = ACSCard;
        this.euiccName = euiccName;
        this.iso7816Channel = new ISO7816Channel(this);
    }

    @Override
    public void updateEuiccConnectionSettings(EuiccConnectionSettings euiccConnectionSettings) {
        this.euiccConnectionSettings = euiccConnectionSettings;
    }

    @Override
    public String getEuiccName() {
        return euiccName;
    }


    @Override
    public boolean open() throws Exception {
        Log.debug(TAG, "Opening Identive interface...");

        // Open connection to card
        ACSCard.connectCard(euiccName);

        // Open (logical) channel to ISD-R
        iso7816Channel.openChannel(euiccConnectionSettings);

        Log.debug(TAG, "Opening Identive interface result: " + isOpen());
        return isOpen();
    }

    @Override
    public void close() throws Exception {
        Log.debug(TAG, "Closing Identive eUICC connection...");
        if(isOpen()) {
            // Close (logical) channel
            iso7816Channel.closeChannel(euiccConnectionSettings);

            // Disconnect card
            ACSCard.disconnectCard();
        }
    }

    @Override
    public boolean isOpen() {
        return ACSCard.isConnected();
    }

    @Override
    public boolean resetEuicc() throws Exception {
        Log.debug(TAG, "Resetting card.");

        // Close (logical) channel
        iso7816Channel.closeChannel(euiccConnectionSettings);

        // Reset card
        ACSCard.resetCard();

        // Open (logical) channel
        iso7816Channel.openChannel(euiccConnectionSettings);

        return isOpen();
    }

    @Override
    public List<String> transmitAPDUS(List<String> apdus) throws Exception {
        if(!isOpen()) {
            open();
        }

        return iso7816Channel.transmitAPDUS(apdus);
    }

    @Override
    public byte[] transmit(byte[] command) {
        return ACSCard.transmitToCard(command);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

}
