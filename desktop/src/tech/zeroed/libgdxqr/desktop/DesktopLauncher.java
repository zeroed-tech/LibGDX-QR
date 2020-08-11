package tech.zeroed.libgdxqr.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import tech.zeroed.libgdxqr.QRCode;
import tech.zeroed.libgdxqr.QRTest;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.resizable  = false;
		config.samples = 4;
		QRCode.init(new DesktopQRCodeNativeInterface());
		new LwjglApplication(new QRTest(), config);
	}
}
