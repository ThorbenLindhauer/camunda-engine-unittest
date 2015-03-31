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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.repositoryService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
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
    // get the model instance of the process definition
    ProcessDefinition definition = repositoryService().createProcessDefinitionQuery().singleResult();
    BpmnModelInstance modelInstance = repositoryService().getBpmnModelInstance(definition.getId());

    // get the extension elements
    Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
    Process process = processes.iterator().next();
    ExtensionElements extensionElements = process.getExtensionElements();
    ModelElementInstance extensionElement = extensionElements.getElements().iterator().next();

    // assert the extension
    assertThat(extensionElement.getElementType().getTypeName()).isEqualTo("someExtension");
    assertThat(extensionElement.getTextContent()).isEqualTo("someExtensionValue");
  }

}
