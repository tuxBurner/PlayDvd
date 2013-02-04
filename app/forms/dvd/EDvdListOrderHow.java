package forms.dvd;

public enum EDvdListOrderHow {

  UP("asc"),
  DOWN("desc");

  /**
   * The field in the db this enum orders the list
   */
  public String dbOrder;

  private EDvdListOrderHow(final String dbField) {
    this.dbOrder = dbField;
  }

}
