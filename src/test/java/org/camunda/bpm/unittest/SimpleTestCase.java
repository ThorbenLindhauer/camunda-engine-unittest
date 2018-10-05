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

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() throws JsonParseException, JsonMappingException, IOException {
    List<ListElement> list = new ArrayList<>();
    ListElement element1 = new ListElement();
    ListElement element2 = new AnotherListElement();
    list.add(element1);
    list.add(element2);
    ObjectValue variableValue = Variables.objectValue(list)
      .serializationDataFormat(Variables.SerializationDataFormats.JSON.getName())
      .create();

    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService()
        .startProcessInstanceByKey("testProcess", Variables.createVariables().putValue("var", variableValue));

    final ObjectValue serializedValue = rule.getRuntimeService().getVariableTyped(processInstance.getId(), "var", false);
    System.out.println(serializedValue.getValueSerialized());
    final List<ListElement> deserializedValue = (List<ListElement>) rule.getRuntimeService().getVariable(processInstance.getId(), "var");

    assertThat(deserializedValue).hasSize(2);
    assertThat(deserializedValue.get(0)).isInstanceOf(ListElement.class);
    assertThat(deserializedValue.get(1)).isInstanceOf(AnotherListElement.class);
  }

}
