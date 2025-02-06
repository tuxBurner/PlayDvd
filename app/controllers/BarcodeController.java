package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.streams.ActorFlow;
import play.mvc.*;

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
  private final MessagesApi messagesApi;

  @Inject
  public BarcodeController(final ActorSystem actorSystem, final Materializer materializer, MessagesApi messagesApi)
  {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
    this.messagesApi = messagesApi;
  }

  /**
   * This initializes the barcodeScanner view
   *
   * @return
   */
  @JSRoute
  public Result displayBarcodeScaner(final Http.Request request)
  {
    final Messages messages = this.messagesApi.preferred(request);

    return ok(views.html.barcode.barcodescanner.render(messages));
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
