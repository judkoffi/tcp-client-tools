package fr.tcp.client;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Helper {
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
}
