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

package com.infineon.esim.lpa.ui.euiccDetails;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.EuiccInfo;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EuiccDetailsActivity extends AppCompatActivity {
    private static final String TAG = EuiccDetailsActivity.class.getName();

    private EuiccDetailsViewModel viewModel;

    private TextView textViewEid;
    private TextView textViewConfiguredAddresses;
    private TextView textViewTcaVersion;
    private TextView textViewGsmaVersion;
    private TextView textViewFirmwareVer;
    private TextView textViewExtCardResource;
    private TextView textViewExtCardResource_memory;
    private TextView textViewUiccCapability;
    private TextView textViewJavaCardVersion;
    private TextView textViewGlobalPlatformVersion;
    private TextView textViewRspCapability;
    private TextView textViewPkiIds;
    private TextView textVieweUICCCategory;
    private TextView textViewForbiddenProfilePolicyRules;
    private TextView textViewProtectionProfileVersion;
    private TextView textViewSasAccreditationNumber;
    private TextView textViewCertificationDataObject;

    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_euicc_details);
        Log.debug(TAG, "Created activity.");

        // Get the view model
        viewModel = new ViewModelProvider(this).get(EuiccDetailsViewModel.class);
        viewModel.getActionStatus().observe(this, actionStatusObserver);

        // Attach UI
        attachUi();

        // Refreshing eUICC info
        viewModel.refreshEuiccInfo();
    }

    @Override
    protected void onPause() {
        Log.debug(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.debug(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onStop() {
        // App crashes on Oppo Reno Z when starting with Android studio if not used...
        super.onStop();
        finish();
    }

    private void attachUi() {
        textViewEid = findViewById(R.id.text_eid);
        textViewConfiguredAddresses = findViewById(R.id.text_configured_addresses);
        textViewTcaVersion = findViewById(R.id.text_tca_version);
        textViewGsmaVersion = findViewById(R.id.text_gsma_version);
        textViewFirmwareVer = findViewById(R.id.text_firmware_version);
        textViewExtCardResource = findViewById(R.id.text_extCardResource);
        textViewExtCardResource_memory = findViewById(R.id.text_extCardResource_memory);
        textViewUiccCapability = findViewById(R.id.text_uicc_capability);
        textViewJavaCardVersion = findViewById(R.id.text_ts102241_version);
        textViewGlobalPlatformVersion = findViewById(R.id.text_globalplatform_version);
        textViewRspCapability = findViewById(R.id.text_rsp_capability);
        textViewPkiIds = findViewById(R.id.text_pki_ids);
        textVieweUICCCategory = findViewById(R.id.text_euicc_category);
        textViewForbiddenProfilePolicyRules = findViewById(R.id.text_forbiddenProfilePolicyRules);
        textViewProtectionProfileVersion = findViewById(R.id.text_protection_profile_version);
        textViewSasAccreditationNumber = findViewById(R.id.text_sas_accreditation);
        textViewCertificationDataObject = findViewById(R.id.text_certificationDataObject);

        textViewEid.setText("");
        textViewConfiguredAddresses.setText("");
        textViewTcaVersion.setText("");
        textViewGsmaVersion.setText("");
        textViewFirmwareVer.setText("");
        textViewExtCardResource.setText("");
        textViewExtCardResource_memory.setText("");
        textViewUiccCapability.setText("");
        textViewJavaCardVersion.setText("");
        textViewGlobalPlatformVersion.setText("");
        textViewRspCapability.setText("");
        textViewPkiIds.setText("");
        textVieweUICCCategory.setText("");
        textViewForbiddenProfilePolicyRules.setText("");
        textViewProtectionProfileVersion.setText("");
        textViewSasAccreditationNumber.setText("");
        textViewCertificationDataObject.setText("");
    }

    private void setEuiccInfo(EuiccInfo euiccInfo) {
        String extCardResource = euiccInfo.getExtCardResource();
        String pkiIds = parsePkiList(euiccInfo.getPkiIdsForSign(),euiccInfo.getPkiIdsForVerify());
        ArrayList<String> memoryData = parseMemoryData(extCardResource);

        textViewEid.setText(euiccInfo.getEid());
        textViewConfiguredAddresses.setText(euiccInfo.getConfiguredAddresses());
        textViewTcaVersion.setText(euiccInfo.getProfileVersion());
        textViewGsmaVersion.setText(euiccInfo.getSvn());
        textViewFirmwareVer.setText(euiccInfo.getEuiccFirmwareVer());
        textViewExtCardResource.setText(euiccInfo.getExtCardResource());
        textViewExtCardResource_memory.setText(getString(R.string.euicc_info_ext_card_resource_memory_example, memoryData.get(0), memoryData.get(1)));
        textViewUiccCapability.setText(euiccInfo.getUiccCapability());
        textViewJavaCardVersion.setText(euiccInfo.getTs102241Version());
        textViewGlobalPlatformVersion.setText(euiccInfo.getGlobalplatformVersion());
        textViewRspCapability.setText(euiccInfo.getRspCapability());
        textViewPkiIds.setText(pkiIds);
        textVieweUICCCategory.setText(euiccInfo.getEuiccCategory());
        textViewForbiddenProfilePolicyRules.setText(euiccInfo.getForbiddenProfilePolicyRules());
        textViewProtectionProfileVersion.setText(euiccInfo.getPpVersion());
        textViewSasAccreditationNumber.setText(euiccInfo.getSasAcreditationNumber());
        textViewCertificationDataObject.setText(euiccInfo.getCertificationDataObject());
    }

    private void dismissProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private static ArrayList<String> parseMemoryData(String hexData) {
        String str = hexData;
        String[] tags2 = {"82", "83"};
        ArrayList<String> memoryDataList = new ArrayList<>();
        int i = 0;
        while (i < tags2.length) {
            String tag = tags2[i];
            int tagIndex = str.indexOf(tag);
            if (tagIndex != -1) {
                int dataSizeIndex = tagIndex + 2;
                int dataSize = Integer.parseInt(str.substring(dataSizeIndex, dataSizeIndex + 2), 16);
                int dataStartIndex = dataSizeIndex + 2;
                int dataEndIndex = (dataSize * 2) + dataStartIndex;
                String dataHex = str.substring(dataStartIndex, dataEndIndex);
                long dataValue = Long.parseLong(dataHex, 16);
                double dataKB = dataValue / 1024.0d;
                memoryDataList.add(String.format(Locale.US,"%.2f",dataKB));
            } else {
                memoryDataList.add("Not Found");
            }
            i++;
            str = hexData;
        }
        return memoryDataList;
    }

    private static String parsePkiIds(List<String> pki) {
        if (!pki.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (String pkiId : pki) {
                sb.append(pkiId);
                sb.append("\n");
            }
            return sb.subSequence(0, sb.length() - 1).toString();
        }
        return "N/A";
    }

    private static String parsePkiList(List<String> ForSign, List<String> ForVerify) {
        String sign = parsePkiIds(ForSign);
        String verify = parsePkiIds(ForVerify);

        if (sign.equals(verify)) {
            return "Sign and Verify:\n" + sign;
        } else {
            return "Sign: " + sign + "\nVerify: " + verify;
        }
    }

    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed that action status changed: " + actionStatus);
        dismissProgressDialog();

        switch (actionStatus.getActionStatus()) {
            case GETTING_EUICC_INFO_STARTED:
                // Getting eUICC info is too fast for a progress dialog
                progressDialog = DialogHelper.showProgressDialog(this, R.string.euicc_info_progress_getting_euicc_info);
                break;
            case GETTING_EUICC_INFO_FINISHED:
                setEuiccInfo(viewModel.getEuiccInfo());
                break;
            default:
                // nothing
        }
    };
}
