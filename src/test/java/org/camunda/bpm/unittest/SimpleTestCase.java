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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
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

  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = rule.getProcessEngine().getRuntimeService();
    managementService = rule.getProcessEngine().getManagementService();
  }

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldEndProcessAfterModificationWithoutVariables() {
    // Given we create a new process instance
    List<String> elements = new ArrayList<String>();
    elements.add("1");
    elements.add("2");
    elements.add("3");

    // entering and waiting in the first MI instance (job executor deactivated)
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue("elements", elements));

    // when we cancel this iteration and start again at task A
    runtimeService().createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("WaitState")
      .startBeforeActivity("Task_A")
      .execute();

    Job job = managementService.createJobQuery().singleResult();

    // then this is in multi-instance state with a single instance
    Assert.assertEquals(1, runtimeService.getVariable(job.getExecutionId(), "nrOfInstances"));
    Assert.assertEquals(0, runtimeService.getVariable(job.getExecutionId(), "loopCounter"));
    Assert.assertNull(runtimeService.getVariable(job.getExecutionId(), "element"));

    // and when we complete the single instance
    managementService.executeJob(job.getId());

    // then the process instance ends
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldEnterNextIterationAfterModificationWithVariables() {
    // Given we create a new process instance
    List<String> elements = new ArrayList<String>();
    elements.add("1");
    elements.add("2");
    elements.add("3");

    // entering and waiting in the first MI instance (job executor deactivated)
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables().putValue("elements", elements));

    // when we cancel this iteration and start again at task A
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("WaitState")
      .startBeforeActivity("Task_A")
      .setVariable("nrOfInstances", 3)
      .setVariable("loopCounter", 1) // as if we are skipping to the next instance
      .setVariable("element", "2")
      .execute();

    Job job = managementService.createJobQuery().singleResult();

    // then this is in multi-instance state with total of three instances and in progress of the second instance
    Assert.assertEquals(3, runtimeService.getVariable(job.getExecutionId(), "nrOfInstances"));
    Assert.assertEquals(1, runtimeService.getVariable(job.getExecutionId(), "loopCounter"));
    Assert.assertEquals("2", runtimeService.getVariable(job.getExecutionId(), "element"));

    // and when we complete the current instance
    managementService.executeJob(job.getId());

    // then the process instance is not ended
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // but in the next iteration
    job = managementService.createJobQuery().singleResult();

    Assert.assertEquals(3, runtimeService.getVariable(job.getExecutionId(), "nrOfInstances"));
    Assert.assertEquals(2, runtimeService.getVariable(job.getExecutionId(), "loopCounter"));
    Assert.assertEquals("3", runtimeService.getVariable(job.getExecutionId(), "element"));
  }
}
