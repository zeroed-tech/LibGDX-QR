package tech.zeroed.libgdxqr;

import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

public class ScannerOverlay extends ViewGroup {
    private RectF topLeft, topRight, bottomLeft, bottomRight;
    private RectF centralRect;
    private Paint transparent, line;

    public ScannerOverlay(Context context) {
        super(context);

        setBackgroundColor(QRCode.backgroundColor);
        setAlpha(Color.alpha(QRCode.backgroundColor) / 255f);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float left = (w - dpToPx(QRCode.rectWidth)) / 2f;
        float top = (h - dpToPx(QRCode.rectHeight)) / 2f;
        configureRectPoints(left, top);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void configureRectPoints(float left, float top) {
        centralRect = new RectF(left, top, dpToPx(QRCode.rectWidth) + left, dpToPx(QRCode.rectHeight) + top);


        topLeft = new RectF(centralRect.left + QRCode.lineWidth / 2, centralRect.top + QRCode.lineWidth / 2, centralRect.left + QRCode.cornerRadius * 2 - QRCode.lineWidth / 2, centralRect.top + QRCode.cornerRadius * 2 - QRCode.lineWidth / 2);

        topRight = new RectF(centralRect.right - QRCode.cornerRadius * 2 + QRCode.lineWidth / 2, centralRect.top + QRCode.lineWidth / 2, centralRect.right - QRCode.lineWidth / 2, centralRect.top + QRCode.cornerRadius * 2 - QRCode.lineWidth / 2);

        bottomLeft = new RectF(centralRect.left + QRCode.lineWidth / 2, centralRect.bottom - QRCode.cornerRadius * 2 - QRCode.lineWidth / 2, centralRect.left + QRCode.cornerRadius * 2 - QRCode.lineWidth / 2, centralRect.bottom - QRCode.lineWidth / 2);

        bottomRight = new RectF(centralRect.right - QRCode.cornerRadius * 2 + QRCode.lineWidth / 2, centralRect.bottom - QRCode.cornerRadius * 2 - QRCode.lineWidth / 2, centralRect.right - QRCode.lineWidth / 2, centralRect.bottom - QRCode.lineWidth / 2);

        // Setup paints needed
        transparent = new Paint();
        transparent.setAntiAlias(true);
        transparent.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        line = new Paint();
        line.setColor(QRCode.lineColor);
        line.setStrokeWidth(QRCode.lineWidth);
        line.setStyle(Paint.Style.STROKE);
        line.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Carve out a transparent, rounded rect in the center of the screen
        canvas.drawRoundRect(centralRect, QRCode.cornerRadius, QRCode.cornerRadius, transparent);

        // Draw lines on all of the corners
        canvas.drawLine(topLeft.left, topLeft.centerY(), topLeft.left, topLeft.centerY() + QRCode.cornerLength, line);
        canvas.drawLine(topLeft.centerX(), topLeft.top, topLeft.centerX() + QRCode.cornerLength, topLeft.top, line);
        canvas.drawArc(topLeft, 180, 90, false, line);

        canvas.drawLine(topRight.right, topRight.centerY(), topRight.right, topRight.centerY() + QRCode.cornerLength, line);
        canvas.drawLine(topRight.centerX(), topRight.top, topRight.centerX() - QRCode.cornerLength, topRight.top, line);
        canvas.drawArc(topRight, 270, 90, false, line);

        canvas.drawLine(bottomLeft.left, bottomLeft.centerY(), bottomLeft.left, bottomLeft.centerY() - QRCode.cornerLength, line);
        canvas.drawLine(bottomLeft.centerX(), bottomLeft.bottom, bottomLeft.centerX() + QRCode.cornerLength, bottomLeft.bottom, line);
        canvas.drawArc(bottomLeft, 90, 90, false, line);

        canvas.drawLine(bottomRight.right, bottomRight.centerY(), bottomRight.right, bottomRight.centerY() - QRCode.cornerLength, line);
        canvas.drawLine(bottomRight.centerX(), bottomRight.bottom, bottomRight.centerX() - QRCode.cornerLength, bottomRight.bottom, line);
        canvas.drawArc(bottomRight, 0, 90, false, line);

        invalidate();
    }
}
