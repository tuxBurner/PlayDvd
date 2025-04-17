package models;


import dao.UserDao;
import forms.dvd.CopyForm;
import forms.dvd.CopySearchFrom;
import forms.dvd.objects.EDvdListOrderBy;
import forms.dvd.objects.EDvdListOrderHow;
import forms.dvd.objects.PrevNextCopies;
import io.ebean.*;
import io.ebean.Query;
import jakarta.persistence.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.mvc.Http;

import java.util.*;

@Entity
@Table(name = "dvd")
public class Dvd extends Model {

  /**
   * The FINDER for the database for searching in the database
   */
  public static final Finder<Long, Dvd> FINDER = new Finder<>(Dvd.class);

  public static final String HULL_NR_SEARCH = "hull:";

  public static final String EAN_NR_SEARCH = "ean:";

  @Id
  public Long id;

  @ManyToOne
  public User owner;

  @OneToOne
  public User borrower;

  @ManyToMany(cascade = CascadeType.MERGE, mappedBy = "dvds")
  public Set<DvdAttribute> attributes;

  public Long borrowDate;


  /**
   * If this is set the user entered a free name which does not exists in the
   * database
   */
  public String borrowerName;

  /**
   * The number off the hull off the dvd
   */
  public Integer hullNr;

  /**
   * Ean Number of the dvd so we can find it again
   */
  public String eanNr;

  /**
   * The Amazon asin nr of the copy
   */
  public String asinNr;

  /**
   * Additional info off the copy like directors cut
   */
  public String additionalInfo;

  /**
   * The movie which is on the dvd
   */
  @ManyToOne
  @Column(nullable = false)
  public Movie movie;

  @Required
  @Column(nullable = false)
  public Long createdDate;


}
