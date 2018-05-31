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
package org.camunda.example;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.producer.DefaultHistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class BusinessKeyHistoryProducer extends DefaultHistoryEventProducer {

  @Override
  public HistoryEvent createProcessInstanceStartEvt(DelegateExecution execution) {
    HistoricProcessInstanceEventEntity historicProcessInstance =
        (HistoricProcessInstanceEventEntity) super.createProcessInstanceStartEvt(execution);

    final String businessKey = generateBusinessKey();
    historicProcessInstance.setBusinessKey(businessKey);
    ((ExecutionEntity) execution).setBusinessKey(businessKey);

    return historicProcessInstance;
  }

  private String generateBusinessKey() {
    // generate the business key here
    return "foo";
  }
}
