package forms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import controllers.Secured;

import play.mvc.Controller;

import models.User;

public class LendForm {

  public String userName;

  public String freeName;

  public Boolean alsoOthersInHull = false;

  public static List<String> getOtherUsers() {
    final List<User> findList = User.find.select("userName").where().ne("userName", Controller.ctx().session().get(Secured.AUTH_SESSION)).orderBy("userName asc").findList();

    List<String> list = null;
    if (CollectionUtils.isEmpty(findList) == false) {
      list = new ArrayList<String>();
      list.add("");
      for (final User user : findList) {
        list.add(user.userName);
      }
    }

    return list;
  }

}
