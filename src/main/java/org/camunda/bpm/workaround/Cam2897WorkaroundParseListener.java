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
package org.camunda.bpm.workaround;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.cdi.impl.event.CdiEventListener;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * @author Thorben Lindhauer
 *
 */
public class Cam2897WorkaroundParseListener extends CdiEventSupportBpmnParseListener {

  @Override
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {

    Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> listeners = activity.getListeners();
    List<DelegateListener<? extends BaseDelegateExecution>> endListeners = listeners.get(ExecutionListener.EVENTNAME_END);

    if (endListeners != null) {
      Iterator<DelegateListener<? extends BaseDelegateExecution>> it = endListeners.iterator();

      while (it.hasNext()) {
        DelegateListener<? extends BaseDelegateExecution> nextListener = it.next();

        if (nextListener instanceof CdiEventListener) {
          it.remove();
        }
      }
    }

    super.parseMultiInstanceLoopCharacteristics(activityElement, multiInstanceLoopCharacteristicsElement, activity);
  }
}
