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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperationManager;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class FlushCaptor extends CommandContextFactory implements ProcessEnginePlugin {

  protected static final ThreadLocal<Boolean> SHOULD_CAPTURE = new ThreadLocal<Boolean>();

  protected List<DbOperation> lastFlush;

  @Override
  public CommandContext createCommandContext() {
    return new CommandContext(processEngineConfiguration)
    {
      @Override
      public void close(CommandInvocationContext commandInvocationContext) {
        super.close(commandInvocationContext);

        Boolean shouldCapture = SHOULD_CAPTURE.get();
        if (shouldCapture != null && shouldCapture == true)
        {
          DbOperationManager dbOperationManager = getDbEntityManager().getDbOperationManager();
          lastFlush = dbOperationManager.calculateFlush();
        }
      }
    };
  }


  public void startCapturingFlushs()
  {
    SHOULD_CAPTURE.set(true);
  }

  public void stopCapturingFlushs()
  {
    SHOULD_CAPTURE.remove();
  }

  public void repeatFlush(CommandExecutor commandExecutor, int times)
  {
    commandExecutor.execute(new Command<Void>(){

      // TODO: validate that flush is only inserts
      // TODO: validate that all operations are entity operations

      @Override
      public Void execute(CommandContext commandContext) {
        IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
        PersistenceSession persistenceSession = commandContext.getSession(PersistenceSession.class);

        for (int i = 0; i < times; i++)
        {
          for (DbOperation dbOperation : lastFlush) {
            try {
              DbEntity dbEntity = ((DbEntityOperation) dbOperation).getEntity();

              if (dbEntity instanceof HistoryEvent || dbEntity instanceof HistoricVariableInstanceEntity)
              {
                // History is not supported for now; too hard to get "foreign keys" right
                continue;
              }

              dbEntity.setId(idGenerator.getNextId());
              updateDbEntityReferences(dbEntity);
              persistenceSession.executeDbOperation(dbOperation);
            } catch (Exception e) {
              throw new RuntimeException("Could not flush", e);
            }
            if (dbOperation.isFailed()) {
              throw new RuntimeException("Could not flush");
            }
          }
        }

        return null;
      }

    });
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.setProcessEngineConfiguration(processEngineConfiguration);
    processEngineConfiguration.setCommandContextFactory(this);
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
  }

  protected void updateDbEntityReferences(DbEntity dbEntity)
  {
    if (dbEntity instanceof ExecutionEntity)
    {
      ExecutionEntity execution = ((ExecutionEntity) dbEntity);
      execution.setProcessInstanceId(execution.getProcessInstance().getId());
      if (execution.getParent() != null)
      {
        execution.setParentId(execution.getParent().getId());
      }
    }
    else if (dbEntity instanceof TaskEntity)
    {
      TaskEntity task = ((TaskEntity) dbEntity);
      task.setExecutionId(task.getExecution().getId());
      task.setProcessInstanceId(task.getExecution().getProcessInstanceId());
    }
    else if (dbEntity instanceof VariableInstanceEntity)
    {
      VariableInstanceEntity variable = ((VariableInstanceEntity) dbEntity);
      ExecutionEntity execution = variable.getExecution();
      variable.setProcessInstanceId(execution.getProcessInstance().getId());
      variable.setExecutionId(execution.getId());
      variable.setTaskId(execution.getTasks().get(0).getId());
    }
  }

}
