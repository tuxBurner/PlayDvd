package helpers;

public enum EImageSize {
  ORIGINAL(0, 0),
  SMALL(180, 255),
  SMALL_LIST_VIEW(77,114),
  GRABBER_POSTER_SMALL(125, 125),
  GRABBER_BACKDROP_SMALL(290, 163),
  SELECT2(60, 80),
  TINY(25, 25),
  BACKCKDROP_POPUP_SIZE(1024,768);

  private final int width;

  private final int height;

  private final String whString;

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getWHString() {
    return whString;
  }

  EImageSize(final int width, final int height) {
    this.width = width;
    this.height = height;
    this.whString = width+"x"+height;
  }
}
