package controllers;


import models.Dvd;
import models.DvdAttribute;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: tuxburner
 * Date: 7/13/13
 * Time: 3:43 PM
 */
@Singleton
public class ExportMoviesController extends Controller {

  @Security.Authenticated(Secured.class)
  public Result displayExportOptions() {

    final User currentUser = User.getCurrentUser();

    return ok(views.html.export.export.render(currentUser.rssAuthKey));
  }

  /**
   * Create mediastubs for xbmc and returns them as a zip file
   * http://wiki.xbmc.org/index.php?title=Media_stubs
   *
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @With(RssSecurityAction.class)
  public Result exportXbmc()  {
       List<Dvd> dvds = Dvd.getAllCopiesForUserForExport(request().username());
    if (CollectionUtils.isEmpty(dvds) == false) {

      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(baos, Charset.forName("UTF-8"));


        final Set<String> alreadyAdded = new HashSet<String>();

        for (final Dvd copy : dvds) {
          final String copyTypeAttribute = DvdAttribute.getCopyTypeAttribute(copy);
          final StringBuilder entryName = new StringBuilder(copy.movie.title);
          if (StringUtils.isEmpty(copy.additionalInfo) == false) {
            entryName.append(" - ");
            entryName.append(copy.additionalInfo);
          }
          entryName.append(".");
          entryName.append(copyTypeAttribute.toLowerCase());
          entryName.append(".disc");

          final String entryNameStr = entryName.toString();

          if(alreadyAdded.contains(entryNameStr) == true) {
            continue;
          }

          alreadyAdded.add(entryNameStr);

          ZipEntry zipEntry = new ZipEntry(entryNameStr);


          zipOutputStream.putNextEntry(zipEntry);
          String xbmcStubContent = generateXBMCStubContent(copy);
          IOUtils.write(xbmcStubContent, zipOutputStream, "UTF-8");
          zipOutputStream.closeEntry();
        }


        IOUtils.closeQuietly(zipOutputStream);
        IOUtils.closeQuietly(baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

       response().setContentType("application/zip");
       response().setHeader("Content-Disposition", "attachment; filename=xbmc_stub.zip");



        return ok(bais);
      } catch (Exception e) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("An error happend while creating zip file", e);
        }
        return internalServerError("An error happend while creating zip file: " + e.getMessage());
      }
    }


    return ok("");
  }

  /**
   * Generates the media stub content for the copy
   *
   * @param copy
   * @return
   */
  private static String generateXBMCStubContent(final Dvd copy) {

    final StringBuilder content = new StringBuilder("<discstub>");
    if (copy.hullNr != null) {
      content.append("<message> Please insert Copy with Nr:");
      content.append(copy.hullNr.toString());
      content.append("</message>");
    }
    content.append("</discstub>");

    return content.toString();
  }


}
