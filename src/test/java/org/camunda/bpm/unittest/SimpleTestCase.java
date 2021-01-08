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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskQuery;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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

  @Before
  public void resetRecorder() {
    RecordingDelegate.reset();
  }

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() throws InterruptedException {
    // Given we create a new process instance
    RuntimeService runtimeService = rule.getRuntimeService();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    ManagementService managementService = rule.getManagementService();
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();

    assertThat(job.getLockExpirationTime()).isNotNull();
    assertThat(job.getLockOwner()).isEqualTo(JobExecutorPlugin.PLUGIN_LOCK_OWNER);

    Thread.sleep(5000);

    assertThat(taskQuery().singleResult()).isNotNull();
    HistoricJobLog jobLog = rule.getHistoryService().createHistoricJobLogQuery().successLog().singleResult();

//    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

  }

  @Test
  @Deployment(resources = {"two-async-continuations.bpmn"})
  public void shouldNotExecuteExclusiveFollowupJobTwice() throws InterruptedException {
    // given
    RuntimeService runtimeService = rule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("testProcess");

    // when job executor takes care of jobs
    Thread.sleep(5000);

    // then
    long numJobs = rule.getManagementService().createJobQuery().count();
    assertThat(numJobs).isEqualTo(0);

    assertThat(taskQuery().singleResult()).isNotNull();

    assertThat(RecordingDelegate.getNumInvocations()).isEqualTo(1);


  }
}
