package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Singleton;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.EAN13Reader;
import helpers.BufferedImageLuminanceSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This {@link Controller} is handling barcode scanning with a webcam
 * User: tuxburner
 * Date: 2/11/13
 * Time: 8:19 PM
 */
@Security.Authenticated(Secured.class)
@Singleton
public class BarcodeController extends Controller {

  /**
   * This initializes the barcodeScanner view
   * @return
   */
  @JSRoute
  public Result displayBarcodeScaner() {
    return ok(views.html.barcode.barcodescanner.render());
  }

  @JSRoute
  public LegacyWebSocket<String> scanBarcode() {

    // For each event received on the socket,
    String code = null;

    return WebSocket.whenReady((in,out) -> {

      // For each event received on the socket,
      in.onMessage((event) -> {
        if(StringUtils.startsWith(event,"data:image/jpeg;base64,")) {
          Logger.debug("Got an image. Trying to parse it.");

          try {
            byte decoded[] = Base64.decodeBase64(StringUtils.removeStart(event, "data:image/jpeg;base64,"));
            InputStream inputStream = new ByteArrayInputStream(decoded);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            EAN13Reader reader = new EAN13Reader();
            com.google.zxing.Result decode = reader.decode(bitmap);
            String decodeText = decode.getText();
            if(Logger.isDebugEnabled()) {
              Logger.debug("Got EAN code: "+decodeText+" from image.");
            }

            out.write(decodeText);

          } catch (IOException e) {
            if(Logger.isErrorEnabled() == true) {
              Logger.error("An error happend while reading the image.",e);
            }
            out.write("error");
          } catch (NotFoundException e) {
            if(Logger.isErrorEnabled() == true) {
              Logger.error("Could not extract barcode from image.", e);
              out.write("error");
            }
          } catch (FormatException e) {
            Logger.error("An error happend while reading the image.", e);
            out.write("error");
          }
        }
      });

      // When the socket is closed.
      in.onClose(() -> Logger.debug("Websocket disconnected"));
    });
  }
}
