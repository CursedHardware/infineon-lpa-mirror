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
package com.infineon.esim.lpa.ui.scanBarcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.ActivationCode;
import com.infineon.esim.lpa.ui.downloadProfile.DownloadActivity;
import com.infineon.esim.lpa.util.android.DialogHelper;
import com.infineon.esim.lpa.util.android.PermissionManager;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

final public class ScanBarcodeActivity extends AppCompatActivity {
    private final static String TAG = ScanBarcodeActivity.class.getName();

    private final PermissionManager permissionManager = new PermissionManager(this);

    private PreviewView cameraPreviewView;
    private LinearLayout cameraPreviewLayout;
    private TextView textViewBarCodeValue;
    private ActivationCode activationCode;
    private Button buttonUseThisCode;

    private ScanBarcodeViewModel viewModel;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;

    private boolean permissionRequestOngoing = false;

    // region Lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        this.viewModel = new ViewModelProvider(this).get(ScanBarcodeViewModel.class);

        // Attach the UI
        attachUi();
    }

    @Override
    protected void onResume() {
        Log.debug(TAG,"Resuming activity.");
        super.onResume();
        if(!permissionRequestOngoing) {
            Log.debug(TAG,"No permission request ongoing. Initialize camera.");
            initializeCamera();
        } else {
            Log.debug(TAG,"Permission request ongoing.");
        }
    }

    @Override
    protected void onPause() {
        Log.debug(TAG,"Pausing activity.");
        super.onPause();
        finishBarcodeScanner();
    }

    @Override
    public void onStop() {
        // App crashes on Oppo Reno Z when starting with Android studio if not used...
        super.onStop();
        finish();
    }

    // endregion

    // region Camera handling

    private void initializeCamera() {
        List<PermissionManager.PermissionRequest> permissionRequests = new ArrayList<>();

        // Check camera permission
        permissionRequests.add(new PermissionManager.PermissionRequest(
                Manifest.permission.CAMERA,
                getString(R.string.scan_qr_camera_permission_alert_dialog_title),
                getString(R.string.scan_qr_camera_permission_alert_dialog_message)));

        permissionRequestOngoing = true;

        permissionManager.request(permissionRequests)
                .context(this)
                .checkPermission(allGranted -> {

                    if (allGranted) {
                        permissionRequestOngoing = false;
                        Log.debug(TAG, "Needed CAMERA permission is granted.");
                        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

                        cameraProviderFuture.addListener(() -> {
                            try {
                                cameraProvider = cameraProviderFuture.get();
                                CameraSelector cameraSelector = getCamera();
                                Preview preview = initializePreview();
                                ImageAnalysis imageAnalysis = initializeBarcodeScanner();

                                cameraProvider.unbindAll();
                                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

                            } catch (ExecutionException | InterruptedException e) {
                                // No errors need to be handled for this Future.
                                // This should never be reached.
                            }
                        }, ContextCompat.getMainExecutor(this));
                    } else {
                        // Finish the activity after the error message.
                        DialogHelper.showErrorDialog(this,
                                R.drawable.ic_warning,
                                R.string.error_camera_permission_request_finally_denied_header,
                                R.string.error_camera_permission_request_finally_denied_body,
                                true);
                    }
                });
    }

    private CameraSelector getCamera() {
        return new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
    }

    private void finishBarcodeScanner() {
        if(cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private Preview initializePreview() {
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        return preview;
    }

    private ImageAnalysis initializeBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageProxy -> {
            Log.debug(TAG, "Analyzing the image!");
            @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                // Pass image to an ML Kit Vision API...
                scanner.process(image)
                        .addOnSuccessListener(barcodeList -> {
                            Log.debug(TAG, "Successfully decoded a barcode!");
                            Log.debug(TAG, "Number of barcodeList detected: " + barcodeList.size());

                            barcodeLoop:
                            for (Barcode barcode : barcodeList) {
                                String rawValue = barcode.getRawValue();
                                Log.debug(TAG, "Raw barcode value: " + rawValue);

                                int valueType = barcode.getValueType();
                                switch (valueType) {
                                    case Barcode.TYPE_TEXT:
                                        String text = barcode.getRawValue();
                                        setBarCodeFromText(text);
                                        break barcodeLoop;
                                    case Barcode.TYPE_CALENDAR_EVENT:
                                    case Barcode.TYPE_CONTACT_INFO:
                                    case Barcode.TYPE_DRIVER_LICENSE:
                                    case Barcode.TYPE_EMAIL:
                                    case Barcode.TYPE_GEO:
                                    case Barcode.TYPE_ISBN:
                                    case Barcode.TYPE_PHONE:
                                    case Barcode.TYPE_PRODUCT:
                                    case Barcode.TYPE_SMS:
                                    case Barcode.TYPE_UNKNOWN:
                                    case Barcode.TYPE_URL:
                                    case Barcode.TYPE_WIFI:
                                    default:
                                        Log.error(TAG, "Barcode detected of wrong type: " + valueType);
                                        break;
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.error(TAG, "Failed to decode a barcode!"))
                .addOnCompleteListener(task -> imageProxy.close());
            }
        });

        return imageAnalysis;
    }

    // endregion

    // region UI manipulation

    private void attachUi() {
        Log.debug(TAG, "Attaching UI.");

        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        cameraPreviewLayout = findViewById(R.id.cameraPreviewLayout);
        textViewBarCodeValue = findViewById(R.id.text_barcode_value);
        buttonUseThisCode = findViewById(R.id.button_use_activation_code);
        buttonUseThisCode.setOnClickListener(useThisCodeButtonClickListener);

        String readerName = viewModel.getEuiccName();
        if (readerName != null) {
            setTitle(getString(R.string.scan_qr_title) + " - " + readerName);
        }
    }

    private void setBarCodeFromText(String barcode) {
        Log.debug(TAG,"Set barcode to: " + barcode);
        textViewBarCodeValue.post(() -> {
            activationCode = new ActivationCode(barcode);
            textViewBarCodeValue.setText(activationCode.toString());

            if (activationCode.isValid()) {
                if (buttonUseThisCode != null) {
                    buttonUseThisCode.setEnabled(true);
                    buttonUseThisCode.setVisibility(View.VISIBLE);
                    cameraPreviewLayout.setVisibility(View.GONE);
                    cameraPreviewView.setVisibility(View.GONE);
                }
            }
        });
    }

    // endregion

    // region Listener

    private final View.OnClickListener useThisCodeButtonClickListener = v -> {
        Log.info(TAG, "Activation code: " + activationCode);

        finishBarcodeScanner();

        // Put activation code into the intent
        Intent i = new Intent(ScanBarcodeActivity.this, DownloadActivity.class);
        i.putExtra(Application.INTENT_EXTRA_ACTIVATION_CODE, activationCode);
        startActivity(i);
    };

    // endregion
}