package grabbers;

/**
 * Image types used by TheTvDb
 */
public enum ETheTvDbImageType {
    POSTER("poster"),
    FANART("fanart"),
    BANNER("series"),
    SEASON("season"),
    SEASON_WIDE("seasonwide");

    /**
     * The field in the db this enum orders the list
     */
    public String type;

    ETheTvDbImageType(final String type) {
        this.type = type;
    }


}
