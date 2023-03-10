/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.model.bpmn.builder;

import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.ErrorEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.Event;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeError;

public abstract class AbstractErrorEventDefinitionBuilder<
        B extends AbstractErrorEventDefinitionBuilder<B>>
    extends AbstractRootElementBuilder<B, ErrorEventDefinition> {

  public AbstractErrorEventDefinitionBuilder(
      final BpmnModelInstance modelInstance,
      final ErrorEventDefinition element,
      final Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  @Override
  public B id(final String identifier) {
    return super.id(identifier);
  }

  /** Sets the error attribute with errorCode. */
  public B error(final String errorCode) {
    element.setError(findErrorForNameAndCode(errorCode));
    return myself;
  }

  /**
   * The attribute specifies a variable that holds the error code. The specified variable will be
   * created with error code as the value.
   *
   * @param errorCodeVariable the name of error code variable
   * @return the builder object
   */
  public B errorCodeVariable(final String errorCodeVariable) {
    final ZeebeError error = myself.getCreateSingleExtensionElement(ZeebeError.class);
    error.setErrorCodeVariable(errorCodeVariable);
    return myself;
  }

  /**
   * The attribute specifies a variable that holds the error message. The specified variable will be
   * created with error message as the value.
   *
   * @param errorMessageVariable the name of error message variable
   * @return the builder object
   */
  public B errorMessageVariable(final String errorMessageVariable) {
    final ZeebeError error = myself.getCreateSingleExtensionElement(ZeebeError.class);
    error.setErrorMessageVariable(errorMessageVariable);
    return myself;
  }

  /**
   * Finishes the building of a error event definition.
   *
   * @param <T>
   * @return the parent event builder
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T extends AbstractFlowNodeBuilder> T errorEventDefinitionDone() {
    return (T) ((Event) element.getParentElement()).builder();
  }
}
