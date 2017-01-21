package plugins.jobs;

import akka.actor.Scheduler;
import com.google.inject.AbstractModule;
import play.Logger;

/**
 * This is a job Module which loads all classes which are annotated with the
 * {@link AkkaJob} annotation and adds them to the {@link Scheduler}
 * 
 * @author tuxburner
 *
 */
public class JobModule extends AbstractModule {

  public static Logger.ALogger LOGGER = Logger.of(JobModule.class);


  @Override
  protected void configure() {
    bind(JobClassLoader.class).asEagerSingleton();
  }
}
