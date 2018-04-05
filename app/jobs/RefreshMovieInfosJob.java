package jobs;

import akka.actor.ActorSystem;
import com.github.tuxBurner.jobs.AbstractConfigurationJob;
import forms.MovieForm;
import forms.grabbers.GrabberInfoForm;
import grabbers.GrabberHelper;
import grabbers.IInfoGrabber;
import models.Movie;
import play.Logger;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This job handles refreshing the movie informations in the database.
 * configuration is under jobs.jobs.RefreshMovieInfosJob
 * <p>
 * Created by tuxburner on 21.01.17.
 */
public class RefreshMovieInfosJob extends AbstractConfigurationJob {

  public RefreshMovieInfosJob(ActorSystem actorSystem) throws Exception {
    super(actorSystem);
  }

  @Override
  public void runInternal() {
    Logger.debug("Job for refreshing movie informations started");

    int movieAmount = getConfiguration().getInt("movieAmount");
    int movieAge = getConfiguration().getInt("movieAge");


    FiniteDuration finiteDuration = Duration.create(movieAge, TimeUnit.DAYS);

    List<Movie> moviesToUpdate = Movie.findMoviesToUpdate(finiteDuration, movieAmount);
    Logger.info("Found: " + moviesToUpdate.size() + " to update the informations for.");

    for (Movie movie : moviesToUpdate) {
      Logger.info("Going to fetch data for movie "+movie.title+" (" + movie.id + ") with grabber: " + movie.grabberType + " (" + movie.grabberId + ")");
      IInfoGrabber grabber = GrabberHelper.getGrabber(movie.grabberType);
      GrabberInfoForm infoForm = new GrabberInfoForm();
      infoForm.grabberMovieId = movie.grabberId;
      infoForm.movieToEditId = movie.id;
      try {
        MovieForm movieForm = grabber.fillInfoToMovieForm(infoForm);
        movieForm.movieId = movie.id;
        Movie.editOrAddFromForm(movieForm, false);
      } catch (Exception e) {
        Logger.error("An error happened while getting movieinformations for movie: "+movie.title+" (" + movie.id + ") with grabber: " + movie.grabberType + " (" + movie.grabberId + ")", e);
        movie.updatedDate = new Date().getTime();
        movie.update();
      }

    }
  }

  /**
   * We want the job to start when the application is starting.
   * @return
   */
  @Override
  public boolean isSubmittedImmediately() {
    return true;
  }
}
