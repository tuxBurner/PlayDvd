package modules.jobs;

import akka.actor.ActorSystem;

import java.util.Date;

@AkkaJob(cronExpression = "0/5 * * * * ?")
public class TestJob extends AbstractAkkaJob {

  public TestJob(ActorSystem actorSystem) throws Exception {
    super(actorSystem);
    setRestartOnFail(false);
  }

  @Override
  public void runInternal() {
    System.out.println("I was called at: " + new Date());
  }

}
