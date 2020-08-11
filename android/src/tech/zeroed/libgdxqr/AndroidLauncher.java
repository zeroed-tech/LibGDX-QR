package tech.zeroed.libgdxqr;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the QR scanner
		QRCode.init(new AndroidQRCodeNativeInterface(this));

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new QRTest(), config);
	}
}
