/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.api;

import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecord;

public interface Engine {

  void init(EngineContext engineContext);

  void replay(TypedRecord record);

  ProcessingResult process(TypedRecord record, ProcessingContext processingContext);

  ProcessingResult onProcessingError(
      Throwable processingException,
      TypedRecord record,
      long position,
      ErrorHandlingContext errorHandlingContext);
}