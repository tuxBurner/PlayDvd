package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

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

  @ManyToOne
  public User owner;

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
   * The movie which is on the dvd
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @Column(nullable = false)
  public Movie movie;

  /**
   * The finder for the database for searching in the database
   */
  public static Finder<Long, Dvd> find = new Finder<Long, Dvd>(Long.class, Dvd.class);

  @Required
  @Column(nullable = false)
  public Long createdDate;

  public String box;

  public String collection;

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

    // create or add the movie to the database by the information from the form
    final Movie movie = Movie.editOrAddFromForm(dvdForm, dvd.movie);

    dvd.movie = movie;
    dvd.hullNr = dvdForm.hullNr;

    dvd.box = dvdForm.box;
    dvd.collection = dvdForm.collection;

    if (dvd.id == null) {
      dvd.createdDate = new Date().getTime();
      Dvd.create(dvd);
    } else {
      dvd.update();
    }

    return dvd;
  }

  /**
   * Gets the distinct name of all boxes and collections in the database to the
   * dvd
   * 
   * @param box
   * @return
   */
  public static List<String> getUserBoxesOrCollections(final boolean box) {

    final String boxOrCollectionAttr = Dvd.boxOrCollectionAttr(box);

    final List<Dvd> findList = Dvd.find.setDistinct(true).select(boxOrCollectionAttr).orderBy(boxOrCollectionAttr + " asc").findList();

    // final RawSql rawsql = RawSqlBuilder.parse("").create();
    // Dvd.find.setRawSql(parse);

    // TODO: do we need this transformation or can we make the select make this
    final List<String> returnVal = new ArrayList<String>();
    returnVal.add("");
    for (final Dvd dvd : findList) {
      if (box) {
        returnVal.add(dvd.box);
      } else {
        returnVal.add(dvd.collection);
      }
    }

    return returnVal;
  }

  /**
   * Gets all dvds which have the same owner and the same attribute excluding
   * the given {@link Dvd}
   * 
   * @param box
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdByBoxOrCollection(final boolean box, final String attrValue, final Dvd dvd) {

    final String attrName = Dvd.boxOrCollectionAttr(box);

    final List<Dvd> findList = Dvd.find.where().eq(attrName, attrValue).eq("owner.id", dvd.owner.id).ne("id", dvd.id).findList();

    return findList;
  }

  private static String boxOrCollectionAttr(final boolean box) {
    final String attrName = (box == true) ? "box" : "collection";
    return attrName;
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
      where.like("movie.title", "%" + listFrom.searchFor + "%");
    }

    if (StringUtils.isEmpty(listFrom.genre) == false) {
      where.eq("movie.attributes.value", listFrom.genre).eq("movie.attributes.attributeType", EAttributeType.GENRE);
    }

    if (StringUtils.isEmpty(listFrom.actor) == false) {
      where.eq("movie.attributes.value", listFrom.actor).eq("movie.attributes.attributeType", EAttributeType.ACTOR);
    }

    if (StringUtils.isEmpty(listFrom.director) == false) {
      where.eq("movie.attributes.value", listFrom.director).eq("movie.attributes.attributeType", EAttributeType.DIRECTOR);
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
    final Dvd userDvd = Dvd.find.fetch("movie").where().eq("owner.userName", username).eq("id", id).findUnique();
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

    final Page<Dvd> page = expressionList.orderBy("createdDate desc").fetch("owner", "userName").fetch("borrower", "userName").fetch("movie").findPagingList(Dvd.DEFAULT_DVDS_PER_PAGE).getPage(pageNr);
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

}
