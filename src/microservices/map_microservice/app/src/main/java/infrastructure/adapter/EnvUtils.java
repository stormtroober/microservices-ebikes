package infrastructure.adapter;

import java.util.function.Function;

public class EnvUtils {

    public static <T> T getEnvOrDefault(String key, T defaultValue, Function<String, T> parser) {
        String value = System.getenv(key);
        return value != null ? parser.apply(value) : defaultValue;
    }

    public static int getEnvOrDefaultInt(String key, int defaultValue) {
        return getEnvOrDefault(key, defaultValue, Integer::parseInt);
    }

    public static String getEnvOrDefaultString(String key, String defaultValue) {
        return getEnvOrDefault(key, defaultValue, Function.identity());
    }
}