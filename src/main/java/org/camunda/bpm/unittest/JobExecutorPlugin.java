package org.camunda.bpm.unittest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.CachedDbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityCache;
import org.camunda.bpm.engine.impl.db.entitymanager.cache.DbEntityState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

public class JobExecutorPlugin implements ProcessEnginePlugin {

  public static final String PLUGIN_LOCK_OWNER = "foo";

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

    LockJobsInterceptor interceptor = new LockJobsInterceptor();

    // TODO: be aware of other plugins
    processEngineConfiguration.setCustomPostCommandInterceptorsTxRequired(Arrays.asList(interceptor));
    processEngineConfiguration.setCustomPostCommandInterceptorsTxRequiresNew(Arrays.asList(interceptor));
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {

  }


  private static class LockJobsInterceptor extends CommandInterceptor {

    @Override
    public <T> T execute(Command<T> command) {

      CommandContext commandContext = Context.getCommandContext();
      // TODO: do not register listener twice in case of nested commands
      commandContext.registerCommandContextListener(new LockJobsCommandContextListener());

      return next.execute(command);
    }

  }
  private static class LockJobsCommandContextListener implements CommandContextListener {

    @Override
    public void onCommandContextClose(CommandContext commandContext) {
      DbEntityCache entityCache = commandContext.getDbEntityManager().getDbEntityCache();

      List<AcquirableJobEntity> allJobs = entityCache.getEntitiesByType(AcquirableJobEntity.class);

      List<JobEntity> newJobs = allJobs.stream()
          .filter(j -> j instanceof JobEntity)
          .map(j -> (JobEntity) j)
          .filter(j -> {
        CachedDbEntity cachedJob = entityCache.getCachedEntity(MessageEntity.class, j.getId());
        return j.getJobHandlerType().equals(AsyncContinuationJobHandler.TYPE) && cachedJob.getEntityState() == DbEntityState.TRANSIENT;
      })
      .collect(Collectors.toList());

      newJobs.forEach(j -> {
        Date lockTime = new Date(new Date().toInstant().toEpochMilli() + Duration.ofMinutes(5).toMillis());

        j.setLockExpirationTime(lockTime);
        j.setLockOwner(PLUGIN_LOCK_OWNER);
      });

      if (!newJobs.isEmpty()) {
        // TODO: do something on rollback
        commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, c -> {
          JobExecutor jobExecutor = c.getProcessEngineConfiguration().getJobExecutor();
          ProcessEngineImpl engine = c.getProcessEngineConfiguration().getProcessEngine();

          List<String> jobIds = newJobs.stream().map(j -> j.getId()).collect(Collectors.toList());

          jobExecutor.executeJobs(jobIds, engine);
        });
      }

    }

    @Override
    public void onCommandFailed(CommandContext commandContext, Throwable t) {
    }

  }
}
