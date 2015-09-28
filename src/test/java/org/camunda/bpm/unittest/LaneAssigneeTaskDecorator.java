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

import java.util.Collection;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.UserTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class LaneAssigneeTaskDecorator extends TaskDecorator {

  public LaneAssigneeTaskDecorator(TaskDefinition taskDefinition, ExpressionManager expressionManager) {
    super(taskDefinition, expressionManager);
  }

  protected void initializeTaskAssignee(TaskEntity task, VariableScope variableScope) {
    UserTask userTaskElement = task.getBpmnModelElementInstance();

    Collection<Process> processElements = task.getBpmnModelInstance().getModelElementsByType(Process.class);
    Process process = processElements.iterator().next();

    Collection<LaneSet> laneSets = process.getLaneSets();
    Lane containingLane = findLaneContainingFlowNode(laneSets, userTaskElement);

    task.setAssignee(containingLane.getName());
  }

  protected Lane findLaneContainingFlowNode(Collection<LaneSet> laneSets, FlowNode flowNode) {
    for (LaneSet laneSet : laneSets) {
      for (Lane lane : laneSet.getLanes()) {
        for (FlowNode candidateFlowNode : lane.getFlowNodeRefs()) {
          if (candidateFlowNode.equals(flowNode)) {
            return lane;
          }
        }
      }
    }

    return null;
  }

}
