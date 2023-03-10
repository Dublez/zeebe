/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.it.client.command;

import static io.camunda.zeebe.test.util.TestUtil.waitUntil;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.zeebe.broker.test.EmbeddedBrokerRule;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.it.util.GrpcClientRule;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.test.util.BrokerClassRuleHelper;
import io.camunda.zeebe.test.util.collection.Maps;
import io.camunda.zeebe.test.util.record.RecordingExporter;
import java.util.Map;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public final class CreateProcessInstanceWithResultTest {

  private static final EmbeddedBrokerRule BROKER_RULE = new EmbeddedBrokerRule();
  private static final GrpcClientRule CLIENT_RULE = new GrpcClientRule(BROKER_RULE);

  @ClassRule
  public static RuleChain ruleChain = RuleChain.outerRule(BROKER_RULE).around(CLIENT_RULE);

  @Rule public final BrokerClassRuleHelper helper = new BrokerClassRuleHelper();

  private String processId;
  private long processDefinitionKey;
  private String jobType;

  @Before
  public void deployProcess() {
    processId = helper.getBpmnProcessId();
    processDefinitionKey =
        CLIENT_RULE.deployProcess(Bpmn.createExecutableProcess(processId).startEvent("v1").done());
    jobType = helper.getJobType();
  }

  @Test
  public void shouldCreateProcessInstanceAwaitResults() {
    final Map<String, Object> variables = Maps.of(entry("foo", "bar"));
    final ProcessInstanceResult result = createProcessInstanceWithVariables(variables).join();

    assertThat(result.getBpmnProcessId()).isEqualTo(processId);
    assertThat(result.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    assertThat(result.getVariablesAsMap()).containsExactly(entry("foo", "bar"));
  }

  @Test
  public void shouldCreateProcessInstanceAwaitResultsWithNoVariables() {
    // given
    final ProcessInstanceResult result =
        CLIENT_RULE
            .getClient()
            .newCreateInstanceCommand()
            .processDefinitionKey(processDefinitionKey)
            .withResult()
            .send()
            .join();

    // then
    assertThat(result.getBpmnProcessId()).isEqualTo(processId);
    assertThat(result.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    assertThat(result.getVariablesAsMap()).isEmpty();
  }

  @Test
  public void shouldCollectMergedVariables() {
    // given
    deployProcessWithJob();
    final Map<String, Object> variables = Maps.of(entry("foo", "bar"));
    final ZeebeFuture<ProcessInstanceResult> resultFuture =
        createProcessInstanceWithVariables(variables);

    completeJobWithVariables(Map.of("x", "y"));

    // then
    final ProcessInstanceResult result = resultFuture.join();
    assertThat(result.getBpmnProcessId()).isEqualTo(processId);
    assertThat(result.getVariablesAsMap())
        .containsExactlyInAnyOrderEntriesOf(Map.of("foo", "bar", "x", "y"));
  }

  @Test
  public void shouldOnlyReturnVariablesInRootScope() {
    // given
    final BpmnModelInstance processWithVariableScopes =
        Bpmn.createExecutableProcess(processId)
            .startEvent()
            .subProcess(
                "sub",
                b -> {
                  b.embeddedSubProcess()
                      .startEvent()
                      .serviceTask("task", t -> t.zeebeJobType(jobType))
                      .endEvent();
                  b.zeebeInputExpression("x", "y");
                })
            .endEvent()
            .done();
    processDefinitionKey = CLIENT_RULE.deployProcess(processWithVariableScopes);

    final ZeebeFuture<ProcessInstanceResult> resultFuture =
        createProcessInstanceWithVariables(Map.of("x", "1"));

    // when
    completeJobWithVariables(Map.of("y", "2"));

    // then
    final ProcessInstanceResult result = resultFuture.join();
    assertThat(result.getBpmnProcessId()).isEqualTo(processId);
    assertThat(result.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    assertThat(result.getVariablesAsMap()).containsExactly(entry("x", "1"));
  }

  @Test
  public void shouldReceiveRejectionCreateProcessInstanceAwaitResults() {
    final var command =
        CLIENT_RULE
            .getClient()
            .newCreateInstanceCommand()
            .processDefinitionKey(123L)
            .withResult()
            .send();

    assertThatThrownBy(() -> command.join())
        .isInstanceOf(ClientException.class)
        .hasMessageContaining("Expected to find process definition with key '123', but none found");
  }

  @Test
  public void shouldCreateProcessInstanceAwaitResultsWithFetchVariables() {
    // when
    final Map<String, Object> variables = Map.of("x", "foo", "y", "bar");
    final ProcessInstanceResult result =
        CLIENT_RULE
            .getClient()
            .newCreateInstanceCommand()
            .processDefinitionKey(processDefinitionKey)
            .variables(variables)
            .withResult()
            .fetchVariables("y")
            .send()
            .join();

    // then
    assertThat(result.getBpmnProcessId()).isEqualTo(processId);
    assertThat(result.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    assertThat(result.getVariablesAsMap()).containsExactly(entry("y", "bar"));
  }

  private ZeebeFuture<ProcessInstanceResult> createProcessInstanceWithVariables(
      final Map<String, Object> variables) {
    return CLIENT_RULE
        .getClient()
        .newCreateInstanceCommand()
        .processDefinitionKey(processDefinitionKey)
        .variables(variables)
        .withResult()
        .send();
  }

  private void deployProcessWithJob() {
    processId = helper.getBpmnProcessId();
    processDefinitionKey =
        CLIENT_RULE.deployProcess(
            Bpmn.createExecutableProcess(processId)
                .startEvent("v1")
                .serviceTask(
                    "task",
                    t -> {
                      t.zeebeJobType(jobType);
                    })
                .endEvent("end")
                .done());
  }

  private void completeJobWithVariables(final Map<String, Object> variables) {
    waitUntil(() -> RecordingExporter.jobRecords(JobIntent.CREATED).withType(jobType).exists());

    final ActivateJobsResponse response =
        CLIENT_RULE
            .getClient()
            .newActivateJobsCommand()
            .jobType(jobType)
            .maxJobsToActivate(1)
            .send()
            .join();

    // when
    CLIENT_RULE
        .getClient()
        .newCompleteCommand(response.getJobs().iterator().next().getKey())
        .variables(variables)
        .send();
  }
}
