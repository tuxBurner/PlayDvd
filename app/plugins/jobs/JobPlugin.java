package plugins.jobs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import play.Application;
import play.Logger;
import akka.actor.Scheduler;
import play.libs.ReflectionsCache;

/**
 * This is a job Plugin which loads all classes which are annotated with the
 * {@link AkkaJob} annotation and adds them to the {@link Scheduler}
 * 
 * @author tuxburner
 *
 */
//TODO: FIX ME TO A MODULE
public class JobPlugin {

  private final Application application;

  public final Set<AbstractJob> jobs = new HashSet<AbstractJob>();

  public JobPlugin(final Application application) {
    this.application = application;
  }

  public void $init$() {
  }


  public void onStart() {


    // find all classes which are annotated with AkkaJob and which are located
    // in the plugins.jobs classpath

    Reflections reflections = ReflectionsCache.getReflections(application.classloader(), "plugins/jobs");
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(AkkaJob.class);

    for (final Class clazz : classes) {
      Logger.debug("Trying to load class: " + clazz.getCanonicalName());

      try {

        final Class<AbstractJob> abstractJobClass = (Class<AbstractJob>) clazz;

        final Constructor<AbstractJob> constructor = abstractJobClass.getConstructor();
        if(constructor == null) {
          continue;
        }
        final AbstractJob newInstance = constructor.newInstance();
        if(newInstance == null) {
          continue;
        }
        jobs.add(newInstance);

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
      }
    }

  }

}
