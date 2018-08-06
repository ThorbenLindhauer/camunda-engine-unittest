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

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.util.Collections;
import java.util.Map;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.Assert;
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
  @Deployment(resources = {"mbi_Bestellbestaetigung.bpmn"})
  public void shouldExecuteProcess() {
    RuntimeService runtimeService = rule.getRuntimeService();
    ManagementService managementService = rule.getManagementService();
    TaskService taskService = rule.getTaskService();
    FormService formService = rule.getFormService();

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey("neu_Bestellbestaetigung")
      .startBeforeActivity("Task_1fcboap")
      .execute();

    Job j = managementService.createJobQuery().singleResult();
    managementService.executeJob(j.getId());

    Task task = taskService.createTaskQuery().singleResult();
    Map<String, Object> vars = Collections.<String, Object>singletonMap("AnzahlTageNachfrist", 5);
    formService.submitTaskForm(task.getId(), vars);

    j = managementService.createJobQuery().singleResult();
    managementService.executeJob(j.getId());

    Object dueDateValue = runtimeService.getVariable(processInstance.getId(), "dueDate");
    Assert.assertEquals(Long.valueOf(5), dueDateValue);
  }

}
