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

import java.util.Map.Entry;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;

public class ExampleDeployer implements Deployer {

  @Override
  public void deploy(DeploymentEntity deployment) {

    // #isNew returns false if this deployment is already in the Camunda database
    // and is only parsed a second time
    if (deployment.isNew())
    {
      for (Entry<String, ResourceEntity> entry : deployment.getResources().entrySet()) {
        byte[] resource = entry.getValue().getBytes(); // this is the XML

        System.out.println("Handling resource " + entry.getValue().getName());

        // use a transaction listener on COMMITTED to only submit models
        // that have been successfully deployed
        Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {

          @Override
          public void execute(CommandContext commandContext) {
            // upload to cawemo

          }
        });
      }

    }

  }

}
