package forms.copy.objects;

public enum ECopyListOrderHow {

  UP("asc"),
  DOWN("desc");

  /**
   * The field in the db this enum orders the list
   */
  public String dbOrder;

  ECopyListOrderHow(final String dbField) {
    this.dbOrder = dbField;
  }

}
