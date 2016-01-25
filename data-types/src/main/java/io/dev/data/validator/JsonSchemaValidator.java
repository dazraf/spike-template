package io.dev.data.validator;

import io.vertx.core.json.JsonObject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JsonSchemaValidator {
  private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaValidator.class);
  private static final Map<String, Schema> schemas = new HashMap<>();

  {
//    try (InputStream inputStream = getClass().getResourceAsStream("json-schema/transaction.schema.json")) {
//      JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
//      Schema schema = SchemaLoader.load(rawSchema);
//      schema.validate(new JSONObject("{\"hello\" : \"world\"}")); // throws a ValidationException if this object is invalid
//    }
  }

  public static void init() {
    schemas.clear();
    String path = "json-schema/";
    try {
      Stream.of(getResourceListing(JsonSchemaValidator.class, path)).map(s -> path + s).forEach(s -> {
        try (InputStream inputStream = JsonSchemaValidator.class.getResourceAsStream(s)) {
          JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
          Schema schema = SchemaLoader.load(rawSchema);
          if (!rawSchema.getJSONObject("properties").has("type")) {
            throw new RuntimeException("schema file " + s + " doesn't have a 'type' property");
          }
          schemas.put(schema.getTitle(), schema);
        } catch (IOException e) {
          LOG.error("Failed to read json schema " + s, e);
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      LOG.error("Failed to read json schemas", e);
      throw new RuntimeException(e);
    }
  }

  static void validate(JsonObject jsonObject) throws Exception {
    if (jsonObject == null) {
      throw new Exception("jsonObject is null");
    }

    Object type = jsonObject.getValue("type");
    if (type == null || !(type instanceof String)) {
      throw new Exception("JSON object does not have a 'type' string property:\n" + jsonObject.toString());
    }
    String stringType = (String)type;
    Schema schema = schemas.get(stringType);
    if (schema == null) {
      throw new Exception("unknown schema type: " + stringType);
    }
    try {
      schema.validate(new JSONTokener(jsonObject.toString()));
    } catch (Throwable throwable) {
      throw new Exception("failed to validate json: " + jsonObject.toString(), throwable);
    }
  }

  static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
    URL dirURL = clazz.getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
        /* A file path: easy enough */
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
        /*
         * In case of a jar file, we can't actually find a directory.
         * Have to assume the same jar as clazz.
         */
      String me = clazz.getName().replace(".", "/") + ".class";
      dirURL = clazz.getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
        /* A JAR path */
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
      Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) { //filter according to the path
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory name
            entry = entry.substring(0, checkSubdir);
          }
          result.add(entry);
        }
      }
      return result.toArray(new String[result.size()]);
    } else if (dirURL.getProtocol().equals("file")) {
      return new File(dirURL.toURI()).list();
    }

    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
  }
}
