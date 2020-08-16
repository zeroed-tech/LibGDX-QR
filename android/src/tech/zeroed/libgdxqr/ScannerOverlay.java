package tech.zeroed.libgdxqr;

import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import static tech.zeroed.libgdxqr.QRCode.*;

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


        topLeft = new RectF(centralRect.left + lineWidth / 2, centralRect.top + lineWidth / 2, centralRect.left + cornerRadius * 2 - lineWidth / 2, centralRect.top + cornerRadius * 2 - lineWidth / 2);

        topRight = new RectF(centralRect.right - cornerRadius * 2 + lineWidth / 2, centralRect.top + lineWidth / 2, centralRect.right - lineWidth / 2, centralRect.top + cornerRadius * 2 - lineWidth / 2);

        bottomLeft = new RectF(centralRect.left + lineWidth / 2, centralRect.bottom - cornerRadius * 2 - lineWidth / 2, centralRect.left + cornerRadius * 2 - lineWidth / 2, centralRect.bottom - lineWidth / 2);

        bottomRight = new RectF(centralRect.right - cornerRadius * 2 + lineWidth / 2, centralRect.bottom - cornerRadius * 2 - lineWidth / 2, centralRect.right - lineWidth / 2, centralRect.bottom - lineWidth / 2);

        // Setup paints needed
        transparent = new Paint();
        transparent.setAntiAlias(true);
        transparent.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth(lineWidth);
        line.setStyle(Paint.Style.STROKE);
        line.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Carve out a transparent, rounded rect in the center of the screen
        canvas.drawRoundRect(centralRect, cornerRadius, cornerRadius, transparent);

        // Draw lines on all of the corners
        canvas.drawLine(topLeft.left, topLeft.centerY(), topLeft.left, topLeft.centerY() + cornerLength, line);
        canvas.drawLine(topLeft.centerX(), topLeft.top, topLeft.centerX() + cornerLength, topLeft.top, line);
        canvas.drawArc(topLeft, 180, 90, false, line);

        canvas.drawLine(topRight.right, topRight.centerY(), topRight.right, topRight.centerY() + cornerLength, line);
        canvas.drawLine(topRight.centerX(), topRight.top, topRight.centerX() - cornerLength, topRight.top, line);
        canvas.drawArc(topRight, 270, 90, false, line);

        canvas.drawLine(bottomLeft.left, bottomLeft.centerY(), bottomLeft.left, bottomLeft.centerY() - cornerLength, line);
        canvas.drawLine(bottomLeft.centerX(), bottomLeft.bottom, bottomLeft.centerX() + cornerLength, bottomLeft.bottom, line);
        canvas.drawArc(bottomLeft, 90, 90, false, line);

        canvas.drawLine(bottomRight.right, bottomRight.centerY(), bottomRight.right, bottomRight.centerY() - cornerLength, line);
        canvas.drawLine(bottomRight.centerX(), bottomRight.bottom, bottomRight.centerX() - cornerLength, bottomRight.bottom, line);
        canvas.drawArc(bottomRight, 0, 90, false, line);

        invalidate();
    }
}
