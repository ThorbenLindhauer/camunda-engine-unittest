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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
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
        Variables.createVariables().putValue("myCounter", 0));

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration();
    CommandExecutor commandExecutor = engineConfiguration.getCommandExecutorTxRequired();

    readModifyWriteVariable(commandExecutor, processInstance.getId(), "myCounter", 1);
  }

  protected void readModifyWriteVariable(CommandExecutor commandExecutor, final String processInstanceId,
      final String variableName, final int valueToAdd) {

    try {
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          Integer myCounter = (Integer) runtimeService().getVariable(processInstanceId, variableName);

          // do something with variable
          myCounter += valueToAdd;

          // the update provokes an OptimisticLockingException when the command ends, if the variable was updated meanwhile
          runtimeService().setVariable(processInstanceId, variableName, myCounter);

          return null;
        }
      });
    } catch (OptimisticLockingException e) {
      // try again
      readModifyWriteVariable(commandExecutor, processInstanceId, variableName, valueToAdd);
    }

  }

}
