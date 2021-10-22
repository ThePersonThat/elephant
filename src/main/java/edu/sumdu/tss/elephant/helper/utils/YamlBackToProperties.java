/**
 * @author Edwin Grosmann
 * @see https://stackoverflow.com/questions/49207935/how-to-convert-yml-to-properties-with-a-gradle-task
 */
package edu.sumdu.tss.elephant.helper.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class YamlBackToProperties {

    public static String convert(String filename) {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(filename))) {
            TreeMap<String, Map<String, Object>> config = yaml.loadAs(in, TreeMap.class);
            return toProperties(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toProperties(TreeMap<String, Map<String, Object>> config) {
        StringBuilder sb = new StringBuilder();
        for (String key : config.keySet()) {
            sb.append(toString(key, config.get(key)));
        }
        return sb.toString();
    }

    private static String toString(String key, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (String mapKey : map.keySet()) {
            if (map.get(mapKey) instanceof Map) {
                sb.append(toString(String.format("%s.%s", key, mapKey), (Map<String, Object>) map.get(mapKey)));
            } else {
                sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
            }
        }
        return sb.toString();
    }
}