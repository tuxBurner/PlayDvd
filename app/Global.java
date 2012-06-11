import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import models.Dvd;
import models.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.typesafe.config.ConfigFactory;

import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {

  @Override
  public void onStart(final Application app) {
    final boolean boolean1 = ConfigFactory.load().getBoolean("dvddb.fillDvds");
    if (boolean1 == true) {
      InitialData.insert(app);
    }
  }

  static class InitialData {

    public static void insert(final Application app) {

      final User user = new User();
      user.password = "Hallo123";
      user.email = "sebasth@gmx.de";
      user.userName = "tuxburner";
      User.create(user);

      if (user != null) {

        // load the csv
        final File file = new File("/home/tuxburner/Downloads/export.csv");
        if (file.exists() == true) {
          try {
            final List<String> readLines = FileUtils.readLines(file, "utf-8");
            for (int i = 1; i < readLines.size(); i++) {
              final String string = readLines.get(i);
              final String[] split = string.split(",");
              final Dvd dvd = new Dvd();
              dvd.createdDate = new Date().getTime();
              dvd.owner = user;
              dvd.title = split[1].trim();
              dvd.hasPoster = false;
              dvd.hasBackdrop = false;
              final String trimToNull = StringUtils.trimToNull(split[0]);
              if (StringUtils.isEmpty(trimToNull) == false && StringUtils.isNumeric(trimToNull)) {
                dvd.hullNr = Integer.valueOf(trimToNull);
              }

              if (split.length == 3) {
                final String trimToNull2 = StringUtils.trimToNull(split[2]);
                if (StringUtils.isEmpty(trimToNull2) == false && StringUtils.isNumeric(trimToNull2)) {
                  dvd.year = Integer.valueOf(trimToNull2);
                } else {
                  dvd.year = 2012;
                }
              } else {
                dvd.year = 2012;
              }

              dvd.save();
            }
          } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }

    }

  }

}
