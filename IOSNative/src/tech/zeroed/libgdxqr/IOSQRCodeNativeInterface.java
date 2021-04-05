package tech.zeroed.libgdxqr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import org.robovm.apple.avfoundation.*;
import org.robovm.apple.coreanimation.CALineCap;
import org.robovm.apple.coreanimation.CAShapeFillRule;
import org.robovm.apple.coreanimation.CAShapeLayer;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.uikit.*;

import java.util.ArrayList;

public class IOSQRCodeNativeInterface implements NativeInterface {

    @Override
    public void scanQRCode() {
        UIViewController controller = ((IOSApplication) Gdx.app).getUIViewController();
        ScannerViewController scanner = new ScannerViewController();
        controller.addChildViewController(scanner);
        controller.getView().addSubview(scanner.getView());
        scanner.getView().setBounds(controller.getView().getBounds());
    }

    protected static class ScannerViewController extends UIViewController implements AVCaptureMetadataOutputObjectsDelegate {
        private AVCaptureSession captureSession;
        private ScannerOverlayPreviewLayer previewLayer;
        private UINavigationBar navBar;
        private boolean setupValid = false;

        @Override
        public void viewDidLoad() {
            super.viewDidLoad();
            setupValid = true;
            AVAuthorizationStatus permissionStatus = AVCaptureDevice.getAuthorizationStatusForMediaType(AVMediaType.Video);
            switch (permissionStatus){
                case NotDetermined:
                    break;
                case Restricted:
                    setupValid = false;
                    failed("Access to this devices camera has been restricted.\nQR scanning disabled");
                    return;
                case Denied:
                    setupValid = false;
                    failed("Camera permission denied, please enable the camera permission in the Settings application.\nQR scanning disabled");
                    return;
                case Authorized:
                    break;
            }

            getView().setBackgroundColor(UIColor.black());
            captureSession = new AVCaptureSession();
            AVCaptureDevice videoCaptureDevice = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
            AVCaptureDeviceInput videoInput;
            try{
                videoInput = new AVCaptureDeviceInput(videoCaptureDevice);
            } catch (NSErrorException e) {
                setupValid = false;
                e.printStackTrace();
                failed(e.getMessage());
                return;
            }

            if(captureSession.canAddInput(videoInput)){
                captureSession.addInput(videoInput);
            }else{
                setupValid = false;
                failed("An error was encountered while setting up the scanner");
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
                setupValid = false;
                failed("An error was encountered while setting up the scanner");
                return;
            }

            previewLayer = new ScannerOverlayPreviewLayer(captureSession);
            previewLayer.setFrame(getView().getLayer().getBounds());
            previewLayer.setVideoGravity(AVLayerVideoGravity.ResizeAspectFill);

            getView().getLayer().addSublayer(previewLayer);

            navBar = new UINavigationBar(new CGRect(0, 0, UIScreen.getMainScreen().getBounds().getWidth(), 44));
            navBar.setAutoresizingMask(UIViewAutoresizing.FlexibleWidth);
            UINavigationItem navItem = new UINavigationItem("Scan a QR code");
            UIBarButtonItem doneItem = new UIBarButtonItem(UIBarButtonSystemItem.Cancel, new UIBarButtonItem.OnClickListener() {
                @Override
                public void onClick(UIBarButtonItem uiBarButtonItem) {
                    failed("User cancelled scan");
                }
            });
            navItem.setLeftBarButtonItem(doneItem);
            navBar.setItems(new NSArray<>(navItem), false);
            getView().addSubview(navBar);

            captureSession.startRunning();
        }

        private void failed(String reason){
            QRCode.QRCodeScanned(false, reason);
            removeView();
        }

        private void found(String code){
            QRCode.QRCodeScanned(true, code);
            removeView();
        }

        private void found(byte[] code){
            QRCode.QRCodeScanned(code);
            removeView();
        }

        private void removeView(){
            willMoveToParentViewController(null);
            getView().removeFromSuperview();
            removeFromParentViewController();
        }

