package controllers;

public class DvdPageNav {

  public String displayVal;

  public Integer idx;

  public DvdPageNav(final int idx) {
    displayVal = String.valueOf(idx + 1);
    this.idx = idx;
  }

  public DvdPageNav(final String displayVal) {
    this.displayVal = displayVal;
  }

}
