package controllers;


import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BarcodeScannerActor extends AbstractActor
{

  public static Props props(ActorRef out)
  {
    return Props.create(BarcodeScannerActor.class, out);
  }

  private final ActorRef out;

  public BarcodeScannerActor(ActorRef out)
  {
    this.out = out;
  }

  @Override
  public Receive createReceive()
  {
    return receiveBuilder()
      .match(String.class, message -> {
        if (StringUtils.startsWith(message, "data:image/jpeg;base64,")) {
          Logger.debug("Got an image. Trying to parse it.");

          try {
            byte decoded[] = Base64.decodeBase64(StringUtils.removeStart(message, "data:image/jpeg;base64,"));
            InputStream inputStream = new ByteArrayInputStream(decoded);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            EAN13Reader reader = new EAN13Reader();
            com.google.zxing.Result decode = reader.decode(bitmap);
            String decodeText = decode.getText();
            if (Logger.isDebugEnabled()) {
              Logger.debug("Got EAN code: " + decodeText + " from image.");
            }

            out.tell(decodeText, self());

          } catch (IOException e) {
            if (Logger.isErrorEnabled() == true) {
              Logger.error("An error happend while reading the image.", e);
            }
            out.tell("error", self());
          } catch (NotFoundException e) {
            if (Logger.isErrorEnabled() == true) {
              Logger.error("Could not extract barcode from image.", e);
              out.tell("error", self());
            }
          } catch (FormatException e) {
            Logger.error("An error happend while reading the image.", e);
            out.tell("error", self());
          }
        } else {
          out.tell("Error No Image was send via webseocket.", self());
        }
      })
      .build();
  }
}
