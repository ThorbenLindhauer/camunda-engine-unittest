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
package org.camunda.example;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldCreateBusinessKey() {

    RuntimeService runtimeService = rule.getRuntimeService();
    HistoryService historyService = rule.getHistoryService();
    ManagementService managementService = rule.getManagementService();

    Job timerStartJob = managementService.createJobQuery().singleResult();
    managementService.executeJob(timerStartJob.getId());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    Assert.assertNotNull(processInstance.getBusinessKey());

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    Assert.assertEquals(processInstance.getBusinessKey(), historicProcessInstance.getBusinessKey());
  }

  @Test
  @Deployment(resources = {"testProcessAsync.bpmn"})
  public void shouldCreateBusinessKeyOnAsyncStart() {

    RuntimeService runtimeService = rule.getRuntimeService();
    HistoryService historyService = rule.getHistoryService();
    ManagementService managementService = rule.getManagementService();

    Job timerStartJob = managementService.createJobQuery().singleResult();
    managementService.executeJob(timerStartJob.getId());

    Job asyncBeforeJob = managementService.createJobQuery().messages().singleResult();
    managementService.executeJob(asyncBeforeJob.getId());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    Assert.assertNotNull(processInstance.getBusinessKey());

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    Assert.assertEquals(processInstance.getBusinessKey(), historicProcessInstance.getBusinessKey());
  }
}
