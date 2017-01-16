package helpers;

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDefaultImage;
import com.timgroup.jgravatar.GravatarRating;

/**
 * Created by tuxburner on 12.01.17.
 */
public class GravatarHelper {

    /**
     * Gets the bytes of a gravatar. Returns null when the user has none.
     * @param gravatarEmail the mail of the gravatar the user has.
     * @param size the size the gravatar should have.
     */
    public static byte[] getGravatarBytes(final String gravatarEmail, final int size) {
        final Gravatar gravatar = new Gravatar();
        gravatar.setSize(size);
        gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
        gravatar.setDefaultImage(GravatarDefaultImage.GRAVATAR_ICON);
        byte[] gravatarBytes = gravatar.download(gravatarEmail);
        return gravatarBytes;
    }

}
