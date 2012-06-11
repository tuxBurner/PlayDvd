package controllers;

import java.util.List;

import models.Dvd;

import com.avaje.ebean.Page;

public class DvdPage {

  public final Page<Dvd> page;

  public final List<Dvd> list;

  public int fromIdx;

  public int toIdx;

  public boolean leftDotted;

  public DvdPage(final Page<Dvd> page) {
    this.page = page;
    list = page.getList();

    final int totalPageCount = page.getTotalPageCount();
    fromIdx = 0;
    toIdx = page.getTotalPageCount();

    if (totalPageCount > 10) {
      final int pageIndex = page.getPageIndex();
      if (pageIndex < 10) {
        leftDotted = true;
      } else {
        leftDotted = false;
      }

    }
  }

}
