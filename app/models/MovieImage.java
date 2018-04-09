package models;

import helpers.EImageSize;
import helpers.EImageStoreType;
import helpers.EImageType;
import io.ebean.Finder;
import io.ebean.Model;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Set;

/**
 * Holds all Image informations for the movie images
 * This is needed so we dont have to ask always s3 if the file exists or not
 *
 * User: tuxburner
 * Date: 5/18/13
 * Time: 1:10 PM
 */
@Entity
public class MovieImage extends Model
{

  @Id
  public Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EImageSize size;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EImageType type;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EImageStoreType storeType;

  @ManyToOne
  public Movie movie;

  /**
   * The FINDER for the database for searching in the database
   */
  public static Finder<Long, MovieImage> FINDER = new Finder<>(MovieImage.class);


  /**
   * Checks if the image exists in the database
   * @param movieId
   * @param size
   * @param type
   * @return
   */
  public static boolean checkForImage(final Long movieId, final EImageSize size, final EImageType type) {
    final int rowCount = FINDER.query()
      .where()
      .eq("movie.id", movieId)
      .eq("size", size).eq("type", type)
      .findCount();
    return (rowCount > 0);
  }

  public static MovieImage getForMovie(final Long movieId, final EImageSize size, final EImageType type) {
    final MovieImage movieImage = FINDER.query()
      .where()
      .eq("movie.id", movieId)
      .eq("size", size)
      .eq("type", type)
      .findOne();
    return movieImage;
  }

  /**
   * Creates a new {@link MovieImage}
   * @param movieId
   * @param size
   * @param type
   */
  public static void createMovieImage(final Long movieId, final EImageSize size, final EImageType type, final EImageStoreType storeType) {
    final Movie movie = Movie.FINDER.byId(movieId);
    if(movie == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not fing movie with id: "+movieId+" for creating a: "+MovieImage.class.getName());
      }
      return;
    }

    final MovieImage image = new MovieImage();
    image.movie = movie;
    image.size = size;
    image.type = type;
    image.storeType = storeType;

    image.save();
  }

  /**
   * Deletes all images for a movie
   * @param movieId
   */
  public static  void deleteForMovie(final Long movieId) {
    final Set<MovieImage> set = FINDER.query()
      .where()
      .eq("movie.id", movieId)
      .findSet();
    if(CollectionUtils.isEmpty(set) == false) {
      for(final MovieImage image : set) {
        image.delete();
      }
    }
  }

}
