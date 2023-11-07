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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.acs.smartcard.Reader;

import java.util.ArrayList;
import java.util.List;

public class ACSCard {
    private static final String TAG = ACSCard.class.getName();
    private Context context;
    private UsbManager mManager;
    private Reader mReader;

    public ACSCard(Context context) {
        this.context = context;
        mManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mManager);
    }

    List<String> getReaderNames() {
        List<String> euiccNames = new ArrayList<>();

        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                euiccNames.add(device.getDeviceName());
            }
        }

        return euiccNames;
    }

    boolean isConnected() {
        return false;
    }

    void establishContext() throws Exception {
    }

    void releaseContext() throws Exception {
    }

    void connectCard(String cardName) throws Exception {
//        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
//                ACTION_USB_PERMISSION), 0);
//        UsbDevice mDevice = mManager.getDeviceList().get(cardName);
//        mManager.requestPermission(mDevice, mPermissionIntent);
    }

    void disconnectCard() throws Exception {
    }

    void resetCard() throws Exception {
    }

    byte[] transmitToCard(byte[] command) {
        byte[] response = null;

        return response;
    }
}
