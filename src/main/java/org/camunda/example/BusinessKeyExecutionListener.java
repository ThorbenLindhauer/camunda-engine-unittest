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
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

public class BusinessKeyExecutionListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {

    String businessKey = generateBusinessKey();
    ((ExecutionEntity) execution).setBusinessKey(businessKey);

    DbEntityManager entityManager = Context.getCommandContext().getDbEntityManager();
    HistoricProcessInstanceEventEntity historicProcessInstance =
        entityManager.getCachedEntity(HistoricProcessInstanceEventEntity.class, execution.getProcessInstanceId());

    if (historicProcessInstance != null)
    {
      historicProcessInstance.setBusinessKey(businessKey);
    }
  }

  private String generateBusinessKey() {
    // generate business key here
    return "foo";
  }
}
