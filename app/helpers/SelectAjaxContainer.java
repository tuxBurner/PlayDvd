package helpers;

/**
 * This is a simple container for the select2 and the ajax conversion from the
 * backend
 * 
 * @author tuxburner
 * 
 */
public class SelectAjaxContainer {

  public SelectAjaxContainer(final String id, final String text) {
    super();
    this.id = id;
    this.text = text;
  }

  public final String id;

  public final String text;

}
