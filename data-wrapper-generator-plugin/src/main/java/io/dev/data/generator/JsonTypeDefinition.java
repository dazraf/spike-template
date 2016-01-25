package io.dev.data.generator;

import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class JsonTypeDefinition extends Hashable {
  private final List<Field> fields = new LinkedList<>();
  private volatile String hash;

  public void addField(Field field) {
    this.fields.add(field);
  }

  @Override
  public String hash() {
    if (hash == null) {
      // compute the merkle hash
      hash = hash(fields.stream().map(Field::hash).collect(joining()));
    }
    return hash;
  }
}
