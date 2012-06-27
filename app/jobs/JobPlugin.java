package jobs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import play.Application;
import play.Logger;
import play.Plugin;
import play.libs.Akka;
import play.libs.Time.CronExpression;
import akka.actor.Scheduler;
import akka.util.Duration;
import akka.util.FiniteDuration;

/**
 * This is a job Plugin which loads all classes which are annotated with the
 * {@link AkkaJob} annotation and adds them to the {@link Scheduler}
 * 
 * @author tuxburner
 * 
 */
public class JobPlugin extends Plugin {

  private final Application application;

  public JobPlugin(final Application application) {
    this.application = application;
  }

  @Override
  public void onStart() {

    final Set<String> classes = new HashSet<String>();

    // find all classes which are annotated with AkkaJob and which are located
    // in the jobs classpath
    classes.addAll(application.getTypesAnnotatedWith("jobs", AkkaJob.class));

    final Set<Class<AbstractJob>> jobClasses = new HashSet<Class<AbstractJob>>();

    for (final String clazz : classes) {
      Logger.debug("Trying to load class: " + clazz);

      try {
        final Class<?> forName = Class.forName(clazz, true, application.classloader());
        // if (forName.isAssignableFrom(AbstractJob.class) == false) {
        // Logger.error("The class: " + clazz + " which is annotated with: " +
        // AkkaJob.class.getName() + " is not of the type: " +
        // AbstractJob.class.getName());
        // continue;
        // }
        // instanziate the job
        final Class<AbstractJob> abstractJobClass = (Class<AbstractJob>) forName;

        final Annotation annotation = abstractJobClass.getAnnotation(AkkaJob.class);
        final AkkaJob akkaJob = (AkkaJob) annotation;

        final String annoCronExpression = akkaJob.cronExpression().trim();

        final boolean validExpression = CronExpression.isValidExpression(annoCronExpression);
        if (validExpression == false) {
          Logger.error("The annotated cronExpression: " + annoCronExpression + " is not a valid CronExpression in class: " + clazz);
          continue;
        }

        final CronExpression cronExpression = new CronExpression(annoCronExpression);
        final long nextInterval = cronExpression.getNextInterval(new Date());

        final Constructor<AbstractJob> constructor = abstractJobClass.getConstructor(null);
        final AbstractJob abstractJobInstance = constructor.newInstance(null);

        // todo parse the crontype expression and create a duration from it
        final FiniteDuration create = Duration.create(0, TimeUnit.MILLISECONDS);

        Akka.system().scheduler().schedule(Duration.create(nextInterval, TimeUnit.MILLISECONDS), Duration.create(10, TimeUnit.SECONDS), abstractJobInstance);

      } catch (final NoSuchMethodException e) {
        Logger.error("Could not find default constructor with no parameters in: " + clazz, e);
      } catch (final SecurityException e) {
        Logger.error("Error while initializing class: " + clazz, e);
      } catch (final InstantiationException e) {
        Logger.error("Error while initializing class: " + clazz, e);
      } catch (final IllegalAccessException e) {
        Logger.error("Error while initializing class: " + clazz, e);
      } catch (final IllegalArgumentException e) {
        Logger.error("Error while initializing class: " + clazz, e);
      } catch (final InvocationTargetException e) {
        Logger.error("Error while initializing class: " + clazz, e);
      } catch (final ClassNotFoundException e) {
        Logger.error("Cannot load class: " + clazz, e);
      } catch (final ParseException e) {
        Logger.error("Cannot parse cronExpression in class: " + clazz, e);
      }
    }

  }

}
