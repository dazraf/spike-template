package io.dev.data.generator;

import org.junit.Test;

import java.io.IOException;

public class DataWrapperGeneratorTest {

  @Test
  public void thatWeCanGenerateASimpleObjectType() throws JsonObjectTypeFactory.FieldMissingException, IOException, JsonObjectTypeFactory.UnknownFieldType {
    final JsonObjectTypeFactory dataWrapperGenerator = new JsonObjectTypeFactory("json-examples/");
    final JsonTypeDefinition typeDefinition = dataWrapperGenerator.createTypeDefinition("transaction.json");
  }
}
