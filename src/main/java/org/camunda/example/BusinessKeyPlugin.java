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

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public class BusinessKeyPlugin implements ProcessEnginePlugin {

  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // Choose one of these:
    processEngineConfiguration.setCustomPreBPMNParseListeners(Arrays.<BpmnParseListener>asList(new BusinessKeyParseListener()));

//    processEngineConfiguration.setHistoryEventProducer(new BusinessKeyHistoryProducer());
  }

  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  public void postProcessEngineBuild(ProcessEngine processEngine) {

  }

}