        @Override
        public void viewWillAppear(boolean animated) {
            super.viewWillAppear(animated);
            if(!setupValid){
                removeView();
                return;
            }
            if(captureSession != null && !captureSession.isRunning()){
                captureSession.startRunning();
            }
        }

        private void updatePreviewLayer(AVCaptureConnection layer, AVCaptureVideoOrientation orientation) {
            layer.setVideoOrientation(orientation);
            previewLayer.setFrame(getView().getBounds());
        }

        @Override
        public void viewDidLayoutSubviews() {
            super.viewDidLayoutSubviews();
            if(!setupValid){
                removeView();
                return;
            }

            setRotation();
        }

        private void setRotation(){
            UIDevice currentDevice = UIDevice.getCurrentDevice();
            UIDeviceOrientation orientation = currentDevice.getOrientation();
            AVCaptureConnection connection = previewLayer.getConnection();

            if(connection.supportsVideoOrientation()){
                switch (orientation) {
                    case Portrait: {
                        updatePreviewLayer(connection, AVCaptureVideoOrientation.Portrait);
                        break;
                    }
                    case PortraitUpsideDown: {
                        updatePreviewLayer(connection, AVCaptureVideoOrientation.PortraitUpsideDown);
                        break;
                    }
                    case LandscapeLeft: {
                        updatePreviewLayer(connection, AVCaptureVideoOrientation.LandscapeRight);
                        break;
                    }
                    case LandscapeRight: {
                        updatePreviewLayer(connection, AVCaptureVideoOrientation.LandscapeLeft);
                        break;
                    }

                    default: updatePreviewLayer(connection, AVCaptureVideoOrientation.Portrait);

                }
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
        public void viewWillTransitionToSize(CGSize cgSize, UIViewControllerTransitionCoordinator uiViewControllerTransitionCoordinator) {
            super.viewWillTransitionToSize(cgSize, uiViewControllerTransitionCoordinator);

            previewLayer.setFrame(new CGRect(new CGPoint(0,0), cgSize));
            previewLayer.setVideoGravity(AVLayerVideoGravity.ResizeAspectFill);
            setRotation();
        }

        @Override
        public boolean prefersStatusBarHidden() {
            return true;
        }

        @Override
        public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
            return UIInterfaceOrientationMask.All;
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

    /**
     * Ported from https://github.com/gaebel/scanner-overlays
     */
    private static class ScannerOverlayPreviewLayer extends AVCaptureVideoPreviewLayer {
        private double cornerLength = QRCode.cornerLength;
        private double lineWidth = QRCode.lineWidth;
        private UIColor lineColor = colorFromInt(QRCode.lineColor);
        private UIColor backgroundColor = colorFromInt(QRCode.backgroundColor);
        private CALineCap lineCap = CALineCap.Round;
        private CGSize maskSize = new CGSize(QRCode.rectWidth, QRCode.rectHeight);
        private CAShapeLayer maskLayer;
        private CAShapeLayer shapeLayer;

        public ScannerOverlayPreviewLayer(AVCaptureSession captureSession) {
            super(captureSession);
            setCornerRadius(10);
            setNeedsDisplay();
            setNeedsDisplayOnBoundsChange(true);
        }

        private UIColor colorFromInt(int color){
            double a = ((color & 0xff000000) >>> 24) / 255f;
            double r = ((color & 0x00ff0000) >>> 16) / 255f;
            double g = ((color & 0x0000ff00) >>> 8) / 255f;
            double b = ((color & 0x000000ff)) / 255f;
            return new UIColor(r, g, b, a);
        }

        private CGRect getMaskContainer(){
            CGRect bounds = getBounds();
            return new CGRect((bounds.getWidth() / 2) - (maskSize.getWidth() / 2),
                    (bounds.getHeight() / 2) - (maskSize.getHeight() / 2),
                    maskSize.getWidth(), maskSize.getHeight());
        }

        private CGPoint offsetBy(CGPoint point, double x, double y){
            return new CGPoint(point.getX()+x, point.getY()+y);
        }

        @Override
        public void draw(CGContext ctx) {
            super.draw(ctx);

            if(maskLayer != null){
                maskLayer.removeFromSuperlayer();
            }

            if(shapeLayer != null){
                shapeLayer.removeFromSuperlayer();
            }

            CGRect maskContainer = getMaskContainer();

            CGMutablePath path = CGMutablePath.createMutable();
            path.addRect(null, getBounds());
            path.addRoundedRect(null, maskContainer, getCornerRadius(), getCornerRadius());

            maskLayer = new CAShapeLayer();
            maskLayer.setPath(path);
            maskLayer.setFillColor(backgroundColor.getCGColor());
            maskLayer.setFillRule(CAShapeFillRule.EvenOdd);

            addSublayer(maskLayer);

            if(getCornerRadius() > cornerLength){
                setCornerRadius(cornerLength);
            }
            if(cornerLength > maskContainer.getWidth() / 2){
                cornerLength = (float) (maskContainer.getWidth() / 2f);
            }

            CGPoint upperLeftPoint = new CGPoint(maskContainer.getMinX(), maskContainer.getMinY());
            CGPoint upperRightPoint = new CGPoint(maskContainer.getMaxX(), maskContainer.getMinY());
            CGPoint lowerRightPoint = new CGPoint(maskContainer.getMaxX(), maskContainer.getMaxY());
            CGPoint lowerLeftPoint = new CGPoint(maskContainer.getMinX(), maskContainer.getMaxY());

            UIBezierPath upperLeftCorner = new UIBezierPath();
            upperLeftCorner.move(offsetBy(upperLeftPoint, 0, cornerLength));
            upperLeftCorner.addArc(offsetBy(upperLeftPoint, getCornerRadius(), getCornerRadius()), getCornerRadius(), Math.PI, 3 * Math.PI / 2, true);
            upperLeftCorner.addLine(offsetBy(upperLeftPoint, cornerLength, 0));

            UIBezierPath upperRightCorner = new UIBezierPath();
            upperRightCorner.move(offsetBy(upperRightPoint, -cornerLength, 0));
            upperRightCorner.addArc(offsetBy(upperRightPoint, -getCornerRadius(), getCornerRadius()), getCornerRadius(), 3 * Math.PI / 2, 0, true);
            upperRightCorner.addLine(offsetBy(upperRightPoint, 0, cornerLength));

            UIBezierPath lowerRightCorner = new UIBezierPath();
            lowerRightCorner.move(offsetBy(lowerRightPoint, 0, -cornerLength));
            lowerRightCorner.addArc(offsetBy(lowerRightPoint, -getCornerRadius(), -getCornerRadius()), getCornerRadius(), 0, Math.PI / 2, true);
            lowerRightCorner.addLine(offsetBy(lowerRightPoint, -cornerLength, 0));

            UIBezierPath bottomLeftCorner = new UIBezierPath();
            bottomLeftCorner.move(offsetBy(lowerLeftPoint, cornerLength, 0));
            bottomLeftCorner.addArc(offsetBy(lowerLeftPoint, getCornerRadius(), -getCornerRadius()), getCornerRadius(), Math.PI / 2, Math.PI, true);
            bottomLeftCorner.addLine(offsetBy(lowerLeftPoint, 0, -cornerLength));

            CGMutablePath combinedPath = CGMutablePath.createMutable();
            combinedPath.addPath(null, upperLeftCorner.getCGPath());
            combinedPath.addPath(null, upperRightCorner.getCGPath());
            combinedPath.addPath(null, lowerRightCorner.getCGPath());
            combinedPath.addPath(null, bottomLeftCorner.getCGPath());

            shapeLayer = new CAShapeLayer();
            shapeLayer.setPath(combinedPath);
            shapeLayer.setStrokeColor(lineColor.getCGColor());
            shapeLayer.setFillColor(UIColor.clear().getCGColor());
            shapeLayer.setLineWidth(lineWidth);
            shapeLayer.setLineCap(lineCap);

            addSublayer(shapeLayer);
        }
    }
}