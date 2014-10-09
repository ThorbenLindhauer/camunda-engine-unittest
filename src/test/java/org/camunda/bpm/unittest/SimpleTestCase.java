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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"terminateEnd.bpmn20.xml"})
  public void shouldBeCancelledOnTerminateEnd() {
    RuntimeService runtimeService = rule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    TaskService taskService = rule.getTaskService();
    Task task = taskService.createTaskQuery().taskName("task before termination").singleResult();
    taskService.complete(task.getId());

    assertWasCancelled(true);
  }

  @Test
  @Deployment(resources = {"oneTaskProcess.bpmn20.xml"})
  public void shouldBeCancelledOnProcessInstanceDeletion() {
    RuntimeService runtimeService = rule.getRuntimeService();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    assertWasCancelled(true);
  }

  @Test
  @Deployment(resources = {"terminateEnd.bpmn20.xml"})
  public void shouldNotBeCancelledOnRegularEnd() {
    RuntimeService runtimeService = rule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    TaskService taskService = rule.getTaskService();
    Task task = taskService.createTaskQuery().taskName("task before end").singleResult();
    taskService.complete(task.getId());

    assertWasCancelled(false);
  }

  protected void assertWasCancelled(boolean wasCancelled) {
    HistoryService historyService = rule.getHistoryService();
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
    Assert.assertNotNull(variable);
    Assert.assertEquals("isCancelled", variable.getVariableName());
    Assert.assertEquals(wasCancelled, variable.getValue());
  }



}
