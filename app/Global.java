import forms.DvdForm;
import helpers.ImageHelper;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import models.Dvd;
import models.EDvdAttributeType;
import models.Movie;
import models.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import play.Application;
import play.GlobalSettings;
import play.Logger;

import com.typesafe.config.ConfigFactory;

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

  }

  static class InitialData {

    public static void insert(final Application app) {

      final User user = new User();

      user.password = "hallo123";
      user.email = "sebasth@gmx.de";
      user.userName = "tuxBurner";
      user.defaultCopyType = "BLURAY";
      User.create(user);

      if (user != null) {

        // load the csv
        final File file = new File("export.csv");
        if (file.exists() == true) {
          try {

            final List<String> ageRatings = DvdForm.getAgeRatings();

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
