package forms;

import play.data.validation.Constraints.Required;

public class ExternalImageForm {

  @Required
  public String url;

  @Required
  public String imgSize;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getImgSize() {
    return imgSize;
  }

  public void setImgSize(String imgSize) {
    this.imgSize = imgSize;
  }
}
