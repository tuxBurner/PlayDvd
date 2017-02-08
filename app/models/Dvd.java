package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import forms.dvd.CopyForm;
import forms.dvd.CopySearchFrom;
import forms.dvd.objects.EDvdListOrderBy;
import forms.dvd.objects.EDvdListOrderHow;
import forms.dvd.objects.PrevNextCopies;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Constraints.Required;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "dvd")
public class Dvd extends Model {


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

  /**
   * The FINDER for the database for searching in the database
   */
  public static Find<Long, Dvd> FINDER = new Find<Long, Dvd>() {
  };

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
   * Creates a dvd from a {@link CopyForm )
   *
   * @param userName
   * @param dvdForm
   * @return
   * @throws Exception
   */
  public static Dvd createFromForm(final String userName, final CopyForm copyForm) throws Exception {

    final User owner = User.getUserByName(userName);
    if (owner == null) {
      throw new Exception("User does not exist in the Database");
    }

    final Dvd dvd = new Dvd();
    dvd.owner = owner;

    return Dvd.createOrUpdateFromForm(copyForm, dvd);
  }

  /**
   * Edits a dvd from the informations from the form
   *
   * @param userName
   * @param copyForm
   * @throws Exception
   */
  public static Dvd editFromForm(final String userName, final CopyForm copyForm) throws Exception {
    final Dvd dvdForUser = Dvd.getDvdForUser(copyForm.dvdId, userName);
    if (dvdForUser == null) {
      throw new Exception("The dvd could not been edited !");
    }

    return Dvd.createOrUpdateFromForm(copyForm, dvdForUser);
  }

  /**
   * Creates or updates the given dvd
   *
   * @param copyForm
   * @param copy
   * @return
   * @throws Exception
   */
  private static Dvd createOrUpdateFromForm(final CopyForm copyForm, final Dvd copy) throws Exception {

    final Movie movie = Movie.FINDER.byId(copyForm.movieId);

    if (movie == null) {
      final String message = "No movie by the id: " + copyForm.movieId + " found.";
      Logger.error(message);
      throw new Exception(message);
    }

    copy.movie = movie;
    copy.hullNr = copyForm.hullNr;
    copy.eanNr = copyForm.eanNr;
    copy.asinNr = copyForm.asinNr;
    copy.additionalInfo = copyForm.additionalInfo;

    if (copy.id == null) {
      copy.createdDate = new Date().getTime();
      Dvd.create(copy);
    } else {
      Ebean.deleteManyToManyAssociations(copy, "attributes");
      copy.update();
    }

    Dvd.addSingleAttribute(copyForm.box, EDvdAttributeType.BOX, copy);
    Dvd.addSingleAttribute(copyForm.collection, EDvdAttributeType.COLLECTION, copy);
    Dvd.addSingleAttribute(copyForm.ageRating, EDvdAttributeType.RATING, copy);
    Dvd.addSingleAttribute(copyForm.copyType, EDvdAttributeType.COPY_TYPE, copy);
    final Set<DvdAttribute> audioTypes = DvdAttribute.gatherAndAddAttributes(new HashSet<String>(copyForm.audioTypes), EDvdAttributeType.AUDIO_TYPE);
    copy.attributes.addAll(audioTypes);

    copy.update();

    return copy;
  }

  /**
   * Adds a single Attribute to the dvd
   *
   * @param attrToAdd
   * @param attributeType
   * @param copy
   */
  // TODO: do we need this when we have the new EBEAN nand check also movie
  // and
  // attributes
  public static void addSingleAttribute(final String attrToAdd, final EDvdAttributeType attributeType, final Dvd copy) {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<>();
    attribute.add(attrToAdd);
    final Set<DvdAttribute> dbAttrs = DvdAttribute.gatherAndAddAttributes(attribute, attributeType);
    if (copy.attributes.isEmpty() == true) {
      copy.attributes = new HashSet<>();
    }
    copy.attributes.addAll(dbAttrs);
  }

  /**
   * Gets all dvds which have the same owner and the same attribute excluding
   * the given {@link Dvd}
   *
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdByBoxOrCollection(final EDvdAttributeType attrType, final String attrValue, final Dvd dvd) {
    final List<Dvd> findList = Dvd.FINDER.where().eq("attributes.attributeType", attrType).eq("attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne("id", dvd.id).orderBy("movie.year asc").findList();
    return findList;
  }

  /**
   * Gets all dvds which fit into the Filter int the {@link CopySearchFrom}
   *
   * @param searchFrom
   * @param itemsPerPage
   * @return
   */
  public static PagedList<Dvd> getDvdsBySearchForm(final CopySearchFrom searchFrom, Integer itemsPerPage) {

    final ExpressionList<Dvd> where = buildExpressionFromSearchFrom(searchFrom);
    return Dvd.getByDefaultPaging(where, searchFrom.currentPage, searchFrom.orderBy, searchFrom.orderHow, itemsPerPage);
  }

