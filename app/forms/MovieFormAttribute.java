package forms;

/**
 * Attribute for the {@link DvdForm}
 * 
 * @author tuxburner
 */
public class MovieFormAttribute {

  public boolean selected = false;
  public String value;

  /**
   * @param selected
   * @param value
   */
  public MovieFormAttribute(final boolean selected, final String value) {
    super();
    this.selected = selected;
    this.value = value;
  }

}
