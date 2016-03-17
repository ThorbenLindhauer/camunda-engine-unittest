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

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class AvoidDuplicateHistoryInsertsListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {

    final HistoryService historyService = execution.getProcessEngineServices().getHistoryService();

    Context.getCommandContext().registerCommandContextListener(new CommandContextListener() {

      public void onCommandContextClose(CommandContext commandContext) {
        // get persistent historic variables
        List<HistoricVariableInstance> historicPersistentVariables = historyService
          .createHistoricVariableInstanceQuery()
          .list();

        // get variables currently in cache (for which inserts/updates are going to be flushed)
        DbEntityManager dbEntityManager = commandContext.getDbEntityManager();
        List<HistoricVariableInstanceEntity> cachedVariables = dbEntityManager
            .getCachedEntitiesByType(HistoricVariableInstanceEntity.class);

        // remove those that are already persistent
        for (HistoricVariableInstanceEntity cachedEntity : cachedVariables) {
          for (HistoricVariableInstance persistentEntity : historicPersistentVariables) {
            if (cachedEntity.getName().equals(persistentEntity.getName())) {
              // note: this drops all updates to the variable instance that were made in the current transaction
              dbEntityManager.getDbEntityCache().remove(cachedEntity);
            }
          }
        }

      }

      public void onCommandFailed(CommandContext commandContext, Throwable t) {
      }
    });

  }

}
