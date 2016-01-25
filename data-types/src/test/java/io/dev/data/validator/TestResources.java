package io.dev.data.validator;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.stream.Stream;

public class TestResources {
  private static final Logger LOG = LoggerFactory.getLogger(TestResources.class);

  @Test
  public void readContents() throws IOException, URISyntaxException {
    String path = "json-schema/";
    final String[] resourceListing = JsonSchemaValidator.getResourceListing(JsonSchemaValidator.class, path);

    Stream.of(resourceListing).map(s -> path + s).forEach(s -> {
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(s)) {
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        LOG.info(rawSchema.toString(2));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
