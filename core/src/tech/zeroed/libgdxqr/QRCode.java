package tech.zeroed.libgdxqr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

public class QRCode {
    private static QRCode instance;

    // Contains references to system dependent implementations of functions
    private NativeInterface nativeInterface;
    private CodeScanned callback;
    private boolean scanInProgress = false;

    // Change this to true to force the scanner to return a byte array rather than a string
    public static boolean forceBinary = false;

    // Static properties used to configure native scanners
    // (this saves having to pass all config values through multiple constructors to get them to the native code)

    // Width and height of the overlay window
    public static int rectWidth, rectHeight;
    // Color of the corners around the overlay window
    public static int lineColor;
    // Width of the line making up the corners around the overlay
    public static float lineWidth;
    // Length of the line making up each side of the corners around the overlay
    public static float cornerLength;
    // Radius of the overlay's corners and the line drawn over the top
    public static float cornerRadius;
    // Color of the overlay background around the window
    public static int backgroundColor;

    private QRCode(NativeInterface nativeInterface) {
        this.nativeInterface = nativeInterface;
        rectWidth = 300;
        rectHeight = 300;
        lineColor = Color.argb8888(1, 1, 1, 1);
        backgroundColor = Color.argb8888(0.75f, 0, 0, 0);
        lineWidth = 5;
        cornerLength = 40;
        cornerRadius = 30;
    }

    public static void init(){
    }

    public static void init(NativeInterface nativeInterface){
        QRCode.instance = new QRCode(nativeInterface);
    }

    public static QRGenerator CreateGenerator(){
        return new QRGenerator();
    }

    public static void scanQRCode(CodeScanned callback){
        if(instance == null)
            throw new IllegalStateException("Please call init() with a native implementation before trying to use native features");
        if(instance.scanInProgress)
            return;
        instance.scanInProgress = true;
        instance.callback = callback;
        instance.nativeInterface.scanQRCode();
    }

    public static void QRCodeScanned(boolean successful, String value){
        if(instance == null)
            throw new IllegalStateException("Please call init() with a native implementation before trying to use native features");
        if(instance.callback == null)
            return;

        // Call the appropriate callback
        if(successful)
            instance.callback.OnCodeScanned(value);
        else
            instance.callback.OnCodeScanError(value);
        instance.scanInProgress = false;
    }

    public static void QRCodeScanned(byte[] value){
        if(instance == null)
            throw new IllegalStateException("Please call init() with a native implementation before trying to use native features");
        if(instance.callback == null)
            return;

        // Call the appropriate callback
        instance.callback.OnCodeScanned(value);

        instance.scanInProgress = false;
    }
}
