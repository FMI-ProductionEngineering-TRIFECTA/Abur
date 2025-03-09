package ro.unibuc.hello.utils;

import java.util.HashMap;
import java.util.Map;

public class SeederUtils {

    private static final Map<String, Integer> counters = new HashMap<String, Integer>();
    private static final Map<String, String> templates = new HashMap<>();

    public static void setTemplate(String key, String template) {
        counters.put(key, 0);
        templates.put(key, template + "%s");
    }

    public static String getId(String key, Integer at) {
        return String.format(templates.get(key), at);
    }

    public static String getNewId(String key) {
        if (!counters.containsKey(key)) {
            throw new RuntimeException("Counter not found");
        }
        Integer counter = counters.get(key);
        counters.put(key, counter + 1);
        return String.format(templates.get(key), counter);
    }

}
