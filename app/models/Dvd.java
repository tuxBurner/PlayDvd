package models;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;

import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.typesafe.config.ConfigFactory;

import forms.DvdForm;
import forms.DvdListFrom;

@Entity
public class Dvd extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = -8607299241692950618L;

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

  @Required
  @Column(nullable = false)
  public Long createdDate;

  @ManyToOne
  public User owner;

  @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "dvds")
  public Set<DvdAttibute> attributes;

  @OneToOne
  public User borrower;

  public Long borrowDate;

  private final static int DEFAULT_DVDS_PER_PAGE = ConfigFactory.load().getInt("dvddb.dvds.perpage");

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
   * The finder for the database for searching in the database
   */
  public static Finder<Long, Dvd> find = new Finder<Long, Dvd>(Long.class, Dvd.class);

  /**
   * Persists the {@link Dvd} to the database
   * 
   * @param dvd
   * @return
   */
  public static Dvd create(final Dvd dvd) {
    dvd.save();
    return dvd;
  }

/**
	 * Creates a dvd from a {@link DvdForm)
	 * @param userName
	 * @param dvdForm
	 * @return 
	 * @throws Exception 
	 */
  public static Dvd createFromForm(final String userName, final DvdForm dvdForm) throws Exception {

    final User owner = User.getUserByName(userName);
    if (owner == null) {
      throw new Exception("User does not exist in the Database");
    }

    final Dvd dvd = new Dvd();
    dvd.owner = owner;

    return Dvd.createOrUpdateFromForm(dvdForm, dvd);
  }

  /**
   * Edits a dvd from the informations from the form
   * 
   * @param userName
   * @param dvdForm
   * @throws Exception
   */
  public static Dvd editFromForm(final String userName, final DvdForm dvdForm) throws Exception {
    final Dvd dvdForUser = Dvd.getDvdForUser(dvdForm.dvdId, userName);
    if (dvdForUser == null) {
      throw new Exception("The dvd could not been edited !");
    }

    return Dvd.createOrUpdateFromForm(dvdForm, dvdForUser);
  }

  /**
   * Creates or updates the given dvd
   * 
   * @param userName
   * @param dvdForm
   * @param dvd
   * @return
   * @throws Exception
   */
  private static Dvd createOrUpdateFromForm(final DvdForm dvdForm, final Dvd dvd) throws Exception {

    dvd.title = dvdForm.title;
    dvd.description = dvdForm.plot;
    dvd.year = dvdForm.year;
    dvd.runtime = dvdForm.runtime;
    dvd.hullNr = dvdForm.hullNr;

    Dvd dvdDb = null;
    if (dvd.id == null) {
      dvd.createdDate = new Date().getTime();
      dvd.hasPoster = false;
      dvd.hasBackdrop = false;
      dvdDb = Dvd.create(dvd);
    } else {
      Ebean.deleteManyToManyAssociations(dvd, "attributes");
      dvdDb = dvd;
    }

    // add the images if we have some :)
    final Boolean newPoster = ImageHelper.createFileFromUrl(dvdDb.id, dvdForm.posterUrl, EImageType.POSTER, EImageSize.ORIGINAL);
    if (dvdDb.hasPoster == false || dvdDb.hasPoster == null) {
      dvdDb.hasPoster = newPoster;
    }

    final Boolean newBackDrop = ImageHelper.createFileFromUrl(dvdDb.id, dvdForm.backDropUrl, EImageType.BACKDROP, EImageSize.ORIGINAL);
    if (dvdDb.hasBackdrop == false || dvdDb.hasBackdrop == null) {
      dvdDb.hasBackdrop = newBackDrop;
    }

    dvdDb.update();

    dvdDb.attributes = new HashSet<DvdAttibute>();

    // gather all the genres and add them to the dvd
    final Set<DvdAttibute> genres = DvdAttibute.gatherAndAddAttributes(new HashSet<String>(dvdForm.genres), EAttributeType.GENRE);
    dvdDb.attributes.addAll(genres);

    final Set<DvdAttibute> actors = DvdAttibute.gatherAndAddAttributes(new HashSet<String>(dvdForm.actors), EAttributeType.ACTOR);
    dvdDb.attributes.addAll(actors);

    Dvd.addSingleAttribute(dvdForm.director, EAttributeType.DIRECTOR, dvdDb);
    Dvd.addSingleAttribute(dvdForm.box, EAttributeType.BOX, dvdDb);
    Dvd.addSingleAttribute(dvdForm.collection, EAttributeType.COLLECTION, dvdDb);

    // save all the attributes to the database :)
    dvdDb.saveManyToManyAssociations("attributes");

    return dvdDb;
  }

  /**
   * Gets all dvds which have the same owner and the same attribute excluding
   * the given {@link Dvd}
   * 
   * @param attributeType
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdByAttrAndUser(final EAttributeType attributeType, final String attrValue, final Dvd dvd) {

    final List<Dvd> findList = Dvd.find.where().eq("attributes.attributeType", attributeType).eq("attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne("id", dvd.id).findList();

    return findList;
  }

  /**
   * Adds a single Attribute to the dvd
   * 
   * @param attrToAdd
   * @param attributeType
   * @param dvd
   */
  private static void addSingleAttribute(final String attrToAdd, final EAttributeType attributeType, final Dvd dvd) {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<String>();
    attribute.add(attrToAdd);
    final Set<DvdAttibute> dbAttrs = DvdAttibute.gatherAndAddAttributes(attribute, attributeType);
    dvd.attributes.addAll(dbAttrs);
  }

  /**
   * Gets all dvds for the given username ordered by the date
   * 
   * @param username
   * @return
   */
  public static Page<Dvd> getUserDvds(final String username, final Integer pageNr) {
    return Dvd.getByDefaultPaging(Dvd.find.where().eq("owner.userName", username), pageNr);
  }

  /**
   * Gets all dvds which fit into the Filter int the {@link DvdListFrom}
   * 
   * @param listFrom
   * @return
   */
  public static Page<Dvd> getDvdsByForm(final DvdListFrom listFrom) {
    final ExpressionList<Dvd> where = Dvd.find.where();

    if (StringUtils.isEmpty(listFrom.searchFor) == false) {
      where.like("title", "%" + listFrom.searchFor + "%");
    }

    if (StringUtils.isEmpty(listFrom.genre) == false) {
      where.eq("attributes.value", listFrom.genre).eq("attributes.attributeType", EAttributeType.GENRE);
    }

    if (StringUtils.isEmpty(listFrom.actor) == false) {
      where.eq("attributes.value", listFrom.actor).eq("attributes.attributeType", EAttributeType.ACTOR);
    }

    if (StringUtils.isEmpty(listFrom.director) == false) {
      where.eq("attributes.value", listFrom.director).eq("attributes.attributeType", EAttributeType.DIRECTOR);
    }

    if (StringUtils.isEmpty(listFrom.userName) == false) {
      where.eq("owner.userName", listFrom.userName);

      if (listFrom.lendDvd == true) {
        where.isNotNull("borrowDate");
      }
    }

    return Dvd.getByDefaultPaging(where, listFrom.currentPage);
  }

  /**
   * Gets a dvd by a username and the id this should be used for deleting and
   * editing where only the owner can do this
   * 
   * @param id
   * @param username
   * @return
   */
  public static Dvd getDvdForUser(final Long id, final String username) {
    final Dvd userDvd = Dvd.find.where().eq("owner.userName", username).eq("id", id).findUnique();
    return userDvd;

  }

  /**
   * Get dvds for the given page number
   * 
   * @param pageNr
   * @return
   */
  public static Page<Dvd> getDvds(final Integer pageNr) {
    return Dvd.getByDefaultPaging(Dvd.find.where(), pageNr);
  }

  /**
   * This does all the default paging etc stuff
   * 
   * @param expressionList
   * @return
   */
  private static Page<Dvd> getByDefaultPaging(final ExpressionList<Dvd> expressionList, Integer pageNr) {

    if (pageNr == null) {
      pageNr = 0;
    }

    final Page<Dvd> page = expressionList.orderBy("createdDate desc").fetch("owner", "userName").fetch("borrower", "userName").findPagingList(Dvd.DEFAULT_DVDS_PER_PAGE).getPage(pageNr);
    return page;
  }

  /**
   * Lends the {@link Dvd} to a user or to a freename
   * 
   * @param dvdId
   * @param ownerName
   * @param userName
   * @param freeName
   */
  public static void lendDvdToUser(final Long dvdId, final String ownerName, final String userName, final String freeName) {
    final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, ownerName);

    boolean updated = false;

    if (dvdForUser != null) {
      if (StringUtils.isEmpty(userName) == false) {
        final User userByName = User.getUserByName(userName);
        if (userByName != null) {
          dvdForUser.borrower = userByName;
          updated = true;
        }
      }

      if (StringUtils.isEmpty(freeName) == false && updated == false) {
        dvdForUser.borrowerName = freeName;
        updated = true;
      }

      if (updated == true) {
        dvdForUser.borrowDate = new Date().getTime();
        dvdForUser.update();
      }
      return;
    }

    Logger.error("Could not find dvd: " + dvdId + " for owner: " + ownerName);
  }

  /**
   * List all dvds selected distinct by title
   * 
   * @return
   */
  public static List<Dvd> listByDistinctTitle() {
    return Dvd.find.setDistinct(true).select("id,title").order("title asc").findList();

  }
}
