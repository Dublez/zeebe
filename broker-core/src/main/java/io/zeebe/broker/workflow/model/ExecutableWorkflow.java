/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.workflow.model;

import io.zeebe.util.buffer.BufferUtil;
import java.util.HashMap;
import java.util.Map;
import org.agrona.DirectBuffer;

/** Executable* prefix in order to avoid confusion with model API classes. */
public class ExecutableWorkflow {

  private DirectBuffer id;
  private Map<DirectBuffer, ExecutableFlowElement> flowElements = new HashMap<>();
  private ExecutableFlowNode startEvent;

  public ExecutableWorkflow(String id) {
    this.id = BufferUtil.wrapString(id);
  }

  public DirectBuffer getId() {
    return id;
  }

  public void addFlowElement(ExecutableFlowElement element) {
    flowElements.put(element.getId(), element);
  }

  public ExecutableFlowElement getElementById(DirectBuffer id) {
    return flowElements.get(id);
  }

  /** convenience function for transformation */
  public <T extends ExecutableFlowElement> T getElementById(String id, Class<T> expectedType) {
    final DirectBuffer buffer = BufferUtil.wrapString(id);
    final ExecutableFlowElement element = flowElements.get(buffer);
    if (element == null) {
      return null;
    }

    if (expectedType.isAssignableFrom(element.getClass())) {
      return (T) element;
    } else {
      throw new RuntimeException("Element is not an instance of " + expectedType.getSimpleName());
    }
  }

  public ExecutableFlowNode getStartEvent() {
    return startEvent;
  }

  public void setStartEvent(ExecutableFlowNode startEvent) {
    this.startEvent = startEvent;
  }
}
