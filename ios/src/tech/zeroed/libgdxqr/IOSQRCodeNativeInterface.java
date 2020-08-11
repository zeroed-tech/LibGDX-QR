package tech.zeroed.libgdxqr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.utils.Array;
import org.robovm.apple.avfoundation.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.uikit.*;

import java.util.ArrayList;

public class IOSQRCodeNativeInterface implements NativeInterface{
    @Override
    public void scanQRCode() {
        UIViewController controller = ((IOSApplication)Gdx.app).getUIViewController();
        ScannerViewController scanner = new ScannerViewController();
        controller.addChildViewController(scanner);

    }

    protected static class ScannerViewController extends UIViewController implements AVCaptureMetadataOutputObjectsDelegate {
        private AVCaptureSession captureSession;
        private AVCaptureVideoPreviewLayer previewLayer;

        @Override
        public void viewDidLoad() {
            super.viewDidLoad();

            getView().setBackgroundColor(UIColor.black());
            captureSession = new AVCaptureSession();
            AVCaptureDevice videoCaptureDevice = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
            AVCaptureDeviceInput videoInput;
            try{
                videoInput = new AVCaptureDeviceInput(videoCaptureDevice);
            } catch (NSErrorException e) {
                e.printStackTrace();
                failed();
                return;
            }

            if(captureSession.canAddInput(videoInput)){
                captureSession.addInput(videoInput);
            }else{
                failed();
                return;
            }
            AVCaptureMetadataOutput metadataOutput = new AVCaptureMetadataOutput();

            if(captureSession.canAddOutput(metadataOutput)){
                captureSession.addOutput(metadataOutput);

                metadataOutput.setMetadataObjectsDelegate(this, DispatchQueue.getMainQueue());
                ArrayList<AVMetadataObjectType> objectTypes = new ArrayList<>();
                objectTypes.add(AVMetadataObjectType.QRCode);
                metadataOutput.setMetadataObjectTypes(objectTypes);
            }else{
                failed();
                return;
            }

            previewLayer = new AVCaptureVideoPreviewLayer(captureSession);
            previewLayer.setFrame(getView().getLayer().getBounds());
            previewLayer.setVideoGravity(AVLayerVideoGravity.ResizeAspectFill);

            getView().getLayer().addSublayer(previewLayer);

            captureSession.startRunning();
        }

        private void failed(){
            QRCode.QRCodeScanned(false, "An error was encountered while setting up the scanner");
        }

        private void found(String code){
            QRCode.QRCodeScanned(true, code);
        }

        @Override
        public void viewWillAppear(boolean animated) {
            super.viewWillAppear(animated);
            if(captureSession != null && !captureSession.isRunning()){
                captureSession.startRunning();
            }
        }

        @Override
        public void viewWillDisappear(boolean animated) {
            super.viewWillDisappear(animated);
            if(captureSession != null && captureSession.isRunning()){
                captureSession.stopRunning();
            }
        }

        @Override
        public boolean prefersStatusBarHidden() {
            return true;
        }

        @Override
        public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
            return UIInterfaceOrientationMask.Portrait;
        }

        @Override
        public void didOutputMetadataObjects(AVCaptureOutput output, NSArray<AVMetadataObject> metadataObjects, AVCaptureConnection connection) {
            captureSession.stopRunning();
            AVMetadataObject metadataObject = metadataObjects.first();
            if(metadataObject != null){
                if(metadataObject instanceof AVMetadataMachineReadableCodeObject) {
                    AVMetadataMachineReadableCodeObject readableCodeObject = (AVMetadataMachineReadableCodeObject)metadataObject;
                    String scannedValue = readableCodeObject.getStringValue();
                    found(scannedValue);
                }
            }
        }
    }
}
