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
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  protected static final int NUM_INSTANCES = 10000;
  protected static final Map<String, Object> variables = new HashMap<String, Object>();

  static
  {
    for (int i = 0; i < 20; i++)
    {
      variables.put(new String(new char[] {(char) (65 + i)}), i);
    }
  }

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldCreateInstancesNewWay() {
    // Given we create a new process instance

    ProcessEngineConfigurationImpl engineConfiguration = rule.getProcessEngineConfiguration();
    FlushCaptor flushCaptor = (FlushCaptor) engineConfiguration.getCommandContextFactory();

    long startTime = System.currentTimeMillis();
    try{
      flushCaptor.startCapturingFlushs();
      runtimeService().startProcessInstanceByKey("testProcess", variables);
    }
    finally
    {
      flushCaptor.stopCapturingFlushs();
    }

    int numTransactions = 1;
    int chunkSize = (NUM_INSTANCES - 1) / numTransactions;

    for (int i = 0; i < numTransactions; i++)
    {
      flushCaptor.repeatFlush(engineConfiguration.getCommandExecutorTxRequired(), chunkSize);
//      System.out.println("Transaction " + i + " done");
    }
    long endTime = System.currentTimeMillis();

    System.out.println("Inserting took " + (endTime - startTime) + "ms");

    assertThat(rule.getProcessEngine().getRuntimeService().createProcessInstanceQuery().count()).isEqualTo(NUM_INSTANCES);
  }


  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldCreateInstanceOldWay() {
    // Given we create a new process instance

    ProcessEngineConfigurationImpl engineConfiguration = rule.getProcessEngineConfiguration();

    long startTime = System.currentTimeMillis();
    engineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>()
    {

      @Override
      public Void execute(CommandContext commandContext) {
        for (int i = 0; i < NUM_INSTANCES; i++)
        {
          commandContext.getProcessEngineConfiguration().getRuntimeService().startProcessInstanceByKey("testProcess", variables);
        }

        return null;
      }

    });
    long endTime = System.currentTimeMillis();

    System.out.println("Inserting took " + (endTime - startTime) + "ms");

    assertThat(rule.getProcessEngine().getRuntimeService().createProcessInstanceQuery().count()).isEqualTo(NUM_INSTANCES);
  }


}
