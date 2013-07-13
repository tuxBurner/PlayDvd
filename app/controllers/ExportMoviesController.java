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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: tuxburner
 * Date: 7/13/13
 * Time: 3:43 PM
 */
public class ExportMoviesController extends Controller {

  @Security.Authenticated(Secured.class)
  public static Result displayExportOptions() {

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
  public static Result exportXbmc()  {
    List<Dvd> dvds = Dvd.getAllCopiesForUserForExport(Secured.getUsername());
    if (CollectionUtils.isEmpty(dvds) == false) {

      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(baos);

        for (final Dvd copy : dvds) {
          final String copyTypeAttribute = DvdAttribute.getCopyTypeAttribute(copy);
          final StringBuilder entryName = new StringBuilder(copy.movie.title);
          if (StringUtils.isEmpty(copy.additionalInfo) == false) {
            entryName.append(" - ");
            entryName.append(copy.additionalInfo);
          }


          ZipEntry zipEntry = new ZipEntry(entryName.toString() +"."+copy.id+"." + copyTypeAttribute.toLowerCase() + ".disc");
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
