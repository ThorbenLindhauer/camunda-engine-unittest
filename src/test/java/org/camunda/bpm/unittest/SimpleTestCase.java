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

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase {

  public static final BpmnModelInstance MODEL1 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNullDelegate.class)
      .endEvent()
      .done();

  public static final BpmnModelInstance MODEL2 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNotNullDelegate.class)
      .endEvent()
      .done();

  public static final BpmnModelInstance MODEL3 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNullDelegate.class)
      .serviceTask()
      .camundaClass(SetVariableNotNullDelegate.class)
      .endEvent()
      .done();

  public static final BpmnModelInstance MODEL4 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNotNullDelegate.class)
      .serviceTask()
      .camundaClass(SetVariableNullDelegate.class)
      .endEvent()
      .done();

  public static final BpmnModelInstance MODEL5 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNotNullDelegate.class)
      .serviceTask()
      .camundaAsyncBefore()
      .camundaClass(SetVariableNullDelegate.class)
      .endEvent()
      .done();

  public static final BpmnModelInstance MODEL6 = Bpmn.createExecutableProcess()
      .startEvent()
      .serviceTask()
      .camundaClass(SetVariableNullDelegate.class)
      .serviceTask()
      .camundaAsyncBefore()
      .camundaClass(SetVariableNotNullDelegate.class)
      .endEvent()
      .done();


  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  public void shouldNotInsertNullValue() {
    // given
    deploy(MODEL1);

    // when
    ProcessInstance procInst = startProcessInstance();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNull();
  }


  @Test
  public void shouldInsertNotNullValue() {
    // given
    deploy(MODEL2);

    // when
    ProcessInstance procInst = startProcessInstance();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNotNull();
    assertThat(historicVariableInstance.getValue()).isEqualTo(Constants.VARAIBLE_VALUE);
  }


  @Test
  public void shouldInsertNotNullValueAfterSettingNullValue() {
    // given
    deploy(MODEL3);

    // when
    ProcessInstance procInst = startProcessInstance();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNotNull();
    assertThat(historicVariableInstance.getValue()).isEqualTo(Constants.VARAIBLE_VALUE);
  }


  @Test
  public void shouldRemoveValueWhenSettingNullValue() {
    // given
    deploy(MODEL4);

    // when
    ProcessInstance procInst = startProcessInstance();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNull();
  }


  @Test
  public void shouldRemovePersistentValueWhenSettingNullValue() {
    // given
    deploy(MODEL5);
    ProcessInstance procInst = startProcessInstance();

    // when
    executeSingleJob();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNull();
  }


  @Test
  public void shouldInsertNotNullValueAfterSettingNullValueInPriorTransaction() {
    // given
    deploy(MODEL6);

    ProcessInstance procInst = startProcessInstance();

    // when
    executeSingleJob();

    // then
    HistoricVariableInstance historicVariableInstance = selectHistoryVariableInstance(procInst);
    assertThat(historicVariableInstance).isNotNull();
    assertThat(historicVariableInstance.getValue()).isEqualTo(Constants.VARAIBLE_VALUE);
  }

  private HistoricVariableInstance selectHistoryVariableInstance(ProcessInstance procInst) {
    return rule.getHistoryService().createHistoricVariableInstanceQuery()
      .processInstanceId(procInst.getId())
      .variableName(Constants.VARIABLE_NAME)
      .singleResult();
  }

  private void deploy(BpmnModelInstance modelInstance)
  {
    Deployment deployment = rule.getRepositoryService().createDeployment()
      .addModelInstance("process.bpmn", modelInstance)
      .deploy();

    rule.manageDeployment(deployment);
  }

  private ProcessInstance startProcessInstance()
  {
    ProcessDefinition procDef = rule.getRepositoryService().createProcessDefinitionQuery().singleResult();
    return rule.getRuntimeService().startProcessInstanceByKey(procDef.getKey());
  }

  private void executeSingleJob()
  {
    Job job = rule.getManagementService().createJobQuery().singleResult();
    rule.getManagementService().executeJob(job.getId());
  }
}
