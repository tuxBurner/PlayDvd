package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helpers.SelectAjaxContainer;
import io.ebean.Finder;
import io.ebean.Model;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the {@link Entity} holding certain attributes
 * 
 * @author tuxburner
 * 
 */
@Entity
public class MovieAttribute extends Model
{

  /**
   * Default FINDER for the {@link MovieAttribute}
   */
  public static final Finder<Long, MovieAttribute> FINDER = new Finder<>(MovieAttribute.class);

  @Id
  private Long pk;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EMovieAttributeType attributeType;

  private String value;

  @ManyToMany(cascade = CascadeType.ALL)
  private Set<Movie> movies;

  public Long getPk() {
    return pk;
  }

  public void setPk(Long pk) {
    this.pk = pk;
  }

  public EMovieAttributeType getAttributeType() {
    return attributeType;
  }

  public void setAttributeType(EMovieAttributeType attributeType) {
    this.attributeType = attributeType;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Set<Movie> getMovies() {
    return movies;
  }

  public void setMovies(Set<Movie> movies) {
    this.movies = movies;
  }
}
