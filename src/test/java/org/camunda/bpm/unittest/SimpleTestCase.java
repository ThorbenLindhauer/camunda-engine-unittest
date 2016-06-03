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

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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
  @Deployment(resources = {"testProcess.bpmn", "callingProcess.bpmn", "callingProcess_V2.bpmn"})
  public void shouldExecuteProcess() {
    // Given we create a new process instance
    ProcessDefinition callingProcess = rule.getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey("callingProcess")
        .singleResult();
    ProcessInstance processInstance = runtimeService().startProcessInstanceById(callingProcess.getId());

    LockedExternalTask task = rule.getExternalTaskService().fetchAndLock(1, "foo")
      .topic("foo", 1000L)
      .execute()
      .get(0);
    // creating an incident in the called and calling process
    rule.getExternalTaskService().handleFailure(task.getId(), "foo", "error", 0, 1000L);

    Incident incidentInCallingProcess = runtimeService().createIncidentQuery().processDefinitionId(callingProcess.getId()).singleResult();

    // when
    ProcessDefinition callingProcessV2 = rule.getRepositoryService()
        .createProcessDefinitionQuery()
        .processDefinitionKey("callingProcessV2")
        .singleResult();

    MigrationPlan migrationPlan = runtimeService()
        .createMigrationPlan(callingProcess.getId(), callingProcessV2.getId())
        .mapEqualActivities()
        .build();

    runtimeService().newMigration(migrationPlan).processInstanceIds(processInstance.getId()).execute();

    // then
    Incident incidentAfterMigration = runtimeService().createIncidentQuery().incidentId(incidentInCallingProcess.getId()).singleResult();
    Assert.assertEquals(callingProcessV2.getId(), incidentAfterMigration.getProcessDefinitionId());
    Assert.assertEquals("CallActivityV2", incidentAfterMigration.getActivityId());

  }

}
