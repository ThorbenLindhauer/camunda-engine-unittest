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

import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

public class NullValueHistoryHandler extends DbHistoryEventHandler {

  @Override
  protected void insertHistoricVariableUpdateEntity(HistoricVariableUpdateEventEntity historyEvent) {
    // updates the historic variable instance according to the new value
    super.insertHistoricVariableUpdateEntity(historyEvent);

    DbEntityManager dbEntityManager = getDbEntityManager();

    HistoricVariableInstanceEntity historicVariableInstance = dbEntityManager.selectById(HistoricVariableInstanceEntity.class, historyEvent.getVariableInstanceId());
    if (isValueNull(historyEvent))
    {
      // marks the variable in the entity cache as deleted. Will be removed from database on flush
      // at the end of the transaction.
      historicVariableInstance.delete();
    }
    else
    {
      CachedDbEntity cachedEntity = dbEntityManager.getDbEntityCache().getCachedEntity(historicVariableInstance);
      DbEntityState entityState = cachedEntity.getEntityState();

      // This is the case when we marked the variable previously as deleted. We have to "undelete" it
      // in the cache in order to ensure that the value is not removed from the database on flush.
      if ((entityState == DbEntityState.DELETED_TRANSIENT) || (entityState == DbEntityState.DELETED_PERSISTENT)) {
        dbEntityManager.undoDelete(historicVariableInstance);
      }
    }
  }

  private boolean isValueNull(HistoricVariableUpdateEventEntity historyEvent)
  {
    return historyEvent.getTextValue() == null && historyEvent.getTextValue2() == null
        && historyEvent.getDoubleValue() == null && historyEvent.getByteValue() == null
        && historyEvent.getLongValue() == null;
  }
}
