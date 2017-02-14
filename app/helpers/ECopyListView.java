package helpers;

/**
 * The type of list views which are avaible
 * User: tuxburner
 * Date: 5/1/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public enum ECopyListView {
  COVERVIEW("fa-th-large"),
  SMALLCOVERVIEW("fa-th"),
  TABLEVIEW("fa-table");

  public final String icon;

  ECopyListView(final String icon) {
     this.icon = icon;
  }
}
