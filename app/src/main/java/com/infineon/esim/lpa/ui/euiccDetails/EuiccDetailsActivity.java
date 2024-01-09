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
import java.util.Locale;

public class EuiccDetailsActivity extends AppCompatActivity {
    private static final String TAG = EuiccDetailsActivity.class.getName();

    private EuiccDetailsViewModel viewModel;

    private TextView textViewEid;
    private TextView textViewGsmaVersion;
    private TextView textViewTcaVersion;
    private TextView textViewPkiIds;
    private TextView textViewFirmwareVer;
    private TextView textViewSasAccreditationNumber;
    private TextView textViewForbiddenProfilePolicyRules;
    private TextView textViewExtCardResource;
    private TextView textViewExtCardResource_memory;

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
        textViewGsmaVersion = findViewById(R.id.text_gsma_version);
        textViewTcaVersion = findViewById(R.id.text_tca_version);
        textViewPkiIds = findViewById(R.id.text_pki_ids);
        textViewFirmwareVer = findViewById(R.id.text_firmware_version);
        textViewSasAccreditationNumber = findViewById(R.id.text_sas_accreditation);
        textViewForbiddenProfilePolicyRules = findViewById(R.id.text_forbiddenProfilePolicyRules);
        textViewExtCardResource = findViewById(R.id.text_extCardResource);
        textViewExtCardResource_memory = findViewById(R.id.text_extCardResource_memory);

        textViewEid.setText("");
        textViewGsmaVersion.setText("");
        textViewTcaVersion.setText("");
        textViewPkiIds.setText("");
        textViewFirmwareVer.setText("");
        textViewSasAccreditationNumber.setText("");
        textViewForbiddenProfilePolicyRules.setText("");
        textViewExtCardResource.setText("");
        textViewExtCardResource_memory.setText("");
    }

    private void setEuiccInfo(EuiccInfo euiccInfo) {
        String extCardResource = euiccInfo.getExtCardResource();
        ArrayList<String> memoryData = parseMemoryData(extCardResource);
        textViewEid.setText(euiccInfo.getEid());
        textViewGsmaVersion.setText(euiccInfo.getSvn());
        textViewTcaVersion.setText(euiccInfo.getProfileVersion());
        if (euiccInfo.getPkiIdsForSignAsString() == euiccInfo.getPkiIdsForVerifyAsString()){
            textViewPkiIds.setText("Sign and Verify:\n"+euiccInfo.getPkiIdsForSignAsString());
        } else {
            textViewPkiIds.setText("Sign:\n" + euiccInfo.getPkiIdsForSignAsString()+"\nVerify:\n" + euiccInfo.getPkiIdsForVerifyAsString());
        }
        textViewFirmwareVer.setText(euiccInfo.getEuiccFirmwareVer());
        textViewSasAccreditationNumber.setText(euiccInfo.getSasAcreditationNumber());
        textViewForbiddenProfilePolicyRules.setText(euiccInfo.getForbiddenProfilePolicyRules());
        textViewExtCardResource.setText(extCardResource);
        textViewExtCardResource_memory.setText(getString(R.string.euicc_info_ext_card_resource_memory_example, memoryData.get(0), memoryData.get(1)));
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
