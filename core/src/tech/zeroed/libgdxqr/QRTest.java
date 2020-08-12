package tech.zeroed.libgdxqr;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import jdk.internal.jline.internal.TestAccessible;

public class QRTest implements ApplicationListener, InputProcessor {

    SpriteBatch batch;
    TextureRegion[] qrcodes;
    OrthographicCamera cam;

    @Override
    public void create() {
        String input = "Zeroed";
        batch = new SpriteBatch();

        qrcodes = new TextureRegion[]{

                // Generate a basic QR code
                QRCode.CreateGenerator().blockSize(12).generate(input),
                // Generate a QR code with arcs on the eye borders
                QRCode.CreateGenerator().blockSize(12).setEyeBorderShape(QRGenerator.Shape.ARC).generate(input),
                // Generate a QR code with arcs on the eye borders and circular inner bits
                QRCode.CreateGenerator().blockSize(12).setEyeBorderShape(QRGenerator.Shape.ARC).setEyeInnerShape(QRGenerator.Shape.CIRCLE).generate(input),
                // Generate a QR code with arcs on the eye borders and circular everything else
                QRCode.CreateGenerator().blockSize(12).setEyeBorderShape(QRGenerator.Shape.ARC).setEyeInnerShape(QRGenerator.Shape.CIRCLE).setInnerShape(QRGenerator.Shape.CIRCLE).generate(input),
                // Generate a QR code where everything is a circle
                QRCode.CreateGenerator().blockSize(12).setEyeBorderShape(QRGenerator.Shape.CIRCLE).setEyeInnerShape(QRGenerator.Shape.CIRCLE).setInnerShape(QRGenerator.Shape.CIRCLE).generate(input),
                // Change up the colors
                QRCode.CreateGenerator().blockSize(12).primaryColor(Color.WHITE).secondaryColor(Color.BLACK).generate(input),
                QRCode.CreateGenerator().blockSize(12).primaryColor(Color.GREEN).secondaryColor(Color.BLACK).generate(input),
                // Generate a QR code with a larger border
                QRCode.CreateGenerator().blockSize(12).borderSize(3).generate(input),
                // Generate a larger QR code
                QRCode.CreateGenerator().blockSize(20).generate(input),



        };


        cam = new OrthographicCamera();

        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        cam.update();
        Gdx.gl.glClearColor(0, 0, 0, 0.7f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        qrcode = new QRGenerator(30)
//                .generate("Zeroed");

        batch.begin();
        int x = 0, y = 0;
        for (TextureRegion qrcode : qrcodes) {
            if (x + qrcode.getRegionWidth() >= Gdx.graphics.getWidth()) {
                x = 0;
                y += qrcode.getRegionHeight();
            }
            batch.draw(qrcode, x, y);
            x += qrcode.getRegionWidth();
            if (x >= Gdx.graphics.getWidth()) {
                x = 0;
                y += qrcode.getRegionHeight();
            }
        }
        batch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        for (TextureRegion qrcode : qrcodes)
            qrcode.getTexture().dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = 30f;
        cam.viewportHeight = 30f * height / width;
        cam.update();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        QRCode.scanQRCode(new CodeScanned() {
            @Override
            public void OnCodeScanned(String code) {
                Gdx.app.log("QRTest", "Scanned code: "+code);
            }

            @Override
            public void OnCodeScanError(String error) {
                Gdx.app.error("QRTest", "Error scanning code: "+error);
            }
        });
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}