import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import models.Dvd;
import models.Movie;
import models.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import play.Application;
import play.GlobalSettings;

import com.typesafe.config.ConfigFactory;

public class Global extends GlobalSettings {

  @Override
  public void onStart(final Application app) {
    final boolean boolean1 = ConfigFactory.load().getBoolean("dvddb.fillDvds");
    if (boolean1 == true) {
      InitialData.insert(app);
    }

    // final Yaml yaml = new Yaml();
    // final Map<String, Object> map = new HashMap<String, Object>();
    //
    // final List<User> users = User.find.all();
    // map.put(User.class.getCanonicalName(), users);
    //
    // final List<Dvd> dvds = Dvd.find.all();
    // map.put(Dvd.class.getCanonicalName(), dvds);
    //
    // final List<Movie> movies = Movie.find.all();
    // map.put(Movie.class.getCanonicalName(), movies);
    //
    // final List<MovieAttibute> movieAttributes = MovieAttibute.finder.all();
    // map.put(MovieAttibute.class.getCanonicalName(), movieAttributes);
    //
    // final String dump = yaml.dump(map);
    // System.out.println(dump);
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
              dvd.movie = new Movie();
              dvd.createdDate = new Date().getTime();
              dvd.owner = user;
              dvd.movie.title = split[1].trim();
              dvd.movie.hasPoster = false;
              dvd.movie.hasBackdrop = false;
              final String trimToNull = StringUtils.trimToNull(split[0]);
              if (StringUtils.isEmpty(trimToNull) == false && StringUtils.isNumeric(trimToNull)) {
                dvd.hullNr = Integer.valueOf(trimToNull);
              }

              if (split.length == 3) {
                final String trimToNull2 = StringUtils.trimToNull(split[2]);
                if (StringUtils.isEmpty(trimToNull2) == false && StringUtils.isNumeric(trimToNull2)) {
                  dvd.movie.year = Integer.valueOf(trimToNull2);
                } else {
                  dvd.movie.year = 2012;
                }
              } else {
                dvd.movie.year = 2012;
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
