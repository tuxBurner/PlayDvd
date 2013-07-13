package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import forms.dvd.DvdForm;
import forms.dvd.DvdSearchFrom;
import forms.dvd.objects.EDvdListOrderBy;
import forms.dvd.objects.EDvdListOrderHow;
import forms.dvd.objects.PrevNextCopies;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Dvd extends Model {

  /**
	 *
	 */
  private static final long serialVersionUID = -8607299241692950618L;

  public static final String HULL_NR_SEARCH = "hull:";

  public static final String EAN_NR_SEARCH = "ean:";

  @Id
  public Long id;

  @ManyToOne
  public User owner;

  @OneToOne
  public User borrower;

  @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "dvds")
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
   * @param dvdForm
   * @param dvd
   * @return
   * @throws Exception
   */
  private static Dvd createOrUpdateFromForm(final DvdForm dvdForm, final Dvd dvd) throws Exception {

    final Movie movie = Movie.finder.byId(dvdForm.movieId);

    if (movie == null) {
      final String message = "No movie by the id: " + dvdForm.movieId + " found.";
      Logger.error(message);
      throw new Exception(message);
    }

    dvd.movie = movie;
    dvd.hullNr = dvdForm.hullNr;
    dvd.eanNr = dvdForm.eanNr;
    dvd.asinNr = dvdForm.asinNr;
    dvd.additionalInfo = dvdForm.additionalInfo;

    if (dvd.id == null) {
      dvd.createdDate = new Date().getTime();
      Dvd.create(dvd);
    } else {
      Ebean.deleteManyToManyAssociations(dvd, "attributes");
      dvd.update();
    }

    Dvd.addSingleAttribute(dvdForm.box, EDvdAttributeType.BOX, dvd);
    Dvd.addSingleAttribute(dvdForm.collection, EDvdAttributeType.COLLECTION, dvd);
    Dvd.addSingleAttribute(dvdForm.ageRating, EDvdAttributeType.RATING, dvd);
    Dvd.addSingleAttribute(dvdForm.copyType, EDvdAttributeType.COPY_TYPE, dvd);
    final Set<DvdAttribute> audioTypes = DvdAttribute.gatherAndAddAttributes(new HashSet<String>(dvdForm.audioTypes), EDvdAttributeType.AUDIO_TYPE);
    dvd.attributes.addAll(audioTypes);

    dvd.update();

    return dvd;
  }

  /**
   * Adds a single Attribute to the dvd
   *
   * @param attrToAdd
   * @param attributeType
   * @param dvd
   */
  // TODO: do we need this when we have the new EBEAN nand check also movie
  // and
  // attributes
  public static void addSingleAttribute(final String attrToAdd, final EDvdAttributeType attributeType, final Dvd dvd) {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<String>();
    attribute.add(attrToAdd);
    final Set<DvdAttribute> dbAttrs = DvdAttribute.gatherAndAddAttributes(attribute, attributeType);
    dvd.attributes.addAll(dbAttrs);
  }

  /**
   * Gets all dvds which have the same owner and the same attribute excluding
   * the given {@link Dvd}
   *
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdByBoxOrCollection(final EDvdAttributeType attrType, final String attrValue, final Dvd dvd) {
    final List<Dvd> findList = Dvd.find.where().eq("attributes.attributeType", attrType).eq("attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne("id", dvd.id).orderBy("movie.year asc").findList();
    return findList;
  }

  /**
   * Gets all dvds which fit into the Filter int the {@link forms.dvd.DvdSearchFrom}
   *
   *
   * @param searchFrom
   * @param itemsPerPage
   * @return
   */
  public static Page<Dvd> getDvdsBySearchForm(final DvdSearchFrom searchFrom, Integer itemsPerPage) {

    final ExpressionList<Dvd> where = buildExpressionFromSearchFrom(searchFrom);


    return Dvd.getByDefaultPaging(where, searchFrom.currentPage, searchFrom.orderBy, searchFrom.orderHow, itemsPerPage);
  }

  /**
   * Creates the {@link ExpressionList} from the given {@link DvdSearchFrom}
  * @param searchFrom
   * @return
   */
  private final static ExpressionList<Dvd> buildExpressionFromSearchFrom(final DvdSearchFrom searchFrom) {
    final ExpressionList<Dvd> where = Dvd.find.where();

    if (StringUtils.isEmpty(searchFrom.searchFor) == false) {

      if (searchFrom.searchFor.startsWith(Dvd.HULL_NR_SEARCH) == true) {
        final String idToSearch = StringUtils.trimToNull(StringUtils.removeStart(searchFrom.searchFor, Dvd.HULL_NR_SEARCH));
        if (idToSearch != null && StringUtils.isNumeric(idToSearch) == true) {
          where.eq("hullNr", idToSearch);
        }

      } else if (searchFrom.searchFor.startsWith(Dvd.EAN_NR_SEARCH) == true) {
        final String idToSearch = StringUtils.trimToNull(StringUtils.removeStart(searchFrom.searchFor, Dvd.EAN_NR_SEARCH));
        if (idToSearch != null && StringUtils.isNumeric(idToSearch) == true) {
          where.eq("eanNr", idToSearch);
        }

      } else {
        where.like("movie.title", "%" + searchFrom.searchFor + "%");
      }
    }

    if (StringUtils.isEmpty(searchFrom.genre) == false) {
      where.eq("movie.attributes.value", searchFrom.genre).eq("movie.attributes.attributeType", EMovieAttributeType.GENRE);
    }

    if (StringUtils.isEmpty(searchFrom.actor) == false) {
      where.eq("movie.attributes.value", searchFrom.actor).eq("movie.attributes.attributeType", EMovieAttributeType.ACTOR);
    }

    if (StringUtils.isEmpty(searchFrom.director) == false) {
      where.eq("movie.attributes.value", searchFrom.director).eq("movie.attributes.attributeType", EMovieAttributeType.DIRECTOR);
    }

    if (StringUtils.isEmpty(searchFrom.ageRating) == false) {
      where.eq("attributes.value", searchFrom.ageRating).eq("attributes.attributeType", EDvdAttributeType.RATING);
    }

    if (StringUtils.isEmpty(searchFrom.copyType) == false) {
      where.eq("attributes.value", searchFrom.copyType).eq("attributes.attributeType", EDvdAttributeType.COPY_TYPE);
    }

    if (StringUtils.isEmpty(searchFrom.userName) == false) {
      where.eq("owner.userName", searchFrom.userName);

      if (searchFrom.lendDvd == true) {
        where.isNotNull("borrowDate");
      }
    }

    if (searchFrom.moviesToReview == true) {
      where.eq("movie.hasToBeReviewed", true);
    }

    return where;
  }


  /**
   * Retrieves the {@link Dvd} is below or before the current {@link Dvd}
   *
   * @param dvd
   * @param searchFrom
   * @return
   */
  public static PrevNextCopies getNextAndPrev(final Dvd dvd, final DvdSearchFrom searchFrom) {

    final EDvdListOrderHow orderHow = searchFrom.orderHow;
    final EDvdListOrderBy orderBy = searchFrom.orderBy;


    Object orderDvdVal = null;

    switch(orderBy) {
      case DATE:
        orderDvdVal = dvd.createdDate;
        break;
      case MOVIE_TITLE:
        orderDvdVal = dvd.movie.title;
        break;
    }

    if(orderDvdVal == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("The value for ordering is null");
      }
      return new PrevNextCopies(null,null);
    }

    // now lets make the order for the prev
    final Dvd prevCopy = getPrev(searchFrom,orderDvdVal);
    final Dvd nextCopy = getNext(searchFrom,orderDvdVal);

    return new PrevNextCopies(prevCopy,nextCopy);
  }

  /**
   * Gets the previous {@link Dvd} according to the current {@link DvdSearchFrom}
   * @param searchFrom
   * @param orderDvdVal
   * @return
   */
  private static  Dvd getPrev(final DvdSearchFrom searchFrom, final Object orderDvdVal) {
    final EDvdListOrderBy orderBy = searchFrom.orderBy;
    final EDvdListOrderHow orderHow = searchFrom.orderHow;

    final ExpressionList<Dvd> prev = buildExpressionFromSearchFrom(searchFrom);

    // decide which to take
    if(EDvdListOrderHow.UP.equals(orderHow)) {
      prev.lt(orderBy.dbField, orderDvdVal);
    } else {
      prev.gt(orderBy.dbField, orderDvdVal);
    }

    final PagingList<Dvd> prevPagingList = prev.orderBy(orderBy.dbField + " " + orderHow.dbOrder).findPagingList(1);
    final int totalRowCount = prevPagingList.getTotalRowCount();
    Dvd prevCopy = null;
    if(totalRowCount > 0) {
      final Page<Dvd> prevPage = prevPagingList.getPage(totalRowCount-1);
      final List<Dvd> prevList = prevPage.getList();
      prevCopy = (prevList.size() == 1) ? prevList.get(0) : null;
    }

    return prevCopy;
  }

  /**
   * Gets the next {@link Dvd} according to the current {@link DvdSearchFrom}
   * @param searchFrom
   * @param orderDvdVal
   * @return
   */
  private static Dvd getNext(final DvdSearchFrom searchFrom, final Object orderDvdVal) {

    final EDvdListOrderBy orderBy = searchFrom.orderBy;
    final EDvdListOrderHow orderHow = searchFrom.orderHow;
    final ExpressionList<Dvd> next = buildExpressionFromSearchFrom(searchFrom);

    // decide which to take
    if(EDvdListOrderHow.UP.equals(orderHow)) {
      next.gt(orderBy.dbField, orderDvdVal);
    } else {
      next.lt(orderBy.dbField, orderDvdVal);
    }

    final Page<Dvd> nextPage = next.orderBy(orderBy.dbField + " " + orderHow.dbOrder).findPagingList(1).getPage(0);
    final List<Dvd> nextList = nextPage.getList();
    final Dvd nextCopy = (nextList.size() == 1) ? nextList.get(0) : null;

    return nextCopy;
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
    return getDvdForUser(id,username,false);
  }

    /**
     * Gets a dvd by a username and the id this should be used for deleting and
     * editing where only the owner can do this
     *
     * @param id the id of the {@link Dvd}
     * @param username the name of the {@link User} owning the dvd
     * @param fetchBorrower if true the {@link Dvd#borrower} information will be fetched to
     * @return
     */
  public static Dvd getDvdForUser(final Long id, final String username, final boolean fetchBorrower) {
      Query<Dvd> dvdQuery = Dvd.find.fetch("movie");
      if(fetchBorrower == true) {
          dvdQuery.fetch("borrower");
      }

      final Dvd  userDvd = dvdQuery.where().ieq("owner.userName", username).eq("id", id).findUnique();
      return userDvd;
  }

  /**
   * Gets all {@link Dvd}s for the user
   *
   * @param username
   * @return
   */
  public static List<Dvd> getAllCopiesForUserForExport(final String username) {
    return Dvd.find.fetch("movie","title").where().ieq("owner.userName", username).findList();
  }

  /**
   * Gets dvds which are in the same hull but not the dvd itself
   *
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdUnBorrowedSameHull(final Dvd dvd) {

    // dont bother the database
    if (dvd.hullNr == null) {
      return null;
    }

    final List<Dvd> findList = Dvd.find.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNull("borrowDate").orderBy("movie.title").findList();

    if (CollectionUtils.isEmpty(findList) == true) {
      return null;
    }

    return findList;
  }


  /**
   * Gets all {@link Dvd} which are in the same hull and are borrowed to the same borrower
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdBorrowedSameHull(final Dvd dvd) {

    // dont bother the database
    if (dvd.hullNr == null) {
      return null;
    }

    final ExpressionList<Dvd> notNull = Dvd.find.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNotNull("borrowDate");

    Dvd.borrowerOrBorrowed(dvd, notNull);

    final List<Dvd> findList = notNull.orderBy("movie.title").findList();

    if (CollectionUtils.isEmpty(findList) == true) {
      return null;
    }

    return findList;
  }

  /**
   * Attaches borrower as user or as simple string to the query where
   * @param dvd
   * @param notNull
   */
  private static void borrowerOrBorrowed(final Dvd dvd, final ExpressionList<Dvd> notNull) {
    if (StringUtils.isEmpty(dvd.borrowerName) == false) {
      notNull.eq("borrowerName", dvd.borrowerName);
    } else {
      notNull.eq("borrower", dvd.borrower);
    }
  }

  /**
   * Gets all {@link Dvd}s the current {@link User} lent to somebody
   * @return
   */
  public static Map<String,List<Dvd>> getLentDvds() {
    final User currentUser = User.getCurrentUser();

    final Map<String,List<Dvd>> result = new TreeMap<String, List<Dvd>>();
    final List<Dvd> list = find.where().eq("owner", currentUser).isNotNull("borrowDate").order("borrowDate ASC").findList();
    if(CollectionUtils.isEmpty(list) == false) {
      for(final Dvd dvd : list) {
        final String borrower = (dvd.borrower != null) ? dvd.borrower.userName : dvd.borrowerName;
        if(result.containsKey(borrower) == false) {
          result.put(borrower,new ArrayList<Dvd>());
        }
        result.get(borrower).add(dvd);
      }
    }
    return result;
  }

  /**
   * Counts how many {@link Dvd}s the current {@link User} lent to somebody
   * @return
   */
  public static int getLentDvdsCount()  {
    final User currentUser = User.getCurrentUser();
    return find.where().eq("owner", currentUser).isNotNull("borrowDate").order("borrowDate ASC").findRowCount();
  }

  /**
   * Gets  all {@link Dvd}s the current {@link User} borrowed from an other {@link User}
   * @return
   */
  public static  List<Dvd> getBorrowedDvds() {
    final User currentUser = User.getCurrentUser();
    return find.where().isNotNull("borrower").eq("borrower", currentUser).order("borrowDate ASC").findList();
  }

  /**
   * Counts how many {@link Dvd}s the current {@link User} borrowed from an other {@link User}
   * @return
   */
  public static int getBorrowedDvdsCount() {
    final User currentUser = User.getCurrentUser();
    return find.where().isNotNull("borrower").eq("borrower", currentUser).order("borrowDate ASC").findRowCount();
  }

  /**
   * This does all the defaultold paging etc stuff
   *
   * @param expressionList
   * @param orderHow
   * @param orderBy
   * @param  itemsPerPage
   * @return
   */
  private static Page<Dvd> getByDefaultPaging(final ExpressionList<Dvd> expressionList, Integer pageNr, final EDvdListOrderBy orderBy, final EDvdListOrderHow orderHow, final Integer itemsPerPage) {

    if (pageNr == null) {
      pageNr = 0;
    }

    final Page<Dvd> page = expressionList.orderBy(orderBy.dbField + " " + orderHow.dbOrder).fetch("owner", "userName").fetch("borrower", "userName").fetch("movie").findPagingList(
        itemsPerPage).getPage(pageNr);

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
  public static void lendDvdToUser(final Long dvdId, final String ownerName, final String userName, final String freeName, final Boolean alsoOthersInHull) {
    final Dvd dvdToLend = Dvd.getDvdForUser(dvdId, ownerName);

    if (dvdToLend != null) {

      final Set<Dvd> dvdsToLend = new HashSet<Dvd>();
      dvdsToLend.add(dvdToLend);

      // user also wants to lend dvds in the same hull
      if (alsoOthersInHull == true && dvdToLend.hullNr != null) {
        final Set<Dvd> findSet = Dvd.find.where().eq("hullNr", dvdToLend.hullNr).ne("id", dvdToLend.id).isNull("borrowDate").eq("owner", dvdToLend.owner).findSet();
        dvdsToLend.addAll(findSet);
      }

      User userByName = null;
      if (StringUtils.isEmpty(userName) == false) {
        userByName = User.getUserByName(userName);
      }

      for (final Dvd dvd : dvdsToLend) {

        boolean updated = false;

        if (userByName != null) {
          if (userByName != null) {
            dvd.borrower = userByName;
            updated = true;
          }
        }

        if (StringUtils.isEmpty(freeName) == false && updated == false) {
          dvd.borrowerName = freeName;
          updated = true;
        }

        if (updated == true) {
          dvd.borrowDate = new Date().getTime();
          dvd.update();
        }
      }
      return;
    }

    Logger.error("Could not find dvd: " + dvdId + " for owner: " + ownerName);
  }

  /**
   * Unlents the {@link Dvd} from a {@link User} or from a freename
   *
   * @param dvdId
   * @param ownerName
   * @param alsoOthersInHull
   * @return
   */
  public static Set<Long> unlendDvdToUser(final Long dvdId, final String ownerName, final Boolean alsoOthersInHull) {
    final Dvd dvdToUnlend = Dvd.getDvdForUser(dvdId, ownerName);

    final Set<Long> unlendIds = new HashSet<Long>();

    if (dvdToUnlend != null) {

      final Set<Dvd> dvdsToUnlend = new HashSet<Dvd>();
      dvdsToUnlend.add(dvdToUnlend);

      // user also wants to lend dvds in the same hull
      if (alsoOthersInHull == true && dvdToUnlend.hullNr != null) {

        final ExpressionList<Dvd> expression = Dvd.find.where().eq("hullNr", dvdToUnlend.hullNr).ne("id", dvdToUnlend.id).eq("owner", dvdToUnlend.owner).isNotNull("borrowDate");
        Dvd.borrowerOrBorrowed(dvdToUnlend, expression);

        dvdsToUnlend.addAll(expression.findSet());
      }

      for (final Dvd dvd : dvdsToUnlend) {

        unlendIds.add(dvd.id);

        if(Logger.isDebugEnabled()) {
          Logger.debug("Unlending dvd: " + dvdId);
        }

        dvd.borrowDate = null;
        dvd.borrower = null;
        dvd.borrowerName = null;
        dvd.update();
      }

    }

    Logger.error("Could not find dvd: " + dvdId + " for owner: " + ownerName);

    return unlendIds;
  }

  /**
   * Gets a {@link Dvd} for a user which is not the owner and where the {@link Dvd#borrowDate} is null
   * @param dvdId
   * @return
   */
  public static Dvd getDvdToBorrow(final Long dvdId, final String userName) {
     if(StringUtils.isEmpty(userName) == true || dvdId == null) {
       return null;
     }

    return Dvd.find.where().idEq(dvdId).ne("owner.userName",userName).findUnique();
  }

  /**
   * Gets all dvds which the owner of th dvd has of the same movie series
   *
   * @param attrValue
   * @param dvd
   * @return
   */
  public static List<Dvd> getbyMovieSeries(final String attrValue, final Dvd dvd) {

    final List<Dvd> findList = Dvd.find.where().eq("movie.attributes.attributeType", EMovieAttributeType.MOVIE_SERIES).eq("movie.attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne(
        "id",
        dvd.id).findList();

    return findList;
  }

}
