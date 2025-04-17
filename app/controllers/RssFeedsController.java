package controllers;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedOutput;
import dao.UserDao;
import helpers.EImageSize;
import helpers.EImageType;
import models.Dvd;
import models.EMovieAttributeType;
import models.MovieAttribute;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: tuxburner
 */
@Singleton
public class RssFeedsController extends Controller {

  private final MessagesApi messagesApi;


  @Inject
  RssFeedsController(final MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  /**
   * Displays the links to the diffrent RSS Feeds
   *
   * @return
   */
  @Security.Authenticated(Secured.class)
  public Result displayRssFeedLinks(final Http.Request request) {

    final User currentUser = UserDao.findCurrentUser(request);
    if (currentUser == null) {
      return unauthorized();
    }

    final Messages messages = this.messagesApi.preferred(request);
    final String rssAuthKey = currentUser.rssAuthKey;
    return ok(views.html.rss.rssFeedsList.render(rssAuthKey, request, messages));
  }

  /**
   * Gets the last 10 {@Dvd}s
   *
   * @return
   */
  @With(RssSecurityAction.class)
  public Result getLastAddedCopies(final Http.Request request) {
    final List<Dvd> copies = Dvd.FINDER.query()
        .orderBy("createdDate DESC")
        .setFirstRow(0)
        .setMaxRows(10)
        .findPagedList()
        .getList();
    return createFeedContentForList(copies, "Last 10 added copies", "The last 10 added copies in the database", request);
  }

  /**
   * Gets the last week added {@Dvd}s
   *
   * @return
   */
  @With(RssSecurityAction.class)
  public Result getLastAddedWeekCopies(final Http.Request request) {
    final Date now = new Date();
    final Calendar cal = Calendar.getInstance();
    cal.setTime(now);
    cal.add(Calendar.DATE, -7);
    final long sqlTime = cal.getTime().getTime();

    final List<Dvd> copies = Dvd.FINDER.query()
        .where()
        .gt("createdDate", sqlTime)
        .orderBy("createdDate DESC")
        .findList();
    return createFeedContentForList(copies, "Last 7 days added copies", "The last 7 days added copies in the database", request);
  }

  /**
   * Streams the poster image of a {@link models.Movie}
   *
   * @param copyId
   * @return
   */
  @With(RssSecurityAction.class)
  public Result getPosterImage(final Long copyId, final Http.Request request) {
    return DashboardController.getStreamImage(copyId, EImageType.POSTER.name(), EImageSize.SMALL.name(), request);
  }


  /**
   * Creates the {@link Result} as AtomFeed
   *
   * @param copies
   * @param feedTitle
   * @param feedSubTitle
   * @return
   */
  private Result createFeedContentForList(final List<Dvd> copies, final String feedTitle, final String feedSubTitle, final Http.Request request) {
    try {
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType("atom_1.0");
      feed.setTitle(feedTitle);
      feed.setLink(routes.ApplicationController.index().absoluteURL(request));
      if (StringUtils.isEmpty(feedSubTitle) == false) {
        feed.setDescription(feedSubTitle);
      }
      feed.setEncoding("utf-8");

      List<SyndEntry> entries = createFeedEntriesFromCopies(copies, request);

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
  private List<SyndEntry> createFeedEntriesFromCopies(final List<Dvd> copies, final Http.Request request) {
    List<SyndEntry> entries = new ArrayList<SyndEntry>();
    if (CollectionUtils.isEmpty(copies) == false) {
      for (final Dvd copy : copies) {
        entries.add(convertCopyToFeedEntry(copy, request));
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
  private SyndEntry convertCopyToFeedEntry(final Dvd copy, final Http.Request request) {
    final SyndEntryImpl entry = new SyndEntryImpl();

    String title = copy.movie.getTitle();
    if (StringUtils.isEmpty(copy.additionalInfo) == false) {
      title += "[" + copy.additionalInfo + "]";
    }

    entry.setTitle(title);
    entry.setLink(routes.DashboardController.displayCopyOnPage(copy.id).absoluteURL(request));
    entry.setPublishedDate(new Date(copy.createdDate));
    entry.setAuthor(copy.owner.userName);


    List<SyndCategory> genres = new ArrayList<SyndCategory>();
    for (MovieAttribute attr : copy.movie.getAttributes()) {
      if (EMovieAttributeType.GENRE.equals(attr.attributeType) == true) {
        SyndCategory cat = new SyndCategoryImpl();
        cat.setName(attr.value);
        genres.add(cat);
      }
    }
    entry.setCategories(genres);

    SyndContent description = new SyndContentImpl();
    description.setType("text/html");
    final Messages messages = this.messagesApi.preferred(request);
    description.setValue(views.html.rss.rssFeedItem.render(copy, request.getQueryString(RssSecurityAction.RSS_FEED_AUTH_PARAM), request, messages).toString());
    entry.setDescription(description);

    return entry;
  }

}
