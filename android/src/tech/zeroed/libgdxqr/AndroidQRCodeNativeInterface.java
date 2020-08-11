package tech.zeroed.libgdxqr;

import android.app.Activity;
import android.content.Intent;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidEventListener;

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
            String result = data == null ? "" : data.getDataString();
            QRCode.QRCodeScanned(resultCode == Activity.RESULT_OK, result);
        }
    }
}
