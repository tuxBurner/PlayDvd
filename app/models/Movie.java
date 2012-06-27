package models;

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
   * List all movies orderd by the title only fetching id and title
   * 
   * @return
   */
  public static List<Movie> listByDistinctTitle() {
    return Movie.find.select("id,title").order("title asc").findList();
  }

}