  /**
   * Creates the {@link ExpressionList} from the given {@link CopySearchFrom}
   *
   * @param searchFrom
   * @return
   */
  private static ExpressionList<Dvd> buildExpressionFromSearchFrom(final CopySearchFrom searchFrom) {
    final ExpressionList<Dvd> where = Dvd.FINDER.where();

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
  public static PrevNextCopies getNextAndPrev(final Dvd dvd, final CopySearchFrom searchFrom) {

    final EDvdListOrderBy orderBy = searchFrom.orderBy;

    Object orderDvdVal = null;

    switch (orderBy) {
      case DATE:
        orderDvdVal = dvd.createdDate;
        break;
      case MOVIE_TITLE:
        orderDvdVal = dvd.movie.title;
        break;
    }

    if (orderDvdVal == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("The value for ordering is null");
      }
      return new PrevNextCopies(null, null);
    }

    // now lets make the order for the prev
    final Dvd prevCopy = getPrev(searchFrom, orderDvdVal);
    final Dvd nextCopy = getNext(searchFrom, orderDvdVal);

    return new PrevNextCopies(prevCopy, nextCopy);
  }

  /**
   * Gets the previous {@link Dvd} according to the current {@link CopySearchFrom}
   *
   * @param searchFrom
   * @param orderDvdVal
   * @return
   */
  private static Dvd getPrev(final CopySearchFrom searchFrom, final Object orderDvdVal) {
    final EDvdListOrderBy orderBy = searchFrom.orderBy;
    final EDvdListOrderHow orderHow = searchFrom.orderHow;

    final ExpressionList<Dvd> prev = buildExpressionFromSearchFrom(searchFrom);

    // decide which to take
    if (EDvdListOrderHow.UP.equals(orderHow)) {
      prev.lt(orderBy.dbField, orderDvdVal);
    } else {
      prev.gt(orderBy.dbField, orderDvdVal);
    }

    Query<Dvd> dvdQuery = prev.orderBy(orderBy.dbField + " " + orderHow.dbOrder);


    final int totalRowCount = dvdQuery.findRowCount();
    Dvd prevCopy = null;
    if (totalRowCount > 0) {
      PagedList<Dvd> pagedList = dvdQuery.findPagedList(totalRowCount - 1, 1);
      final List<Dvd> prevList = pagedList.getList();
      prevCopy = (prevList.size() == 1) ? prevList.get(0) : null;
    }

    return prevCopy;
  }

  /**
   * Gets the next {@link Dvd} according to the current {@link CopySearchFrom}
   *
   * @param searchFrom
   * @param orderDvdVal
   * @return
   */
  private static Dvd getNext(final CopySearchFrom searchFrom, final Object orderDvdVal) {

    final EDvdListOrderBy orderBy = searchFrom.orderBy;
    final EDvdListOrderHow orderHow = searchFrom.orderHow;
    final ExpressionList<Dvd> next = buildExpressionFromSearchFrom(searchFrom);

    // decide which to take
    if (EDvdListOrderHow.UP.equals(orderHow)) {
      next.gt(orderBy.dbField, orderDvdVal);
    } else {
      next.lt(orderBy.dbField, orderDvdVal);
    }

    final PagedList<Dvd> nextPage = next.orderBy(orderBy.dbField + " " + orderHow.dbOrder).findPagedList(0, 1);
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
    return getDvdForUser(id, username, false);
  }

  /**
   * Gets a dvd by a username and the id this should be used for deleting and
   * editing where only the owner can do this
   *
   * @param id            the id of the {@link Dvd}
   * @param username      the name of the {@link User} owning the dvd
   * @param fetchBorrower if true the {@link Dvd#borrower} information will be fetched to
   * @return
   */
  public static Dvd getDvdForUser(final Long id, final String username, final boolean fetchBorrower) {
    Query<Dvd> dvdQuery = Dvd.FINDER.fetch("movie");
    if (fetchBorrower == true) {
      dvdQuery.fetch("borrower");
    }

    final Dvd userDvd = dvdQuery.where().ieq("owner.userName", username).eq("id", id).findUnique();
    return userDvd;
  }

  /**
   * Gets all {@link Dvd}s for the user
   *
   * @param username
   * @return
   */
  public static List<Dvd> getAllCopiesForUserForExport(final String username) {
    return Dvd.FINDER.fetch("movie", "title").where().ieq("owner.userName", username).findList();
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

    final List<Dvd> findList = Dvd.FINDER.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNull("borrowDate").orderBy("movie.title").findList();

    if (CollectionUtils.isEmpty(findList) == true) {
      return null;
    }

    return findList;
  }


  /**
   * Gets all {@link Dvd} which are in the same hull and are borrowed to the same borrower
   *
   * @param dvd
   * @return
   */
  public static List<Dvd> getDvdBorrowedSameHull(final Dvd dvd) {

    // dont bother the database
    if (dvd.hullNr == null) {
      return null;
    }

    final ExpressionList<Dvd> notNull = Dvd.FINDER.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNotNull("borrowDate");

    Dvd.borrowerOrBorrowed(dvd, notNull);

    final List<Dvd> findList = notNull.orderBy("movie.title").findList();

    if (CollectionUtils.isEmpty(findList) == true) {
      return null;
    }

    return findList;
  }

