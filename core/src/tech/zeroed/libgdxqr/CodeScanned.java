package tech.zeroed.libgdxqr;

public interface CodeScanned {
    void OnCodeScanned(String code);
    void OnCodeScanError(String error);
}
