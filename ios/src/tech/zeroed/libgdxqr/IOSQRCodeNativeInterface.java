package tech.zeroed.libgdxqr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
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

public class IOSQRCodeNativeInterface implements NativeInterface{
    @Override
    public void scanQRCode() {
        UIViewController controller = ((IOSApplication)Gdx.app).getUIViewController();
        ScannerViewController scanner = new ScannerViewController();
        controller.addChildViewController(scanner);
        controller.getView().addSubview(scanner.getView());
        scanner.getView().setBounds(controller.getView().getBounds());
    }

    protected static class ScannerViewController extends UIViewController implements AVCaptureMetadataOutputObjectsDelegate {
        private AVCaptureSession captureSession;
        private ScannerOverlayPreviewLayer previewLayer;

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

            previewLayer = new ScannerOverlayPreviewLayer(captureSession);
            previewLayer.setFrame(getView().getLayer().getBounds());
            previewLayer.setVideoGravity(AVLayerVideoGravity.ResizeAspectFill);

            getView().getLayer().addSublayer(previewLayer);

            captureSession.startRunning();
        }

        private void failed(){
            QRCode.QRCodeScanned(false, "An error was encountered while setting up the scanner");
            willMoveToParentViewController(null);
            getView().removeFromSuperview();
            removeFromParentViewController();
        }

        private void found(String code){
            QRCode.QRCodeScanned(true, code);
            willMoveToParentViewController(null);
            getView().removeFromSuperview();
            removeFromParentViewController();
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

    private static class ScannerOverlayPreviewLayer extends AVCaptureVideoPreviewLayer {
        private double cornerLength = 30;
        private double lineWidth = 6;

        private UIColor lineColor = UIColor.white();
        private CALineCap lineCap = CALineCap.Round;
        private CGSize maskSize = new CGSize(200, 200);

        public ScannerOverlayPreviewLayer(AVCaptureSession captureSession) {
            super(captureSession);
        }

        private CGRect getMaskContainer(){
            CGRect bounds = getBounds();
            return new CGRect((bounds.getWidth() / 2) - (maskSize.getWidth() / 2),
                    (bounds.getHeight() / 2) - (maskSize.getHeight() / 2),
                    maskSize.getWidth(), maskSize.getHeight());
        }

        private CGPoint offsetBy(CGPoint point, double x, double y){
            point.setX(point.getX()+x);
            point.setY(point.getY()+y);
            return point;
        }

        @Override
        public void draw(CGContext ctx) {
            super.draw(ctx);

            CGRect maskContainer = getMaskContainer();

            CGMutablePath path = CGMutablePath.createMutable();
            path.addRect(null, getBounds());
            path.addRoundedRect(null, maskContainer, getCornerRadius(), getCornerRadius());

            CAShapeLayer maskLayer = new CAShapeLayer();
            maskLayer.setPath(path);
            maskLayer.setFillColor(getBackgroundColor());
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

            CAShapeLayer shapeLayer = new CAShapeLayer();
            shapeLayer.setPath(combinedPath);
            shapeLayer.setStrokeColor(lineColor.getCGColor());
            shapeLayer.setFillColor(UIColor.clear().getCGColor());
            shapeLayer.setLineWidth(lineWidth);
            shapeLayer.setLineCap(lineCap);

            addSublayer(shapeLayer);
        }
    }
}
