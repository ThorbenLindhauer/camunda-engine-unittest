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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import java.util.Date;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  protected static final Date NOW = new Date();

  @Before
  public void setUp()
  {
    ClockUtil.setCurrentTime(NOW);
  }

  @After
  public void tearDown()
  {
    ClockUtil.reset();
  }

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {

    // given retries configured as D:5/10 (i.e. 5 times with 10 minutes offset)
    runtimeService().startProcessInstanceByKey("testProcess");

    Job job = managementService().createJobQuery().singleResult();

    // when
    try
    {
      managementService().executeJob(job.getId());
    }
    catch (Exception e)
    {
    }

    // then the custom configuration has been applied
    Job updatedJob = managementService().createJobQuery().singleResult();

    Assert.assertNotNull(updatedJob);
    Assert.assertEquals(4, updatedJob.getRetries());
    Assert.assertEquals(new Date(NOW.getTime() + 10 * 60 * 1000), ((JobEntity) updatedJob).getLockExpirationTime());
  }
}
