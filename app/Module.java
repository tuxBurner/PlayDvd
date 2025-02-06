import com.google.inject.AbstractModule;
import com.typesafe.config.ConfigFactory;
import helpers.*;
import models.MovieImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.io.File;
import java.util.Iterator;

/**
 * This is the module which is always loaded by the application and prepares some stuff for the application.
 * Like MailHelper etc
 * Created by tuxburner on 28.01.17.
 */
public class Module extends AbstractModule {

  @Override
  protected void configure() {

    startUp();

    // bind the MailerHelper to an Instance
    bind(MailerHelper.class).asEagerSingleton();
    bind(CacheHelper.class).asEagerSingleton();
  }

  /**
   * Does some stuff when the application is starting
   */
  private void startUp() {
    if (ImageHelper.IMAGE_ROOT.exists() == false) {
      Logger.info("The folder: " + ImageHelper.IMAGE_ROOT.getAbsolutePath() + " does not exists, creating it.");
      boolean mkdirs = ImageHelper.IMAGE_ROOT.mkdirs();
      if (mkdirs == false) {
        Logger.error("An error happened while generating the root image folder: " + ImageHelper.IMAGE_ROOT.getAbsolutePath());
      }
    }


    final boolean fillMovieImages = ConfigFactory.load().getBoolean("dvddb.fillMovieImages");
    if (fillMovieImages == true) {
      fillMovieImages();
    }
  }

  /**
   * Regenerates movie Images when needed.
   */
  private void fillMovieImages() {

    final Iterator<File> fileIterator = FileUtils.iterateFiles(ImageHelper.IMAGE_ROOT, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);

    while (fileIterator.hasNext()) {
      final File next = fileIterator.next();

      final String fileName = next.getName();
      final String baseName = FilenameUtils.getBaseName(fileName);
      final String[] split = StringUtils.split(baseName, "_", 3);

      if (split.length != 3) {
        Logger.error("File name " + baseName + " could not split to 3 parts");
        continue;
      }

      final Long movieId = Long.valueOf(split[0]);
      final EImageType type = EImageType.valueOf(split[1]);
      final EImageSize size = EImageSize.valueOf(split[2]);

      final boolean exists = MovieImage.checkForImage(movieId, size, type);
      if (exists == false) {
        MovieImage.createMovieImage(movieId, size, type, EImageStoreType.LOCAL);
      }
    }
  }
}
