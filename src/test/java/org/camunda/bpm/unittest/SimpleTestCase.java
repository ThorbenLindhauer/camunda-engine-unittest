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

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"workaround-idea-conditional-event.bpmn"})
  public void shouldExecuteProcess() {
    RuntimeService runtimeService = rule.getRuntimeService();
    TaskService taskService = rule.getTaskService();

    runtimeService.startProcessInstanceByKey("workaround");

    Task loopTask0 = taskService.createTaskQuery().singleResult();

    taskService.complete(loopTask0.getId(), Variables.createVariables().putValue("iteration", 0));

    Task loopTask1 = taskService.createTaskQuery().singleResult();

    assertThat(loopTask1).isNull(); // condition not yet fulfilled

    runtimeService.correlateMessage("EnableCondition");

    loopTask1 = taskService.createTaskQuery().singleResult();

    assertThat(loopTask1).isNotNull();
    assertThat(loopTask1.getTaskDefinitionKey()).isEqualTo("loopTask");


    taskService.complete(loopTask1.getId(), Variables.createVariables().putValue("iteration", 1));

    Task loopTask2 = taskService.createTaskQuery().singleResult();

    assertThat(loopTask1).isNotNull(); // condition is still fulfilled
    assertThat(loopTask1.getTaskDefinitionKey()).isEqualTo("loopTask");

    taskService.complete(loopTask2.getId(), Variables.createVariables().putValue("iteration", 2));


    Task afterLoopTask = taskService.createTaskQuery().singleResult();

    assertThat(afterLoopTask).isNotNull();
    assertThat(afterLoopTask.getTaskDefinitionKey()).isEqualTo("afterLoopTask");

  }

}
