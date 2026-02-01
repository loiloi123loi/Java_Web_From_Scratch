package com.polime.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        String env = System.getProperty("app.env");
        if (env == null || env.isEmpty()) {
            env = System.getenv("APP_ENV");
        }
        if (env == null || env.isEmpty()) {
            env = "local";
        }
        System.out.println("-> Active Environment Config: " + env);

        loadYamlConfig(env);
    }

    @SuppressWarnings("unchecked")
    private void loadYamlConfig(String env) {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.yml")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.yml");
                return;
            }
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(input);

            flattenMap("", root, properties);

            if (root.containsKey("profiles")) {
                Map<String, Object> profiles = (Map<String, Object>) root.get("profiles");
                if (profiles.containsKey(env)) {
                    Map<String, Object> profileProps = (Map<String, Object>) profiles.get(env);
                    flattenMap("", profileProps, properties);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading application.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, Properties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.equals("profiles")) {
                continue;
            }

            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                flattenMap(fullKey, (Map<String, Object>) value, props);
            } else if (value != null) {
                props.put(fullKey, value.toString());
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        try {
            return (value != null) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLongProperty(String key, long defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
