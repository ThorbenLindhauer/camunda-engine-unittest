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

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

public class CustomBpmn extends Bpmn {

  private CustomBpmnParser customBpmnParser = new CustomBpmnParser();

  @Override
  protected BpmnModelInstance doCreateEmptyModel() {
    return customBpmnParser.getEmptyModel();
  }

  private class CustomBpmnParser extends BpmnParser
  {
    @Override
    protected BpmnModelInstanceImpl createModelInstance(DomDocument document) {
      return new CustomBpmnModelInstance((ModelImpl) Bpmn.INSTANCE.getBpmnModel(), Bpmn.INSTANCE.getBpmnModelBuilder(), document);
    }
  }

  private class CustomBpmnModelInstance extends BpmnModelInstanceImpl
  {
    private int idCounter = 0;

    public CustomBpmnModelInstance(ModelImpl model, ModelBuilder modelBuilder, DomDocument document) {
      super(model, modelBuilder, document);
    }

    @Override
    public <T extends ModelElementInstance> T newInstance(ModelElementType type, String id) {
      ModelElementInstance instance = super.newInstance(type, id);
      if (id == null)
      {
        final String generatedId = "element_" + idCounter;
        idCounter++;
        ModelUtil.setNewIdentifier(type, instance, generatedId, false);
      }

      return (T) instance;
    }
  }
}