  /**
   * Attaches borrower as user or as simple string to the query where
   *
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
   *
   * @return
   */
  public static Map<String, List<Dvd>> getLentDvds() {
    final User currentUser = User.getCurrentUser();

    final Map<String, List<Dvd>> result = new TreeMap<String, List<Dvd>>();
    final List<Dvd> list = FINDER.where().eq("owner", currentUser).isNotNull("borrowDate").order("borrowDate ASC").findList();
    if (CollectionUtils.isEmpty(list) == false) {
      for (final Dvd dvd : list) {
        final String borrower = (dvd.borrower != null) ? dvd.borrower.userName : dvd.borrowerName;
        if (result.containsKey(borrower) == false) {
          result.put(borrower, new ArrayList<Dvd>());
        }
        result.get(borrower).add(dvd);
      }
    }
    return result;
  }

  /**
   * Counts how many {@link Dvd}s the current {@link User} lent to somebody
   *
   * @return
   */
  public static int getLentDvdsCount() {
    final User currentUser = User.getCurrentUser();
    return FINDER.where().eq("owner", currentUser).isNotNull("borrowDate").order("borrowDate ASC").findRowCount();
  }

  /**
   * Gets  all {@link Dvd}s the current {@link User} borrowed from an other {@link User}
   *
   * @return
   */
  public static List<Dvd> getBorrowedDvds() {
    final User currentUser = User.getCurrentUser();
    return FINDER.where().isNotNull("borrower").eq("borrower", currentUser).order("borrowDate ASC").findList();
  }

  /**
   * Counts how many {@link Dvd}s the current {@link User} borrowed from an other {@link User}
   *
   * @return
   */
  public static int getBorrowedDvdsCount() {
    final User currentUser = User.getCurrentUser();
    return FINDER.where().isNotNull("borrower").eq("borrower", currentUser).order("borrowDate ASC").findRowCount();
  }

  /**
   * This does all the defaultold paging etc stuff
   *
   * @param expressionList
   * @param orderHow
   * @param orderBy
   * @param itemsPerPage
   * @return
   */
  private static PagedList<Dvd> getByDefaultPaging(final ExpressionList<Dvd> expressionList, Integer pageNr, final EDvdListOrderBy orderBy, final EDvdListOrderHow orderHow, final Integer itemsPerPage) {

    if (pageNr == null) {
      pageNr = 0;
    }

    PagedList<Dvd> pagedList = expressionList.orderBy(orderBy.dbField + " " + orderHow.dbOrder)
        .fetch("owner", "userName")
        .fetch("borrower", "userName")
        .fetch("movie", "*")
        .findPagedList(pageNr, itemsPerPage);

    return pagedList;
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
        final Set<Dvd> findSet = Dvd.FINDER.where().eq("hullNr", dvdToLend.hullNr).ne("id", dvdToLend.id).isNull("borrowDate").eq("owner", dvdToLend.owner).findSet();
        dvdsToLend.addAll(findSet);
      }

      User userByName = null;
      if (StringUtils.isEmpty(userName) == false) {
        userByName = User.getUserByName(userName);
      }

      for (final Dvd dvd : dvdsToLend) {

        boolean updated = false;

        if (userByName != null) {

          dvd.borrower = userByName;
          updated = true;

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

    Logger.error("Could not FINDER dvd: " + dvdId + " for owner: " + ownerName);
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

        final ExpressionList<Dvd> expression = Dvd.FINDER.where().eq("hullNr", dvdToUnlend.hullNr).ne("id", dvdToUnlend.id).eq("owner", dvdToUnlend.owner).isNotNull("borrowDate");
        Dvd.borrowerOrBorrowed(dvdToUnlend, expression);

        dvdsToUnlend.addAll(expression.findSet());
      }

      for (final Dvd dvd : dvdsToUnlend) {

        unlendIds.add(dvd.id);

        if (Logger.isDebugEnabled()) {
          Logger.debug("Unlending dvd: " + dvdId);
        }

        dvd.borrowDate = null;
        dvd.borrower = null;
        dvd.borrowerName = null;
        dvd.update();
      }

    }

    Logger.error("Could not FINDER dvd: " + dvdId + " for owner: " + ownerName);

    return unlendIds;
  }

  /**
   * Gets a {@link Dvd} for a user which is not the owner and where the {@link Dvd#borrowDate} is null
   *
   * @param dvdId
   * @return
   */
  public static Dvd getDvdToBorrow(final Long dvdId, final String userName) {
    if (StringUtils.isEmpty(userName) == true || dvdId == null) {
      return null;
    }

    return Dvd.FINDER.where().idEq(dvdId).ne("owner.userName", userName).findUnique();
  }

  /**
   * Gets all dvds which the owner of th dvd has of the same movie series
   *
   * @param attrValue
   * @param dvd
   * @return
   */
  public static List<Dvd> getbyMovieSeries(final String attrValue, final Dvd dvd) {

    final List<Dvd> findList = Dvd.FINDER.where().eq("movie.attributes.attributeType", EMovieAttributeType.MOVIE_SERIES).eq("movie.attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne(
        "id",
        dvd.id).findList();

    return findList;
  }

}
