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

  public String reservation;

  public Boolean alsoOthersInHull = false;

}
