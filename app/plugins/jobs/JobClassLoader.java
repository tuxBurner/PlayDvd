package plugins.jobs;

import akka.actor.ActorSystem;
import com.google.inject.Singleton;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * This searches for all {@link Class}s annotated with {@link AkkaJob} and starts them.
 * @author tuxburner
 *
 */
@Singleton
public class JobClassLoader {

    /**
     * Set of the jobs which where found and loaded
     */
    public final Set<AbstractAkkaJob> jobs = new HashSet<AbstractAkkaJob>();

    /**
     * The {@link ActorSystem} handling the jobs.
     */
    private final ActorSystem actorSystem;

    @Inject
    JobClassLoader(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        onStart();
    }

    public void onStart() {
        final Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forClassLoader()).setScanners(
                        new TypeAnnotationsScanner(), new SubTypesScanner()));
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(AkkaJob.class);

        if(JobModule.LOGGER.isInfoEnabled() == true) {
            JobModule.LOGGER.info("Found: "+classes.size()+" annotated job classes.");
        }

        for (final Class clazz : classes) {
            JobModule.LOGGER.debug("Trying to load class: " + clazz.getCanonicalName());
            try {
                final Class<AbstractAkkaJob> abstractJobClass = (Class<AbstractAkkaJob>) clazz;
                final Constructor<AbstractAkkaJob> constructor = abstractJobClass.getConstructor(ActorSystem.class);
                if(constructor == null) {
                    continue;
                }
                final AbstractAkkaJob newInstance = constructor.newInstance(actorSystem);
                if(newInstance == null) {
                    continue;
                }
                jobs.add(newInstance);

            } catch (final NoSuchMethodException e) {
                JobModule.LOGGER.error("Could not find default constructor with no parameters in: " + clazz, e);
            } catch (final SecurityException e) {
                JobModule.LOGGER.error("Error while initializing class: " + clazz, e);
            } catch (final InstantiationException e) {
                JobModule.LOGGER.error("Error while initializing class: " + clazz, e);
            } catch (final IllegalAccessException e) {
                JobModule.LOGGER.error("Error while initializing class: " + clazz, e);
            } catch (final IllegalArgumentException e) {
                JobModule.LOGGER.error("Error while initializing class: " + clazz, e);
            } catch (final InvocationTargetException e) {
                JobModule.LOGGER.error("Error while initializing class: " + clazz, e);
            }
        }
    }
}
