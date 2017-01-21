package plugins.jobs;

import akka.actor.ActorSystem;
import play.Logger;
import play.libs.Akka;
import play.libs.Time.CronExpression;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAkkaJob implements Runnable {

  /**
   * The Cronexpression of this job.
   */
  private final CronExpression cronExpression;

  /**
   * The play actorsystem to use for the job handling.
   */
  private final ActorSystem actorSystem;

  /**
   * Restart this job when it failed ?
   */
  private boolean restartOnFail = true;

  public AbstractAkkaJob(final ActorSystem actorSystem) throws Exception {

    this.actorSystem = actorSystem;

    final Annotation annotation = this.getClass().getAnnotation(AkkaJob.class);
    final AkkaJob akkaJob = (AkkaJob) annotation;

    final String annoCronExpression = akkaJob.cronExpression().trim();

    final boolean validExpression = CronExpression.isValidExpression(annoCronExpression);
    if (validExpression == false) {
      final String message = "The annotated cronExpression: " + annoCronExpression + " is not a valid CronExpression in class: " + this.getClass().getName();
      JobModule.LOGGER.error(message);
      throw new Exception(message);
    }
    cronExpression = new CronExpression(annoCronExpression);
    scheduleJob();
  }

  /**
   * Schedules the job
   */
  private void scheduleJob() {
    final long nextInterval = cronExpression.getNextInterval(new Date());
    final FiniteDuration duration = Duration.create(nextInterval, TimeUnit.MILLISECONDS);
    actorSystem.scheduler().scheduleOnce(duration,this,actorSystem.dispatcher());
  }

  @Override
  public void run() {
    try {
      // TODO: stopwatching how long the job is running
      runInternal();
    } catch (final Exception e) {
      JobModule.LOGGER.error("An error happend in the internal implementation of the job: " + this.getClass().getCanonicalName(), e);
      if (restartOnFail == false) {
        if (JobModule.LOGGER.isDebugEnabled() == true) {
          JobModule.LOGGER.debug("Will not restart the job: " + this.getClass().getCanonicalName());
        }
        return;
      }
    }

    scheduleJob();
  }

  public void setRestartOnFail(final boolean restartOnFail) {
    this.restartOnFail = restartOnFail;
  }

  /**
   * Here you can implement the actual doing of the job
   */
  public abstract void runInternal();

}
