import helpers.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;

import play.Application;
import play.GlobalSettings;
import play.Logger;

import com.typesafe.config.ConfigFactory;
import plugins.s3.S3Plugin;

public class Global extends GlobalSettings {

  @Override
  public void onStart(final Application app) {

    if (ImageHelper.IMAGE_ROOT.exists() == false) {
      Logger.info("The folder: " + ImageHelper.IMAGE_ROOT.getAbsolutePath() + " does not exists, creating it.");
      ImageHelper.IMAGE_ROOT.mkdirs();
    }

    final boolean boolean1 = ConfigFactory.load().getBoolean("dvddb.fillDvds");
    if (boolean1 == true) {
      InitialData.insert(app);
    }

    final boolean fillMovieImgs = ConfigFactory.load().getBoolean("dvddb.fillMovieImages");
    if(fillMovieImgs == true) {
      fillMovieImages();
    }


  }

  private static void fillMovieImages() {

    final Iterator<File> fileIterator = FileUtils.iterateFiles(ImageHelper.IMAGE_ROOT, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);
    if(S3Plugin.pluginEnabled() == false) {
      while(fileIterator.hasNext()) {
        final File next = fileIterator.next();

        final String fileName = next.getName();
        final String baseName = FilenameUtils.getBaseName(fileName);
        final String[] split = StringUtils.split(baseName, "_",3);

        if(split.length != 3) {
          Logger.error("File name " + baseName + " could not split to 3 parts");
          continue;
        }

        final Long movieId = Long.valueOf(split[0]);
        final EImageType type = EImageType.valueOf(split[1]);
        final EImageSize size = EImageSize.valueOf(split[2]);

        final boolean exists = MovieImage.checkForImage(movieId, size, type);
        if(exists == false) {
          MovieImage.createMovieImage(movieId,size,type, EImageStoreType.LOCAL);
        }

      }
    } else {
      while(fileIterator.hasNext()) {
        final File next = fileIterator.next();

        final String fileName = next.getName();
        final String baseName = FilenameUtils.getBaseName(fileName);
        final String[] split = StringUtils.split(baseName, "_",3);

        if(split.length != 3) {
          Logger.error("File name " + baseName + " could not split to 3 parts");
          continue;
        }

        final Long movieId = Long.valueOf(split[0]);
        final EImageType type = EImageType.valueOf(split[1]);
        final EImageSize size = EImageSize.valueOf(split[2]);

        final boolean exists = MovieImage.checkForImage(movieId, size, type);
        if(exists == false && EImageSize.ORIGINAL.equals(size) == true) {
          MovieImage.createMovieImage(movieId,size,type, EImageStoreType.S3);
        } else {
          final MovieImage movieImage = MovieImage.getForMovie(movieId, size, type);
          if(EImageSize.ORIGINAL.equals(size) == true) {
            movieImage.storeType = EImageStoreType.S3;
            movieImage.save();
          } else  {
            movieImage.delete();
          }
        }

        if(EImageSize.ORIGINAL.equals(size) == false) {
          Logger.debug("Only migrating images of the original size to s3 cloud skipping file: "+fileName);
          continue;
        }

        Logger.info("Migrating local file to s3: "+fileName);
        S3Plugin.amazonS3.putObject(S3Plugin.s3Bucket,fileName,next);

      }
    }
  }

  static class InitialData {

    public static void insert(final Application app) {

      final User user = new User();

      user.password = "hallo123";
      user.email = "test@gmx.de";
      user.userName = "tuxBurner";
      user.defaultCopyType = "BLURAY";
      User.create(user);

      if (user != null) {

        // load the csv
        final File file = new File("export.csv");
        if (file.exists() == true) {
          try {

            final List<String> ageRatings = DvdInfoHelper.getAgeRatings();

            final List<String> readLines = FileUtils.readLines(file, "iso-8859-1");
            for (int i = 1; i < readLines.size(); i++) {
              final String string = readLines.get(i);
              final String[] split = string.split(",");
              final Dvd dvd = new Dvd();
              dvd.movie = new Movie();
              dvd.movie.hasToBeReviewed = true;
              dvd.createdDate = new Date().getTime();
              dvd.owner = user;
              dvd.movie.title = split[1].trim();
              dvd.movie.hasPoster = false;
              dvd.movie.hasBackdrop = false;
              Dvd.addSingleAttribute("DVD", EDvdAttributeType.COPY_TYPE, dvd);
              final String trimToNull = StringUtils.trimToNull(split[0]);
              if (StringUtils.isEmpty(trimToNull) == false && StringUtils.isNumeric(trimToNull)) {
                dvd.hullNr = Integer.valueOf(trimToNull);
              }

              final String trimToNull2 = StringUtils.trimToNull(split[2]);
              if (StringUtils.isEmpty(trimToNull2) == false && StringUtils.isNumeric(trimToNull2)) {
                dvd.movie.year = Integer.valueOf(trimToNull2);
              } else {
                dvd.movie.year = 2012;
              }

              if (split.length == 4) {
                final String string2 = StringUtils.trimToEmpty(split[3]);
                if (ageRatings.contains(string2)) {
                  Dvd.addSingleAttribute(string2, EDvdAttributeType.RATING, dvd);
                }
              }

              dvd.movie.save();

              dvd.save();
            }
          } catch (final IOException e) {
            if(Logger.isErrorEnabled()) {
              Logger.error("An error happend while adding new Movies to the database",e);
            }
          }
        }

      }

    }

  }

}
