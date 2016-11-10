//package de.example.processes.example.service;
//
//import java.util.List;
//import java.util.Map;
//
//import de.example.ProcessKeyNotFoundException;
//import org.camunda.bpm.engine.RepositoryService;
//import org.camunda.bpm.engine.RuntimeService;
//import org.camunda.bpm.engine.repository.ProcessDefinition;
//import org.camunda.bpm.engine.runtime.ProcessInstance;
//import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ExampleProcessServiceImpl implements ExampleProcess {
//
//  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleProcessServiceImpl.class);
//
//  @Autowired
//  private RuntimeService runtimeService;
//
//  @Autowired
//  private RepositoryService repositoryService;
//
//  @Override
//  public void startProcessByKeyWIthVariables(String processKey, Map<String, Object> variables) {
//    List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery()
//        .processDefinitionKey(processKey).list();
//
//    if (definitionList.size() == 1) {
//      runtimeService.startProcessInstanceByKey(processKey, variables);
//    }
//    else {
//      throw new ProcessKeyNotFoundException(processKey);
//    }
//  }
//
//}
