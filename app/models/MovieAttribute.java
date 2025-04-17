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
  public Long pk;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EMovieAttributeType attributeType;

  public String value;

  @ManyToMany(cascade = CascadeType.ALL)
  public Set<Movie> movies;

}
