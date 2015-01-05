package controllers;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedOutput;
import helpers.EImageSize;
import helpers.EImageType;
import models.Dvd;
import models.EMovieAttributeType;
import models.MovieAttribute;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import scala.collection.mutable.StringBuilder;

import play.twirl.api.Html;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: tuxburner
 * Date: 6/9/13
 * Time: 10:55 PM
 */
public class RssFeedsController extends Controller {

  /**
   * Displays the links to the diffrent RSS Feeds
   * @return
   */
  @Security.Authenticated(Secured.class)
  public static Result displayRssFeedLinks() {

    final User currentUser = User.getCurrentUser();
    if(currentUser == null) {
      return unauthorized();
    }

    final String rssAuthKey = currentUser.rssAuthKey;
    return ok(views.html.rss.rssFeedsList.render(rssAuthKey));
  }

  /**
   * Gets the last 10 {@Dvd}s
   * @return
   */
  @With(RssSecurityAction.class)
  public static Result getLastAddedCopies() {
    final List<Dvd> copies = Dvd.find.orderBy("createdDate DESC").findPagingList(10).getPage(0).getList();
    return createFeedContentForList(copies,"Last 10 added copies","The last 10 added copies in the database");
  }

  /**
   * Gets the last week added {@Dvd}s
   * @return
   */
  @With(RssSecurityAction.class)
  public static Result getLastAddedWeekCopies() {
    final Date now = new Date();
    final Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.add(Calendar.DATE,-7);
    final long sqlTime = cal.getTime().getTime();

    final List<Dvd> copies = Dvd.find.where().gt("createdDate",sqlTime).orderBy("createdDate DESC").findList();
    return createFeedContentForList(copies,"Last 7 days added copies","The last 7 days added copies in the database");
  }

  /**
   * Streams the poster image of a {@link models.Movie}
   *
   * @param copyId
   * @return
   */
  @With(RssSecurityAction.class)
  public static Result getPosterImage(final Long copyId) {
    return Dashboard.streamImage(copyId, EImageType.POSTER.name(), EImageSize.SMALL.name());
  }


  /**
   * Creates the {@link Result} as AtomFeed
   * @param copies
   * @param feedTitle
   * @param feedSubTitle
   * @return
   */
  private static Result createFeedContentForList(final List<Dvd> copies, final String feedTitle, final String feedSubTitle) {
    try {
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType("atom_1.0");
      feed.setTitle(feedTitle);
      feed.setLink(routes.Application.index().absoluteURL(request()));
      if(StringUtils.isEmpty(feedSubTitle) == false) {
        feed.setDescription(feedSubTitle);
      }
      feed.setEncoding("utf-8");

      List<SyndEntry> entries = createFeedEntriesFromCopies(copies);

      feed.setEntries(entries);

      final Writer writer = new StringWriter();
      SyndFeedOutput output = new SyndFeedOutput();
      output.output(feed, writer);
      writer.close();
      Html html = new Html(writer.toString());
      return ok(html);

    } catch (Exception e) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not create the rss feed.", e);
      }

      return internalServerError("An error happend while creating the feed.");
    }
  }

  /**
   * Creates a {@link List} of {@link SyndEntry}s for the given {@link Dvd}s
   *
   * @param copies
   * @return
   */
  private static List<SyndEntry> createFeedEntriesFromCopies(final List<Dvd> copies) {
    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    if (CollectionUtils.isEmpty(copies) == false) {
      for (final Dvd copy : copies) {
        entries.add(convertCopyToFeedEntry(copy));
      }
    }

    return entries;
  }

  /**
   * Creates a feed entry from the given {@link Dvd}
   *
   * @param copy
   * @return
   */
  private static SyndEntry convertCopyToFeedEntry(final Dvd copy) {
    final SyndEntryImpl entry = new SyndEntryImpl();

    String title = copy.movie.title;
    if (StringUtils.isEmpty(copy.additionalInfo) == false) {
      title += "[" + copy.additionalInfo + "]";
    }

    entry.setTitle(title);
    entry.setLink(routes.Dashboard.displayCopyOnPage(copy.id).absoluteURL(request()));
    entry.setPublishedDate(new Date(copy.createdDate));
    entry.setAuthor(copy.owner.userName);


    List<SyndCategory> genres = new ArrayList<SyndCategory>();
    for (MovieAttribute attr : copy.movie.attributes) {
      if (EMovieAttributeType.GENRE.equals(attr.attributeType) == true) {
        SyndCategory cat = new SyndCategoryImpl();
        cat.setName(attr.value);
        genres.add(cat);
      }
    }
    entry.setCategories(genres);

    SyndContent description = new SyndContentImpl();
    description.setType("text/html");
    description.setValue(views.html.rss.rssFeedItem.render(copy,request().getQueryString(RssSecurityAction.RSS_FEED_AUTH_PARAM)).toString());
    entry.setDescription(description);

    return entry;
  }

}
