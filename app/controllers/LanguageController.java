package controllers;

import com.typesafe.config.ConfigFactory;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageController extends Controller {


  /**
   * Flag name for the languages
   */
  private static HashMap<String, String> languageFlags;


  /**
   * Names of the languages
   */
  private static HashMap<String, String> languages;

  /**
   * Tries to determine default language based on users accept-languages
   *
   * @return Integer: Number of language allowed in application's conf
   */
  public static Lang getDefaultLang() {
    final List<Lang> availables = Lang.availables();
    List<Lang> accepted = request().acceptLanguages();

    for (Lang testLang : accepted) {
      if (availables.contains(testLang)) {
        return testLang;
      }
    }
    return ctx().lang();
  }


  /**
   * Allows to change users language
   *
   * @param code Language's code
   * @return Redirect to main page
   */
  public static Result changeLanguage(String code) {

    play.api.i18n.Lang lang = Lang.apply(code);

    final List<Lang> availables = Lang.availables();

    if (availables.contains(lang) == false) {
      lang = getDefaultLang();
    }

    ctx().changeLang(lang.code());

    return redirect(routes.Application.index());
  }

  /**
   * Gets the language name for the lang at the position from the configuration
   *
   * @param i
   * @return
   */
  private static String getLanguageName(int i) {
    String[] languageNames = ConfigFactory.load().getString("application.languageNames").split(",");
    String languageName = Lang.availables().get(i).code();
    if (languageNames[i] != null) {
      languageName = languageNames[i];
    }
    return languageName.trim();
  }

  /**
   * Gets the language flag for the lang at the position from the configuration
   *
   * @param i
   * @return
   */
  private static String getLanguageFlagName(int i) {
    String[] languageNames = ConfigFactory.load().getString("application.languageFlags").split(",");
    String languageName = Lang.availables().get(i).code();
    if (languageNames[i] != null) {
      languageName = languageNames[i];
    }
    return languageName.trim();
  }

  /**
   * Gets the language codes and names for the menue
   *
   * @return
   */
  public static Map<String, String> getLanguageFlags() {
    if (languageFlags == null) {
      languageFlags = new HashMap<String, String>();
      final List<Lang> availables = Lang.availables();
      for (int i = 0; i < availables.size(); i++) {
        languageFlags.put(availables.get(i).code(), getLanguageFlagName(i));
      }
    }
    return languageFlags;
  }


  /**
   * Gets the language codes and names for the menue
   *
   * @return
   */
  public static Map<String, String> getLanguages() {
    if (languages == null) {
      languages = new HashMap<String, String>();
      final List<Lang> availables = Lang.availables();
      for (int i = 0; i < availables.size(); i++) {
        languages.put(availables.get(i).code(), getLanguageName(i));
      }
    }
    return languages;
  }

}