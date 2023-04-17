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

package com.infineon.esim.lpa;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import androidx.preference.PreferenceManager;

import com.infineon.esim.lpa.core.es9plus.TlsUtil;
import com.infineon.esim.lpa.data.DataModel;
import com.infineon.esim.lpa.data.Preferences;
import com.infineon.esim.lpa.util.android.IO;
import com.infineon.esim.lpa.util.android.NetworkStatus;
import com.infineon.esim.util.Log;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

public class Application extends android.app.Application {
    private static final String TAG = Application.class.getName();

    private static Context applicationContext;
    private static Resources resources;

    // Intent extra identifiers
    public static final String INTENT_EXTRA_PROFILE_METADATA = "com.infineon.esim.lpa.PROFILE_METADATA";
    public static final String INTENT_EXTRA_ACTIVATION_CODE = "com.infineon.esim.lpa.ACTIVATION_CODE";

    @Override
    public void onCreate() {
        Log.debug(TAG, "Initializing application.");
        super.onCreate();

        // Initialize resources and app context
        resources = getResources();
        applicationContext = getApplicationContext();

        // Register network callback for network status
        NetworkStatus.registerNetworkCallback();

        // Initialize data model and preferences holder
        DataModel.initializeInstance(this);
        Preferences.initializeInstance();

        // Set trusted Root CAs
        initializeTrustedRootCas();
    }

    public static Context getAppContext() {
        return applicationContext;
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public static String getStringResource(int id) {
        return resources.getString(id);
    }

    public static PackageManager getPacketManager() {
        return applicationContext.getPackageManager();
    }

    public static UsbManager getUsbManager() {
        return (UsbManager) applicationContext.getSystemService(Context.USB_SERVICE);
    }

    public static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static WifiManager getWifiManager() {
        return (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
    }

    public static void initializeTrustedRootCas() {
        List<Certificate> liveCertificates = new ArrayList<>();
        liveCertificates.add(IO.readCertificateFromResource(resources, R.raw.symantec_gsma_rspv2_root_ci1_pem));

        List<Certificate> testCertificates = new ArrayList<>();
        testCertificates.add(IO.readCertificateFromResource(resources, R.raw.gsma_test_root_ca_cert_pem));
        testCertificates.add(IO.readCertificateFromResource(resources, R.raw.gsma_root_ci_test_1_2_pem));
        testCertificates.add(IO.readCertificateFromResource(resources, R.raw.gsma_root_ci_test_1_5_pem));
        TlsUtil.initializeCertificates(liveCertificates, testCertificates);

        boolean trustTestCertificates = Preferences.getTrustGsmaTestCi();
        TlsUtil.setTrustLevel(trustTestCertificates);
    }
}
