package helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.s3.model.*;
import models.MovieImage;
import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang.StringUtils;

import com.typesafe.config.ConfigFactory;
import play.Logger;
import plugins.s3.S3Plugin;

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

    boolean success = false;

    if (S3Plugin.pluginEnabled() == false) {
      success = createOrigImageLocal(movieId, imageType, EImageSize.ORIGINAL, is);
    } else {
      success = createOrigImageOnS3(movieId, imageType, EImageSize.ORIGINAL, is);
    }

    IOUtils.closeQuietly(is);
    return success;
  }

  /**
   * Creates a file on the s3 bucket
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @param is
   * @return
   */
  private static boolean createOrigImageOnS3(final Long movieId, final EImageType imageType, final EImageSize imageSize, final InputStream is) {

    final String imageFileName = createImageFileName(movieId, imageType, imageSize);
    if (Logger.isDebugEnabled() == true) {
      Logger.debug("Creating file " + imageFileName + " on s3 bucket:" + S3Plugin.s3Bucket);
    }

    File tempFile = null;
    try {

      final ObjectListing s3Objects = S3Plugin.amazonS3.listObjects(S3Plugin.s3Bucket, buildSubImagesPrefix(movieId, imageType));
      final List<S3ObjectSummary> objectSummaries = s3Objects.getObjectSummaries();
      if (CollectionUtils.isEmpty(objectSummaries) == false) {
        for (S3ObjectSummary s3ObjectSummary : objectSummaries) {
          if (Logger.isDebugEnabled() == true) {
            Logger.debug("Deleting image: " + s3ObjectSummary.getKey() + " from s3 bucket: " + S3Plugin.s3Bucket);
          }
          S3Plugin.amazonS3.deleteObject(S3Plugin.s3Bucket, s3ObjectSummary.getKey());
        }

        MovieImage.deleteForMovie(movieId);
      }

      tempFile = File.createTempFile("play", "tmp");
      FileUtils.copyInputStreamToFile(is,tempFile);


      PutObjectRequest putObjectRequest = new PutObjectRequest(S3Plugin.s3Bucket, imageFileName, tempFile);
      putObjectRequest.withCannedAcl(CannedAccessControlList.Private); // original is private
      S3Plugin.amazonS3.putObject(putObjectRequest);

      FileUtils.deleteQuietly(tempFile);

      MovieImage.createMovieImage(movieId,imageSize,imageType,EImageStoreType.S3);

      return true;
    } catch (Exception e) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not create file on S3 bucket: " + S3Plugin.s3Bucket, e);
      }
      FileUtils.deleteQuietly(tempFile);
      return false;
    }
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

      FileUtils.copyInputStreamToFile(is,file);
      MovieImage.createMovieImage(movieId,imageSize,imageType,EImageStoreType.LOCAL);


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
   *
   * @param movieId
   * @param imageType
   * @param imageSize
   * @return
   */
  public static String getImageFile(final Long movieId, final EImageType imageType, final EImageSize imageSize) {

    if (S3Plugin.pluginEnabled() == false) {
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
        MovieImage.createMovieImage(movieId,imageSize,imageType,EImageStoreType.LOCAL);
      }

      if (file.exists() == true) {
        return file.getAbsolutePath();
      } else {
        return null;
      }
    } else {
      try {
        // check if the file exists
        final String imageFileName = createImageFileName(movieId, imageType, imageSize);
        final boolean imageExists = MovieImage.checkForImage(movieId, imageSize, imageType);

        if (imageExists == false && imageSize.equals(EImageSize.ORIGINAL) == false) {

          if (Logger.isDebugEnabled() == true) {
            Logger.debug("Could not find: " + imageFileName + " from s3 bucket: " + S3Plugin.s3Bucket + " creating one from the original one.");
          }

          // get the original image from the bucket
          final String origImgName = createImageFileName(movieId, imageType, EImageSize.ORIGINAL);
          final S3Object origImage = S3Plugin.amazonS3.getObject(S3Plugin.s3Bucket, origImgName);
          if (origImage == null) {
            if (Logger.isErrorEnabled() == true) {
              Logger.error("Could not find original image: " + origImgName + " from the s3 bucket: " + S3Plugin.s3Bucket);
            }
            return null;
          }

          final S3ObjectInputStream origImgContent = origImage.getObjectContent();
          final File origTempFile = File.createTempFile("play", "temp");
          FileUtils.copyInputStreamToFile(origImgContent, origTempFile);
          IOUtils.closeQuietly(origImgContent);
          final File resizedImage = resizeImage(origTempFile, imageSize);
          FileUtils.deleteQuietly(origTempFile);
          if(resizedImage == null) {
            return null;
          }

          PutObjectRequest putObjectRequest = new PutObjectRequest(S3Plugin.s3Bucket, imageFileName, resizedImage);
          putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // original is private
          S3Plugin.amazonS3.putObject(putObjectRequest);
          FileUtils.deleteQuietly(resizedImage);
          MovieImage.createMovieImage(movieId,imageSize,imageType,EImageStoreType.S3);
        }

        return S3Plugin.buildUrl(imageFileName);
      } catch (Exception e) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("An error happened while getting the image from s3 bucket:" + S3Plugin.s3Bucket, e);
        }
      }

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
