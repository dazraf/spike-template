package io.dev.data.generator;

import static java.security.MessageDigest.getInstance;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

public abstract class Hashable {
  public abstract String hash();

  protected String hash(String data) {
    try {
      byte[] bytes = getInstance("SHA-256").digest(data.getBytes("UTF-8"));
      return printHexBinary(bytes);
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }
}
