package grabbers;

import com.typesafe.config.ConfigFactory;
import helpers.ConfigurationHelper;
import jodd.http.HttpBrowser;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import jodd.net.MimeTypes;
import play.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpBrowserHelper {

  /**
   * User agent to set on the request
   */
  private final static String WEB_BROWSER_USER_AGENT = ConfigFactory.load()
    .getString("dvdb.browser.useragent");

  /**
   * Calls the given url and returns the content as a {@link String}
   *
   * @param url             the url to call
   * @param queryParameters {@link Map} with url query parameters can be null
   * @return the content as a String
   */
  public static String getContentFromUrl(final String url, final Map<String, String> queryParameters) {

    final HttpBrowser browser = new HttpBrowser();
            
    HttpRequest request = HttpRequest.get(url)
      .charset(StandardCharsets.UTF_8.name())
      .contentType(MimeTypes.MIME_TEXT_HTML)
      .connectionKeepAlive(true)
      .followRedirects(true);


    if (queryParameters != null && queryParameters.isEmpty() == false) {
      request = request.query(queryParameters);
    }


    request.header("User-Agent",WEB_BROWSER_USER_AGENT);

    Logger.debug("Calling url: "+url+" ("+  request.toString()+" )");

    final HttpResponse httpResponse = browser.sendRequest(request);
    httpResponse.charset(StandardCharsets.UTF_8.name());


    final String bodyText = httpResponse.bodyText();

    //Logger.debug("Response is: "+bodyText);

    return bodyText;
  }


  /**
   * Calls the given url and returns the content as a {@link Jerry} document
   *
   * @param url             the url to call
   * @param queryParameters {@link Map} with url query parameters can be null
   * @return the content as a {@link Jerry} document
   */
  public static Jerry getUrlAsJerryDoc(final String url, final Map<String, String> queryParameters) {
    final String contentFromUrl = getContentFromUrl(url, queryParameters);
    return Jerry.jerry(contentFromUrl);
  }

  /**
   * Calls the given url and returns the content as a {@link Jerry} document
   *
   * @param url the url to call   *
   * @return the content as a {@link Jerry} document
   */
  public static Jerry getUrlAsJerryDoc(final String url) {
    final String contentFromUrl = getContentFromUrl(url, null);
    return Jerry.jerry(contentFromUrl);
  }

  /**
   * Calls the given url and returns the content as a {@link Jerry} document
   *
   * @param url        the url to call
   * @param queryKey   the query parameter key
   * @param queryValue the query parameter value
   * @return the content as a {@link Jerry} document
   */
  public static Jerry getUrlAsJerryDoc(final String url, final String queryKey, final String queryValue) {
    final Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put(queryKey, queryValue);
    final String contentFromUrl = getContentFromUrl(url, queryParameters);
    return Jerry.jerry(contentFromUrl);
  }

}
