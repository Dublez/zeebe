/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.journal;

public interface JournalMetaStore {

  /**
   * Update lastFlushedIndex in metastore. This method can be expensive and blocking as the
   * implementations of this may have to write to a database or a file.
   *
   * @param index last flushed index
   */
  void storeLastFlushedIndex(long index);

  /**
   * Read last flushed index from the metastore. This method might be expensive and blocking as the
   * implementations of this may have to read from a database or file. It is recommended for the
   * callers of this method to cache lastFlushedIndex and call this method only when necessary.
   *
   * @return last flushed index
   */
  long loadLastFlushedIndex();
}
