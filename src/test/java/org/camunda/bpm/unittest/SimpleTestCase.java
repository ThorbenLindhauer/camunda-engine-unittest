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

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  private LdapTestEnvironment ldapTestEnvironment;

  @Before
  public void setUp() throws Exception {
    ldapTestEnvironment = new LdapTestEnvironment();
    ldapTestEnvironment.init();
  }

  @After
  public void tearDown() throws Exception {
    ldapTestEnvironment.shutdown();
  }

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() throws Exception {

    ldapTestEnvironment.createGroup("office-berlin");
    String dnPeter = ldapTestEnvironment.createUserUid("peter", "office-berlin", "Peter", "Pan", "peter@pan.org");
    ldapTestEnvironment.createRole("foo", dnPeter);

    TaskService taskService = rule.getTaskService();
    taskService.createTaskQuery().taskCandidateUser("peter").list();

  }

}
