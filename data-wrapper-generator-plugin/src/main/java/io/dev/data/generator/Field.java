package io.dev.data.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Field extends Hashable {
  private static final Logger LOG = LoggerFactory.getLogger(Field.class);
  private volatile String hash;
  private final String fieldName;

  public Field(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String hash() {
    if (hash == null) {
      hash = hash(fieldName);
    }
    return hash;
  }
}
