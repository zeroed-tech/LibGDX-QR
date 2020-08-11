package tech.zeroed.libgdxqr.desktop;

import com.badlogic.gdx.Gdx;
import tech.zeroed.libgdxqr.NativeInterface;

public class DesktopQRCodeNativeInterface implements NativeInterface {
    @Override
    public void scanQRCode() {
        Gdx.app.error("LibGDX-QR", "Desktop doesn't support scanning QR codes (do you really want to hold a QR Code up to your webcam?)");
    }
}
