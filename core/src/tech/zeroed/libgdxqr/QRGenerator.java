package tech.zeroed.libgdxqr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class QRGenerator {
    public enum Shape {
        CIRCLE,
        SQUARE,
        ARC
    }

    private int blockSize;
    private int borderSize;

    private Color primary;
    private Color secondary;

    private Shape eyeBorder;
    private Shape eyeInner;
    private Shape inner;

    protected QRGenerator(){
        this.blockSize = 5;
        this.borderSize = 1;
        this.primary = Color.BLACK;
        this.secondary = Color.WHITE;

        eyeBorder = Shape.SQUARE;
        eyeInner = Shape.SQUARE;
        inner = Shape.SQUARE;
    }

    public QRGenerator blockSize(int blockSize){
        this.blockSize = blockSize;
        return this;
    }

    public QRGenerator borderSize(int borderSize){
        this.borderSize = borderSize;
        return this;
    }

    public QRGenerator primaryColor(Color color){
        this.primary = color;
        return this;
    }

    public QRGenerator secondaryColor(Color color){
        this.secondary = color;
        return this;
    }

    public QRGenerator setEyeBorderShape(Shape shape){
        this.eyeBorder = shape;
        return this;
    }

    public QRGenerator setEyeInnerShape(Shape shape){
        this.eyeInner = shape;
        return this;
    }

    public QRGenerator setInnerShape(Shape shape){
        this.inner = shape;
        return this;
    }

    public TextureRegion generate(String text) {
        BitMatrix bitMatrix;

        PolygonSpriteBatch batch = new PolygonSpriteBatch();
        Texture texture = null;
        FrameBuffer fbo = null;
        OrthographicCamera camera = new OrthographicCamera();

        try {
            // Build a QR encoder and encode the requested text into a bitmatrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 0, 0);

            // Extract the co-ords of the QR code within the bitmatrix
            int[] enclosingRect = bitMatrix.getEnclosingRectangle();
            int left = enclosingRect[0];
            int top = enclosingRect[1];
            int width = enclosingRect[2];
            int height = enclosingRect[3];

            // Build a frame buffer that's the right size to render our QR code
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, blockSize * (width + borderSize*2), blockSize * (height + borderSize*2), false);

            // Setup the camera
            camera.setToOrtho(false, blockSize * (width + borderSize*2), blockSize * (height + borderSize*2));
            camera.update();

            // Start rendering to the frame buffer
            fbo.begin();

            // Clear the frame buffer with the secondary color to form the background
            Gdx.gl.glClearColor(secondary.r, secondary.g, secondary.b, secondary.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            // Create single white pixel. This can be coloured and used to draw the QR code
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            texture = new Texture(pixmap);
            pixmap.dispose();
            TextureRegion region = new TextureRegion(texture, 0, 0, 1, 1);

            // Setup the shape drawer
            ShapeDrawer drawer = new ShapeDrawer(batch, region);
            drawer.setColor(primary);
            batch.setProjectionMatrix(camera.combined);
            drawer.update();
            batch.begin();

            // Draw the inner units
            for(int x = left; x < left+width; x++){
                for(int y = top; y < top+height; y++){
                    // Check to see if the current block is part of one of the eyes and skip if it is (we draw those later)
                    if((x <= left+7 && y <= top+7) || (x >= (left + width - 7) && y <= top+7) || (x <= left+7 && y >= top + height - 7)) {
                        continue;
                    }
                    if(bitMatrix.get(x,y)) {
                        switch (inner){
                            case CIRCLE:
                                drawCircle(drawer, (x - left), (y - top), blockSize * 0.4f);
                                break;
                            case SQUARE:
                            case ARC:
                                // Arc isn't supported for inner
                                drawSquare(drawer, (x - left), (y - top), 1, 1);
                        }

                    }
                }
            }

            // Draw the eyes
            drawEye(drawer, 0,0);
            drawEye(drawer, width - 7,0);
            drawEye(drawer, 0,height - 7);
            batch.end();

            // Extract the frame buffer's contents so it doesn't need to be retained
            Pixmap extractedTexture = ScreenUtils.getFrameBufferPixmap(0, 0, fbo.getWidth(), fbo.getHeight());

            // Finish rendering to the frame buffer
            fbo.end();

            Texture result = new Texture(extractedTexture);
            // Free the pixmap
            extractedTexture.dispose();

            // Return the frame buffer
            return new TextureRegion(result);
        }catch (Exception exception){
            Gdx.app.error("QRGenerator", exception.getMessage(), exception);
        }finally {
            // Free the painting pixel
            if(texture != null)
                texture.dispose();
            // Free the frame buffer
            if(fbo != null)
                fbo.dispose();
            // Free the batch
            batch.dispose();
        }
        return null;
    }

    private void drawCircle(ShapeDrawer drawer, int x, int y, float radius){
        drawer.filledCircle(blockSize/2.0f + x * blockSize + borderSize * blockSize, blockSize/2.0f + y * blockSize + borderSize * blockSize, radius);
    }

    private void drawSquare(ShapeDrawer drawer, int x, int y, int width, int height){
        drawer.filledRectangle(x*blockSize + borderSize * blockSize, y*blockSize + borderSize * blockSize, width*blockSize, height*blockSize);
    }

    private void drawArc(ShapeDrawer drawer, int x, int y, float start){
        drawer.sector((x+1)*blockSize + borderSize * blockSize, (y+1)*blockSize + borderSize * blockSize, blockSize, MathUtils.degreesToRadians*start, MathUtils.degreesToRadians*90);
    }

    private void drawEye(ShapeDrawer drawer, int x, int y){
        switch (eyeBorder){
            case CIRCLE:
                for(int _x = x; _x < x + 7; _x++) {
                    for (int _y = y; _y < y + 7; _y++) {
                        if((_y == y || _y == y + 6) || (_x == x || _x == x + 6))
                            drawCircle(drawer, _x, _y, blockSize*0.5f);
                    }
                }

                break;
            case SQUARE:
                drawSquare(drawer, x, y, 7, 1);
                drawSquare(drawer, x, y+6, 7, 1);
                drawSquare(drawer, x, y, 1, 7);
                drawSquare(drawer, x+6, y, 1, 7);
                break;
            case ARC:
                drawSquare(drawer, x+1, y, 5, 1);
                drawSquare(drawer, x+1, y+6, 5, 1);
                drawSquare(drawer, x, y+1, 1, 5);
                drawSquare(drawer, x+6, y+1, 1, 5);

                drawArc(drawer, x, y, 180);
                drawArc(drawer, x+5, y, 270);
                drawArc(drawer, x, y+5, 90);
                drawArc(drawer, x+5, y+5, 0);

                break;
        }

        // Draw the eyes inner bits
        for(int _x = x + 2; _x < x + 5; _x++){
            for(int _y = y + 2; _y < y + 5; _y++){
                switch (eyeInner){
                    case CIRCLE:
                        drawCircle(drawer, _x, _y, blockSize*0.5f);
                        break;
                    case SQUARE:
                    case ARC:
                        drawSquare(drawer,_x, _y, 1, 1);
                        break;
                }
            }
        }
    }
}
