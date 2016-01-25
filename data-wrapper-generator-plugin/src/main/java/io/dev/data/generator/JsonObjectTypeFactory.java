package io.dev.data.generator;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class JsonObjectTypeFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JsonObjectTypeFactory.class);

  private final ConcurrentHashMap<String, JsonTypeDefinition> types = new ConcurrentHashMap<>();
  private static final String TYPE_FIELD = "_type";
  private final String rootClassPath;

  /**
   *
   * @param rootClassPath must not begin with / and must end with /
   */
  public JsonObjectTypeFactory(String rootClassPath) {
    if (rootClassPath.startsWith("/")) {
      throw new RuntimeException("path must not begin with /");
    }

    if (!rootClassPath.endsWith("/")) {
      throw new RuntimeException("path must end with /");
    }

    this.rootClassPath = rootClassPath;
  }

  public JsonTypeDefinition createTypeDefinition(String exampleFileName) throws IOException, FieldMissingException, UnknownFieldType {
    String result = readResourceFile(exampleFileName);
    JsonObject example = new JsonObject(result);
    return createTypeDefinition(example);
  }

  private JsonTypeDefinition createJsonTypeDefinition(JsonObject exampleJson) throws JsonObjectTypeFactory.UnknownFieldType {
    final JsonTypeDefinition typeDefinition = createTypeDefinition(exampleJson);
    final String hash = typeDefinition.hash();
    return types.computeIfAbsent(hash, h -> typeDefinition);
  }

  private JsonTypeDefinition createTypeDefinition(JsonObject example) throws UnknownFieldType {
    final JsonTypeDefinition jsonTypeDefinition = new JsonTypeDefinition();
    for (String fieldName : example.fieldNames()) {
      if (isProcessableField(fieldName)) {
        processField(example, fieldName, jsonTypeDefinition);
      }
    }
    return jsonTypeDefinition;
  }

  private boolean isProcessableField(String fieldName) {
    return !fieldName.equals(TYPE_FIELD);
  }

  private void processField(JsonObject example, String fieldName, JsonTypeDefinition jsonTypeDefinition) throws UnknownFieldType {
    if (isInstantField(example, fieldName)) {

      jsonTypeDefinition.addField(new InstantField(fieldName));

    } else if (isDoubleField(example, fieldName)) {

      jsonTypeDefinition.addField(new DoubleField(fieldName));

    } else if (isIntegerField(example, fieldName)) {

      jsonTypeDefinition.addField(new IntegerField(fieldName));

    } else if (isBooleanField(example, fieldName)) {

      jsonTypeDefinition.addField(new BooleanField(fieldName));

    } else if (isJsonObjectField(example, fieldName)) {

      processJsonObjectField(example, fieldName, jsonTypeDefinition);

    } else if (isJsonArrayField(example, fieldName)) {

      processJsonArrayField(example, fieldName, jsonTypeDefinition);

    } else if (isStringField(example, fieldName)) {

      jsonTypeDefinition.addField(new StringField(fieldName));

    } else {
      throw new UnknownFieldType(example, fieldName);
    }
  }

  private void processJsonArrayField(JsonObject example, String fieldName, JsonTypeDefinition jsonTypeDefinition) {
    // TBC
  }

  private boolean isJsonArrayField(JsonObject example, String fieldName) {
    try {
      Object value = example.getValue(fieldName);
      return (value instanceof JsonArray);
    } catch (Throwable throwable) {
      return false;
    }
  }

  private void processJsonObjectField(JsonObject example, String fieldName, JsonTypeDefinition jsonTypeDefinition) throws UnknownFieldType {
    JsonObject child = example.getJsonObject(fieldName);
    createJsonTypeDefinition(child);
  }

  private boolean isStringField(JsonObject example, String fieldName) {
    try {
      return example.getValue(fieldName) instanceof String;
    } catch (Throwable throwable) {
      return false;
    }
  }

  private boolean isJsonObjectField(JsonObject example, String fieldName) {
    try {
      return example.getValue(fieldName) instanceof JsonObject;
    } catch (Throwable throwable) {
      return false;
    }
  }

  private boolean isBooleanField(JsonObject example, String fieldName) {
    try {
      return example.getValue(fieldName) instanceof Boolean;
    } catch (Throwable throwable) {
      return false;
    }
  }

  private boolean isIntegerField(JsonObject example, String fieldName) {
    try {
      return example.getValue(fieldName) instanceof Integer;
    } catch (Throwable throwable) {
      return false;
    }
  }

  private boolean isDoubleField(JsonObject example, String fieldName) {
    try {
      Object value = example.getValue(fieldName);
      return (value instanceof Double) || (value instanceof Float);
    } catch (Throwable throwable) {
      return false;
    }
  }

  private boolean isInstantField(JsonObject example, String fieldName) {
    try {
      example.getInstant(fieldName);
      return true;
    } catch (Throwable throwable) {
      return false;
    }
  }

  private String readResourceFile(String file) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(rootClassPath + file)) {
      return readWholeInputStream(is);
    }
  }

  private String readWholeInputStream(InputStream is) {
    return new Scanner(is).useDelimiter("\\Z").next();
  }

  private <T> T getRequiredField(JsonObject json, String fieldName, Class<T> clazz) throws FieldMissingException {
    final Object value = json.getValue(fieldName);
    if (value == null || !clazz.isInstance(value)) {
      throw new FieldMissingException(fieldName, clazz);
    }
    return clazz.cast(value);
  }


  public class FieldMissingException extends Exception {
    public FieldMissingException(String fieldName, Class clazz) {
      super("Field " + fieldName + " of type " + clazz.getName() + " missing");
    }
  }

  public class UnknownFieldType extends Exception {
    public UnknownFieldType(JsonObject jsonObject, String fieldName) {
      super("Unknown field type " + fieldName + ": " + jsonObject.encodePrettily());
    }
  }
}
