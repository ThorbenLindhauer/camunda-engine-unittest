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

import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * @author Thorben Lindhauer
 *
 */
public class TaskAssigneeParseListener extends AbstractBpmnParseListener {

  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    // replace the user task behavior
    TaskDecorator currentTaskDecorator = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDecorator();

    LaneAssigneeTaskDecorator newTaskDecorator = new LaneAssigneeTaskDecorator(currentTaskDecorator.getTaskDefinition(), currentTaskDecorator.getExpressionManager());
    UserTaskActivityBehavior behavior = new UserTaskActivityBehavior(newTaskDecorator);

    activity.setActivityBehavior(behavior);
  }

}
