package tech.zeroed.libgdxqr;

import android.app.Activity;
import android.content.Intent;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.google.android.gms.vision.barcode.Barcode;

public class AndroidQRCodeNativeInterface implements NativeInterface, AndroidEventListener {

    private final AndroidApplication activity;
    private static final int SCAN_QR_CODE = 201;

    public AndroidQRCodeNativeInterface(AndroidApplication activity) {
        this.activity = activity;
        this.activity.addAndroidEventListener(this);
    }

    @Override
    public void scanQRCode() {
        activity.startActivityForResult(new Intent(this.activity, QRScanner.class), SCAN_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SCAN_QR_CODE){
            Barcode barcode = data.getParcelableExtra("Barcode");

            if(resultCode != Activity.RESULT_OK && barcode == null) {
                QRCode.QRCodeScanned(false, data.getDataString());
                return;
            }

            if(resultCode == Activity.RESULT_OK && (QRCode.forceBinary || barcode.rawValue.equals("Unknown encoding")))
                QRCode.QRCodeScanned(barcode.rawBytes);
            else
                QRCode.QRCodeScanned(resultCode == Activity.RESULT_OK, barcode.rawValue);
        }
    }
}
