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

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

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
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {
    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess",
        Variables.createVariables().putValue("correlationVar", "correlationValue"));

    Execution serviceTaskScopeExecution = runtimeService().createExecutionQuery()
        .activityId("ServiceTask_1")
        .processVariableValueEquals("correlationVar", "correlationValue")
        .executionId(AsyncServiceTaskBehavior.MOST_RECENT_EXECUTION_ID)
        .singleResult();

    assertThat(serviceTaskScopeExecution).isNotNull();
    assertThat(serviceTaskScopeExecution.getId()).isNotEqualTo(serviceTaskScopeExecution.getProcessInstanceId());

    // When we signal the async task
    runtimeService().signal(serviceTaskScopeExecution.getId());

    // Then the process instance should be ended
    assertThat(processInstance).isEnded();
  }

}
