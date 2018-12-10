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

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import org.junit.Rule;
import org.junit.Test;

public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  public void shouldExecuteProcess() {
    Bpmn.INSTANCE = new CustomBpmn();

    BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess().startEvent()
          .message("foo")
          .userTask("explicit-id")
          .endEvent()
          .error("bar")
          .done();

    Bpmn.writeModelToStream(System.out, modelInstance);
  }

}
