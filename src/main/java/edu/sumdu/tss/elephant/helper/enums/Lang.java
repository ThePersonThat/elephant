package edu.sumdu.tss.elephant.helper.enums;

public enum Lang {
    EN, UK;

    public static Lang byValue(String value) {
        for (Lang lang : Lang.values()) {
            if (lang.name().equalsIgnoreCase(value)) {
                return lang;
            }
        }
        throw new RuntimeException("Language not found for" + value);
    }

}
