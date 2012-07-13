package forms;

import play.data.validation.Constraints.Required;

public class ExternalImageForm {

  @Required
  public String url;

  @Required
  public String imgSize;
}
