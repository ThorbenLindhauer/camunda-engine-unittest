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
import java.util.Set;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  protected static CdiContainer cdiContainer;

  @BeforeClass
  public static void setUp() {
    cdiContainer = CdiContainerLoader.getCdiContainer();
    cdiContainer.boot();
    cdiContainer.getContextControl().startContexts();
  }

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = "miParallelProcess.bpmn20.xml")
  public void testParallelMultiInstanceEventsAfterExternalTrigger() {

    rule.getRuntimeService().startProcessInstanceByKey("process");

    TestEventListener listenerBean = ProgrammaticBeanLookup.lookup(TestEventListener.class);
    listenerBean.reset();

    List<Task> tasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(3, tasks.size());

    for (Task task : tasks) {
      rule.getTaskService().complete(task.getId());
    }

    Assert.assertEquals(0, rule.getRuntimeService().createProcessInstanceQuery().count());

    // 6: three user task instances (complete + end)
    // 1: one mi body instance (end)
    // 1: one sequence flow instance (take)
    // 2: one end event instance (start + end)
    // = 10 expected events

    // - 4 events that are prevented by the workaround listener
    Set<BusinessProcessEvent> eventsReceived = listenerBean.getEventsReceived();
    Assert.assertEquals(6, eventsReceived.size());
  }

  @Test
  @Deployment(resources = "miSequentialProcess.bpmn20.xml")
  public void testSequentialMultiInstanceEventsAfterExternalTrigger() {

    rule.getRuntimeService().startProcessInstanceByKey("process");

    TestEventListener listenerBean = ProgrammaticBeanLookup.lookup(TestEventListener.class);
    listenerBean.reset();

    for (int i = 0; i < 3; i++) {
      Task task = rule.getTaskService().createTaskQuery().singleResult();
      Assert.assertNotNull(task);
      rule.getTaskService().complete(task.getId());
    }

    Assert.assertEquals(0, rule.getRuntimeService().createProcessInstanceQuery().count());

    Set<BusinessProcessEvent> eventsReceived = listenerBean.getEventsReceived();
    Assert.assertEquals(10, eventsReceived.size());
  }

}
