package de.example.processes.example.task;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;


public class DefaultTask implements JavaDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTask.class);

  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {

    String currentActivity = delegateExecution.getCurrentActivityName();
    String processId = delegateExecution.getProcessInstanceId();

    LOGGER.info("Starting process " + processId + " task " + currentActivity + " " + (new Date()).toString());

    for(int i = 1; i<4; i++) {
      Thread.sleep(1000);
      LOGGER.info( "process: " + processId + " task: " + currentActivity + " count: " + i );
    }

    LOGGER.info("Ending process " + processId + " task " + currentActivity + " " + (new Date()).toString());

  }

}
