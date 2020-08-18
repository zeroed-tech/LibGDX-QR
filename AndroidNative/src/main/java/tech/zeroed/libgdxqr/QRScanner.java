package tech.zeroed.libgdxqr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRScanner extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, Detector.Processor<Barcode> {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private CameraSourcePreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Scan a QR code");

        RelativeLayout root = new RelativeLayout(this);

        preview = new CameraSourcePreview(this, null);

        root.addView(preview, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(new ScannerOverlay(this), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent result = new Intent();
        result.setData(Uri.parse("User cancelled scan"));
        setResult(Activity.RESULT_CANCELED, result);
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // The user has granted us permission to use the camera, start the camera source
                try {
                    preview.start(cameraSource);
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    Intent result = new Intent();
                    result.setData(Uri.parse(e.getMessage()));
                    setResult(Activity.RESULT_CANCELED, result);
                    finish();
                }
            }else{
                Intent result = new Intent();
                result.setData(Uri.parse("Permission denied"));
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            }
        }
    }

    @Override
    public void release() {}

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() != 0) {
            Intent result = new Intent();

            Barcode barcode = barcodes.valueAt(0);
            result.putExtra("Barcode", barcode);

            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
        preview.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        barcodeDetector.setProcessor(this);

        if (ActivityCompat.checkSelfPermission(QRScanner.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                preview.start(cameraSource);
            } catch (IOException e) {
                e.printStackTrace();
                Intent result = new Intent();
                result.setData(Uri.parse(e.getMessage()));
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            }
        } else {
            ActivityCompat.requestPermissions(QRScanner.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }
}

