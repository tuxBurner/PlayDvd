package grabbers;

public class GrabberException extends Exception {

    private static final long serialVersionUID = 1394606310445614392L;

    public GrabberException(final Exception e) {
        super(e);
    }

    public GrabberException(final String message) {
        super(message);
    }
}
