package helpers;

public enum EImageSize {
  ORIGINAL(0, 0),
  SMALL(150, 150),
  GRABBER_POSTER_SMALL(125, 125),
  GRABBER_BACKDROP_SMALL(290, 163);

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
