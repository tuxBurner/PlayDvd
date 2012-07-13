package grabbers;

import play.api.mvc.PathBindable;
import scala.Either;

public class EgrabberBind implements PathBindable<EGrabberType> {

  @Override
  public Either<String, EGrabberType> bind(final String arg0, final String arg1) {

  }

  @Override
  public String javascriptUnbind() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String unbind(final String arg0, final EGrabberType arg1) {
    // TODO Auto-generated method stub
    return null;
  }

}
