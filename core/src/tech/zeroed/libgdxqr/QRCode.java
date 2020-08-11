package tech.zeroed.libgdxqr;

public class QRCode {
    private static QRCode instance;

    // Contains references to system dependent implementations of functions
    private NativeInterface nativeInterface;
    private CodeScanned callback;

    private QRCode(NativeInterface nativeInterface) {
        this.nativeInterface = nativeInterface;
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
    }
}
