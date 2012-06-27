package jobs;

public abstract class AbstractJob implements Runnable {

  public AbstractJob() throws Exception {

  }

  @Override
  public abstract void run();

}
