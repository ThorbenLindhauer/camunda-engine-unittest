package org.camunda.bpm.unittest;

import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class RecordingDelegate implements JavaDelegate {

  private static AtomicInteger NUM_INVOCATIONS = new AtomicInteger(0);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    NUM_INVOCATIONS.incrementAndGet();
  }

  public static void reset() {
    NUM_INVOCATIONS.set(0);
  }

  public static int getNumInvocations() {
    return NUM_INVOCATIONS.get();
  }
}
