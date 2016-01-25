package io.dev.data.generator;

public class JsonObjectField extends Field {
  private final JsonTypeDefinition typeDefinition;

  public JsonObjectField(String fieldName, JsonTypeDefinition typeDefinition) {
    super(fieldName);
    this.typeDefinition = typeDefinition;
  }

  @Override
  protected String hash(String data) {
    return typeDefinition.hash();
  }
}
