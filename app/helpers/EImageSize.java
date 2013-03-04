package helpers;

public enum EImageSize {
  ORIGINAL(0, 0),
  SMALL(180, 255),
  GRABBER_POSTER_SMALL(125, 125),
  GRABBER_BACKDROP_SMALL(290, 163),
  SELECT2(60, 80),
  TINY(25, 25),
  BACKCKDROP_POPUP_SIZE(1024,768);

  private final int width;

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  private final int height;

  private EImageSize(final int width, final int height) {
    this.width = width;
    this.height = height;

  }
}
