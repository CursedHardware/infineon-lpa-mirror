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

package com.infineon.esim.lpa.euicc.base.generic;

import com.infineon.esim.util.Bytes;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Atr {
    private static final String TAG = Atr.class.getName();

    // Allowed/valid ATRs of tested eUICCs. Extend on own risk.
    private static final List<String> VALID_ATR_PARTS = new ArrayList<>(Arrays.asList(
            "3B9F96803FC7828031E073F6215757A44D000560700014",
            "3B9F96803FC7828031E075F62157200355020B60500018",
            "3B9F96803FC7828031E073F62157574A4D0005608000E4",
            "3B9F96803FC7828031E073F62157574A4D0005609000F4",
            "3B9F96803FC7828031E073F62157574A4D020B60010069",
            "3B9793803FC7828031E073FE211310",
            "3B9793801FC78031E073FE2113B2"
    ));

    public static Boolean isAtrValid(byte[] atr) {
        if (atr != null) {
            String atrStr = Bytes.encodeHexString(atr);
            Log.verbose(TAG, "ATR part for comparison: " + atrStr);

            for(String validAtrPart : VALID_ATR_PARTS) {
                if(atrStr.contains(validAtrPart)) {
                    return true;
                }
            }

            boolean isEuicc = false;
            int i = 1;
            int pn = 1;
            byte atrBytes = atr[i];

            while (i < atr.length) {
//                Log.verbose(TAG,"atrBytes: " + String.format("%02X", atrBytes & 0xFF));

                // TAi
                if ((atrBytes & 0x10) != 0) {
                    i += 1;
                    int TAi = atr[i] & 0xFF;
//                    Log.verbose(TAG,"TAi: " + String.format("%02X", TAi));
                }

                // TBi
                if ((atrBytes & 0x20) != 0) {
                    i += 1;
                    int TBi = atr[i] & 0xFF;
                    int protocol = atrBytes & 0xF;

                    if (pn > 2 && protocol == 15 && (TBi & 0x82) == 0x82) {
                        isEuicc = true;
                        Log.verbose(TAG, "Found euicc support but not in list");
                    }
//                    Log.verbose(TAG,"TBi: " + String.format("%02X", TBi));
                }

                // TCi
                if ((atrBytes & 0x40) != 0) {
                    i += 1;
                    int TCi = atr[i] & 0xFF;
//                    Log.verbose(TAG,"TCi: " + String.format("%02X", TCi));
                }

                // TDi
                if ((atrBytes & 0x80) != 0) {
                    i += 1;
                    atrBytes = atr[i];
//                    Log.verbose(TAG,"TDi: " + String.format("%02X", atrBytes & 0xFF));
                    pn += 1;
                } else break;
            }
            if (isEuicc) return true;
        }
        return false;
    }
}
