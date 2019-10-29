/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Duration;
import java.util.Map;

import org.awaitility.Awaitility;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Test;

public class SimpleTestCase {

  private static final String H2_URL = "jdbc:h2:mem:camunda";

  public static final String CALLING_IN_VARIABLE = "callingInput";
  public static final String CALLING_OUT_VARIABLE = "callingOutput";
  public static final String CALLED_IN_VARIABLE = "calledInput";
  public static final String CALLED_OUT_VARIABLE = "calledOutput";

  public static final String ENGINE_USED_FOR_IN_MAPPING = "engineUsedForInMapping";
  public static final String ENGINE_USED_FOR_OUT_MAPPING = "engineUsedForOutMapping";

  public static final String INPUT = "foo";
  public static final String RESULT = "bar";

  private ProcessEngine callingEngine;
  private ProcessEngine calledEngine;

  @Before
  public void createProcessEngines() {
    callingEngine = buildEngine("callingEngine");
    calledEngine = buildEngine("calledEngine");

    Awaitility.setDefaultPollInterval(Duration.ofMillis(100));
  }

  private ProcessEngine buildEngine(String name) {
    return ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
      .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
      .setJdbcUrl(H2_URL)
      .setJobExecutorDeploymentAware(true)
      .setJobExecutorActivate(true)
      .setProcessEngineName(name)
      .buildProcessEngine();
  }

  @Test
  public void shouldExecuteProcess() {
    // given
    callingEngine.getRepositoryService().createDeployment()
      .addClasspathResource("callingProcess.bpmn")
      .deploy();

    calledEngine.getRepositoryService().createDeployment()
      .addClasspathResource("calledProcess.bpmn")
      .deploy();

    // when the called process is started
    VariableMap startVariables = Variables.createVariables().putValue(CALLING_IN_VARIABLE, INPUT);
    ProcessInstance callingInstance = callingEngine.getRuntimeService().startProcessInstanceByKey("callingProcess", startVariables);

    waitUntilAllJobsExecuted();

    Task calledTask = calledEngine.getTaskService().createTaskQuery().singleResult();

    // then it has the input mapped variables available
    Map<String, Object> variablesInCalledProcess = calledEngine.getRuntimeService().getVariables(calledTask.getProcessInstanceId());
    assertThat(variablesInCalledProcess).containsExactly(entry(CALLED_IN_VARIABLE, INPUT));

    // when the called process finishes
    VariableMap completeVariables = Variables.createVariables().putValue(CALLED_OUT_VARIABLE, RESULT);
    calledEngine.getTaskService().complete(calledTask.getId(), completeVariables);

    waitUntilAllJobsExecuted();

    // then the output mapping was applied in the context of the calling process engine
    Map<String, Object> variablesInCallingProcess = callingEngine.getRuntimeService().getVariables(callingInstance.getId());

    assertThat(variablesInCallingProcess).containsOnly(
        entry(CALLING_IN_VARIABLE, INPUT),
        entry(CALLING_OUT_VARIABLE, RESULT),
        entry(ENGINE_USED_FOR_IN_MAPPING, "callingEngine"),
        entry(ENGINE_USED_FOR_OUT_MAPPING, "callingEngine"));
  }

  private void waitUntilAllJobsExecuted() {
    Awaitility.await()
      .until(() -> callingEngine.getManagementService().createJobQuery().count() == 0);
  }

}
