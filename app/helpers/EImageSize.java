package helpers;

public enum EImageSize {
    ORIGINAL(0,0),
	SMALL(150,150);
	
	private final int width;
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private final int height;

	private  EImageSize(final int width, final int height) {
		this.width = width;
		this.height = height; 
		
	}
}
