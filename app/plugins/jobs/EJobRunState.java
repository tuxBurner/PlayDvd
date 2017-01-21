package plugins.jobs;

/**
 * This is the state of the job the job had on its run
 * 
 * @author tuxburner
 * 
 */
public enum EJobRunState {
  RUNNING,
  SCHEDULED,
  STOPPED,
  KILLED,
  ERROR
}
