/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.cmd.FoxJobRetryCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;

public class DynamicRetryCommand extends FoxJobRetryCmd{

  public static final Pattern DYNAMIC_RETRY_PATTERN = Pattern.compile("D:([0-9]*)/([0-9]*)");

  public DynamicRetryCommand(final String jobId, final Throwable exception) {
      super(jobId, exception);
  }

  @Override
  protected void executeCustomStrategy(final CommandContext commandContext, final JobEntity job, final ActivityImpl activity) throws Exception {
      String failedJobRetryTimeCycle = getFailedJobRetryTimeCycle(activity);
      System.out.println("Execute custom strategy with the input : " + failedJobRetryTimeCycle);

      if (failedJobRetryTimeCycle == null) {
        System.out.println("Execute standard strategy");
          executeStandardStrategy(commandContext);
      } else {
          Matcher dynamicPatternMatcher = DYNAMIC_RETRY_PATTERN.matcher(failedJobRetryTimeCycle);
          if (dynamicPatternMatcher.matches())
          {
            int maxRetries = Integer.parseInt(dynamicPatternMatcher.group(1));
            int timeoutInMinutes = Integer.parseInt(dynamicPatternMatcher.group(2));

            int remainingRetries = maxRetries - job.getRetries();

            setLockExpirationTime(job,
                new Date(ClockUtil.getCurrentTime().getTime() + (timeoutInMinutes * 60 * 1000)));
            System.out.println("Left "+remainingRetries+" Retries"+", the job will be locked until " + job.getLockExpirationTime());

            if (isFirstJobExecution(job)) {
              System.out.println("The first job execution");
              initializeRetries(job, maxRetries);
            }else{
  //              if (job.getRetries() == 1) {
  //                  initializeRetries(job, origTimes);
  //              }else{
              System.out.println("Decrementing retries of job " + jobId);
  //              }
            }

            logException(job);
            decrementRetries(job);
            notifyAcquisition(commandContext);
          } else {
              System.out.println("Execute fox strategy");
              super.executeCustomStrategy(commandContext, job, activity);
          }

      }
  }

  private void initializeRetries(final JobEntity job, final int origTimes) {
      System.out.println("Initialize the job retries and set the job retry times to " + origTimes);
      job.setRetries(origTimes);
  }

  protected void setLockExpirationTime(final JobEntity job, final Date lockExpirationTime ) {
      job.setLockExpirationTime(lockExpirationTime);
  }
}
