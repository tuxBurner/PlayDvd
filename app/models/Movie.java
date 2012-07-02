package models;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Ebean;

import forms.DvdForm;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Movie extends Model {

  private static final long serialVersionUID = -2107177668174396511L;

  @Id
  public Long id;

  @Required
  public String title;

  public Boolean hasPoster;

  public Boolean hasBackdrop;

  @Lob
  public String description;

  @Required
  @Column(nullable = false)
  public Integer year;

  public Integer runtime;

  @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "movies")
  public Set<DvdAttibute> attributes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "movie", orphanRemoval = true)
  public Set<Dvd> dvds;

  /**
   * The finder for the database for searching in the database
   */
  public static Finder<Long, Movie> find = new Finder<Long, Movie>(Long.class, Movie.class);

  /**
   * This creates a movie from the information of the given {@link DvdForm}
   * 
   * @param dvdForm
   * @param movie
   * @return
   * @throws Exception
   */
  public static Movie editOrAddFromForm(final DvdForm dvdForm, Movie movie) throws Exception {
    if (movie == null) {
      movie = new Movie();
    }

    movie.title = dvdForm.title;
    movie.description = dvdForm.plot;
    movie.year = dvdForm.year;
    movie.runtime = dvdForm.runtime;

    if (movie.id == null) {
      movie.hasPoster = false;
      movie.hasBackdrop = false;
      movie.save();
    } else {
      Ebean.deleteManyToManyAssociations(movie, "attributes");
    }

    // add the images if we have some :)
    final Boolean newPoster = ImageHelper.createFileFromUrl(movie.id, dvdForm.posterUrl, EImageType.POSTER, EImageSize.ORIGINAL);
    if (movie.hasPoster == false || movie.hasPoster == null) {
      movie.hasPoster = newPoster;
    }

    final Boolean newBackDrop = ImageHelper.createFileFromUrl(movie.id, dvdForm.backDropUrl, EImageType.BACKDROP, EImageSize.ORIGINAL);
    if (movie.hasBackdrop == false || movie.hasBackdrop == null) {
      movie.hasBackdrop = newBackDrop;
    }

    movie.attributes = new HashSet<DvdAttibute>();

    // gather all the genres and add them to the dvd
    final Set<DvdAttibute> genres = DvdAttibute.gatherAndAddAttributes(new HashSet<String>(dvdForm.genres), EAttributeType.GENRE);
    movie.attributes.addAll(genres);

    final Set<DvdAttibute> actors = DvdAttibute.gatherAndAddAttributes(new HashSet<String>(dvdForm.actors), EAttributeType.ACTOR);
    movie.attributes.addAll(actors);

    Movie.addSingleAttribute(dvdForm.director, EAttributeType.DIRECTOR, movie);
    Movie.addSingleAttribute(dvdForm.box, EAttributeType.BOX, movie);
    Movie.addSingleAttribute(dvdForm.collection, EAttributeType.COLLECTION, movie);

    // save all the attributes to the database :)
    // movie.saveManyToManyAssociations("attributes");

    movie.update();

    return movie;
  }

  /**
   * Adds a single Attribute to the dvd
   * 
   * @param attrToAdd
   * @param attributeType
   * @param dvd
   */
  private static void addSingleAttribute(final String attrToAdd, final EAttributeType attributeType, final Movie movie) {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<String>();
    attribute.add(attrToAdd);
    final Set<DvdAttibute> dbAttrs = DvdAttibute.gatherAndAddAttributes(attribute, attributeType);
    movie.attributes.addAll(dbAttrs);
  }

  /**
   * List all movies orderd by the title only fetching id and title
   * 
   * @return
   */
  public static List<Movie> listByDistinctTitle() {
    return Movie.find.select("id,title").order("title asc").findList();
  }

}
