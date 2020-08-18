package tech.zeroed.libgdxqr;

public interface CodeScanned {
    void OnCodeScanned(String code);
    void OnCodeScanned(byte[] code);
    void OnCodeScanError(String error);
}
