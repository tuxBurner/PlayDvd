package modules.jobs;

import akka.actor.ActorSystem;
import play.libs.Time.CronExpression;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static modules.jobs.JobModule.LOGGER;

public abstract class AbstractAkkaJob implements Runnable {

  /**
   * The Cron-Expression of this job.
   */
  protected CronExpression cronExpression;

  /**
   * The play actorsystem to use for the job handling.
   */
  private final ActorSystem actorSystem;

  /**
   * The current state of the job
   */
  protected EJobRunState runState;

  /**
   * Restart this job when it failed ?
   */
  private boolean restartOnFail = true;

  public AbstractAkkaJob(final ActorSystem actorSystem) throws JobException {

    runState = EJobRunState.STOPPED;

    this.actorSystem = actorSystem;
  }

  /**
   * Schedules the job
   */
  public void scheduleJob() {

    if(EJobRunState.DISABLED.equals(runState) == true) {
      LOGGER.info("Runstate for the job: "+this.getClass().getName()+" is "+EJobRunState.DISABLED+" not going to run");
      return;
    }

    if(cronExpression == null) {
      LOGGER.error("No Cronexpression set at: "+this.getClass().getName());
      return;
    }

    final long nextInterval = cronExpression.getNextInterval(new Date());
    final FiniteDuration duration = Duration.create(nextInterval, TimeUnit.MILLISECONDS);
    runState = EJobRunState.SCHEDULED;
    actorSystem.scheduler().scheduleOnce(duration,this,actorSystem.dispatcher());
    if(LOGGER.isDebugEnabled() == true) {
      LOGGER.debug(this.getClass().getName()+" Job is running again in: "+duration.toString());
    }
  }

  @Override
  public void run() {

    // check if the state is not running
    if(EJobRunState.RUNNING.equals(runState) == true) {
      LOGGER.warn(this.getClass().getName()+" Job not started because it is still in run mode.");
      scheduleJob();
      return;
    }

    if(LOGGER.isDebugEnabled() == true) {
      LOGGER.debug(this.getClass().getName()+" Job is going to run.");
    }


    try {
      runState = EJobRunState.RUNNING;
      runInternal();
      runState = EJobRunState.STOPPED;
    } catch (final Exception e) {
      LOGGER.error("An error happend in the internal implementation of the job: " + this.getClass().getName(), e);
      runState = EJobRunState.ERROR;
      if (restartOnFail == false) {
        runState = EJobRunState.KILLED;
        if (LOGGER.isDebugEnabled() == true) {
          LOGGER.debug("Will not restart the job: " + this.getClass().getName());
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

  public CronExpression getCronExpression() {
    return cronExpression;
  }

  public EJobRunState getRunState() {
    return runState;
  }

  public boolean isRestartOnFail() {
    return restartOnFail;
  }
}
