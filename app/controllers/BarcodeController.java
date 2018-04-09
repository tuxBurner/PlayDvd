package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;

/**
 * This {@link Controller} is handling barcode scanning with a webcam
 * User: tuxburner
 * Date: 2/11/13
 * Time: 8:19 PM
 */
@Security.Authenticated(Secured.class)
@Singleton
public class BarcodeController extends Controller
{

  private final ActorSystem actorSystem;
  private final Materializer materializer;

  @Inject
  public BarcodeController(final ActorSystem actorSystem, final Materializer materializer)
  {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
  }

  /**
   * This initializes the barcodeScanner view
   *
   * @return
   */
  @JSRoute
  public Result displayBarcodeScaner()
  {
    return ok(views.html.barcode.barcodescanner.render());
  }

  @JSRoute
  public WebSocket scanBarcode()
  {
    return WebSocket.Text.accept(request ->
      ActorFlow.actorRef(BarcodeScannerActor::props,
        actorSystem, materializer
      )
    );

  }
}
