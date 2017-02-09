package controllers;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * User: tuxburner
 * Date: 6/9/13
 * Time: 10:58 PM
 */
public class RssSecurityAction extends play.mvc.Action.Simple {

    public static String RSS_FEED_AUTH_PARAM = "authKey";

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        if (Logger.isDebugEnabled() == true) {
            Logger.debug("Somebody is calling a Rss Feed checking it if allowed to.");
        }

        final String rssAuthKey = ctx.request().getQueryString(RSS_FEED_AUTH_PARAM);
        if (StringUtils.isEmpty(rssAuthKey) == true) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("Could not find parameter: " + RSS_FEED_AUTH_PARAM + " in the query !");
            }


            return CompletableFuture.completedFuture(unauthorized("No auth key supplied"));
        }

        final User userByRssAuthKey = User.getUserByRssAuthKey(rssAuthKey);
        if (userByRssAuthKey == null) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("Could not find user for rssAuthKey: " + rssAuthKey);
            }


            return CompletableFuture.completedFuture(unauthorized("Auth was no success"));
        }

        Http.Context context = new Http.Context(ctx.request().withUsername(userByRssAuthKey.userName));
        return delegate.call(context);
    }
}
