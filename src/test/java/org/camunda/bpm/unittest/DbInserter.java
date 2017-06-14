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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class DbInserter {

  protected static final int NUM_INSTANCES = 150000;
  protected static final int CHUNK_SIZE = 500;

  public static void main(String[] args)
  {
//    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.oracle.cfg.xml");
    ProcessEngine processEngine = engineConfiguration.buildProcessEngine();

    RepositoryService repositoryService = processEngine.getRepositoryService();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    Deployment deployment = repositoryService.createDeployment().addClasspathResource("testProcess.bpmn").deploy();
    Map<String, Object> variables = new HashMap<String, Object>();

    for (int i = 0; i < 20; i++)
    {
      variables.put(new String(new char[] {(char) (65 + i)}), i);
    }

    FlushCaptor flushCaptor = (FlushCaptor) engineConfiguration.getCommandContextFactory();

    long startTime = System.currentTimeMillis();
    try{
      flushCaptor.startCapturingFlushs();
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    }
    finally
    {
      flushCaptor.stopCapturingFlushs();
    }

    int instancesInserted = 0;

    while (instancesInserted < NUM_INSTANCES)
    {
      flushCaptor.repeatFlush(engineConfiguration.getCommandExecutorTxRequired(), CHUNK_SIZE);
      instancesInserted += CHUNK_SIZE;
      System.out.println("Num instances inserted: " + instancesInserted);
    }
    long endTime = System.currentTimeMillis();

    System.out.println("Inserting took " + (endTime - startTime) + "ms");
  }
}
