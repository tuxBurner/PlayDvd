package jobs;

@AkkaJob(cronExpression = "50")
public class TestJob extends AbstractJob {

  public TestJob() throws Exception {
    super();
  }

  @Override
  public void run() {
    System.out.println("I was called");
  }

}
