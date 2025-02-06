package helpers;

import com.typesafe.config.ConfigFactory;
import models.MovieImage;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class ImageHelper {

  public static File IMAGE_ROOT = new File(ConfigFactory.load().getString("dvddb.imagespath"));

  /**
   * Loads the image from the given url and saves it to the filesystem
   *
   * @param movieId
   * @param urlStr
   * @param imageType
   * @return
   */
  public static boolean createFileFromUrl(final Long movieId, final String urlStr, final EImageType imageType) {

    if (StringUtils.isEmpty(urlStr) == true) {
      return false;
    }

    if (Logger.isDebugEnabled() == true) {
      Logger.debug("Trying to load image from url: " + urlStr);
    }

    InputStream is;
    try {
      final URL url = new URL(urlStr);
      is = url.openStream();
    } catch (MalformedURLException e) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("An error happend with creating the URL " + urlStr + " for the image", e);
      }
      return false;
    } catch (IOException e) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("An error happend while loading the data from the url: " + urlStr, e);
      }
      return false;
    }

    boolean success;


    success = createOrigImageLocal(movieId, imageType, EImageSize.ORIGINAL, is);


    IOUtils.closeQuietly(is);
    return success;
  }


  /**
   * Creates file in the local file system
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @param is
   * @return
   */
  private static boolean createOrigImageLocal(final Long movieId, final EImageType imageType, final EImageSize imageSize, final InputStream is) {

    try {
      final File file = ImageHelper.createFile(movieId, imageType, imageSize);

      if (Logger.isDebugEnabled() == true) {
        Logger.debug("Creating file localy: " + file.getAbsolutePath());
      }

      if (file.exists() == true) {
        if (Logger.isDebugEnabled() == true) {
          Logger.debug("Original file: " + file.getAbsoluteFile() + " already exists deleting it");
        }

        final IOFileFilter fileFilter = new PrefixFileFilter(buildSubImagesPrefix(movieId, imageType));

        // delete all files from the same movie :)
        final Iterator<File> iterateFiles = FileUtils.iterateFiles(ImageHelper.IMAGE_ROOT, fileFilter, null);
        while (iterateFiles.hasNext()) {
          final File subFile = iterateFiles.next();
          if (Logger.isDebugEnabled() == true) {
            Logger.debug("Found subImage: " + subFile.getAbsoluteFile() + " deleting it.");
          }
          FileUtils.deleteQuietly(subFile);
        }
        MovieImage.deleteForMovie(movieId);
      }

      FileUtils.copyInputStreamToFile(is, file);
      MovieImage.createMovieImage(movieId, imageSize, imageType, EImageStoreType.LOCAL);


      return true;
    } catch (final Exception e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("An error happend while saving the image to the filesystem", e);
      }
      return false;
    }
  }

  /**
   * Gets the file for an image
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @return
   */
  public static String getImageFile(final Long movieId, final EImageType imageType, final EImageSize imageSize) {

    final File file = ImageHelper.createFile(movieId, imageType, imageSize);
    // if this is not the original one we will creat one :)
    if (file.exists() == false && imageSize.equals(EImageSize.ORIGINAL) == false) {
      // first we load the original one
      final File origFile = ImageHelper.createFile(movieId, imageType, EImageSize.ORIGINAL);
      if (origFile.exists() == false) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("Could not find original Image: " + origFile.getAbsolutePath());
        }
        return null;
      }
      final File tmpFile = ImageHelper.resizeImage(origFile, imageSize);
      if (tmpFile == null) {
        return null;
      }
      try {
        FileUtils.copyFile(tmpFile, file);
      } catch (IOException e) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("Could not copy file: " + tmpFile.getAbsolutePath() + " to: " + file.getAbsolutePath());
        }
      }
      FileUtils.deleteQuietly(tmpFile);
      MovieImage.createMovieImage(movieId, imageSize, imageType, EImageStoreType.LOCAL);
    }

    if (file.exists() == true) {
      return file.getAbsolutePath();
    } else {
      return null;
    }
  }

  /**
   * resizes the image
   *
   * @param origImgFile
   * @param destSize
   */
  private static File resizeImage(final File origImgFile, final EImageSize destSize) {
    try {
      File destImgFile = File.createTempFile("play", "resize.jpeg");
      Thumbnails.of(origImgFile).size(destSize.getWidth(), destSize.getHeight()).toFile(destImgFile);
      return destImgFile;
    } catch (final IOException e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("An error happened while resizing the image: " + origImgFile.getAbsolutePath(), e);
      }
    }

    return null;
  }

  /**
   * For local file storage this creates a file with the correct name in the IMAGE_ROOT path
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @return
   */
  private static File createFile(final Long movieId, final EImageType imageType, final EImageSize imageSize) {
    final File file = new File(ImageHelper.IMAGE_ROOT, createImageFileName(movieId, imageType, imageSize));
    return file;
  }

  /**
   * Creates the name of the image with the given informations
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @return
   */
  private static String createImageFileName(final Long movieId, final EImageType imageType, final EImageSize imageSize) {
    return movieId + "_" + imageType.name() + "_" + imageSize.name() + ".jpg";
  }

  /**
   * Builds the prefix for finding all the images from the same movie and type
   *
   * @param movieId
   * @param imageType
   * @return
   */
  private static String buildSubImagesPrefix(final Long movieId, final EImageType imageType) {
    return movieId + "_" + imageType.name();
  }

}
