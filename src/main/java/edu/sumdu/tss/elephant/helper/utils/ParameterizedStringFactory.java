package edu.sumdu.tss.elephant.helper.utils;

import java.util.HashMap;
import java.util.Map;

public record ParameterizedStringFactory(String template) {

    public ParameterizedString addParameter(String key, String value) {
        ParameterizedString query = new ParameterizedString(template);
        return query.addParameter(key, value);
    }

    @Override
    public String toString() {
        return template;
    }

    public static class ParameterizedString {
        private final HashMap<String, String> params = new HashMap<>();
        final String template;

        public ParameterizedString(String template) {
            this.template = template;
        }

        public ParameterizedString addParameter(String key, String value) {
            params.put(key, value);
            return this;
        }

        //TODO: bug :test and :test1 - not a same key!
        @Override
        public String toString() {
            String result = template;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replaceAll(":" + entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
}

