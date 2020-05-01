package fr.tcp.client;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Helper {
  public static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
  public static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
  public static final String NUMBER = "0123456789";
  public static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
  public static final int BUFFER_SIZE = 1024;
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


  @SuppressWarnings("serial")
  public static final HashMap<String, Map.Entry<String, Boolean>> DEFAULT_ARGUMENTS =
      new HashMap<>() {
        {
          put("host", new AbstractMap.SimpleEntry<String, Boolean>("serveur address", true));
          put("port", new AbstractMap.SimpleEntry<String, Boolean>("serveur port", true));
          put("numberOfElements",
              new AbstractMap.SimpleEntry<String, Boolean>("number of element", true));

          put("timeout", new AbstractMap.SimpleEntry<String, Boolean>("timout per request", true));
          put("length", new AbstractMap.SimpleEntry<String, Boolean>("max value", false));
        }
      };

  public static Options buildOptions(HashMap<String, Map.Entry<String, Boolean>> arguments) {
    var r = new Options();
    arguments//
      .entrySet()
      .stream()
      .map(m ->
      {
        var option = new Option("" + m.getKey().charAt(0), m.getKey(), true, m.getValue().getKey());
        option.setRequired(m.getValue().getValue());
        return option;
      })
      .forEach(r::addOption);
    return r;
  }

  public static String generateRandomString(int length) {
    var random = ThreadLocalRandom.current();
    if (length < 1)
      throw new IllegalArgumentException(length + " must be gretther than 0");

    var sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
      char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
      sb.append(rndChar);
    }
    return sb.toString();
  }
}
