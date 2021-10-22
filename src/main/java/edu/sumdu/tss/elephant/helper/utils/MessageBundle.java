/**
 * File copied from
 * https://github.com/tipsy/javalin-website-example/blob/master/src/main/java/app/util/MessageBundle.java
 *
 * @author Tipsy davidaase@hotmail.com
 */
package edu.sumdu.tss.elephant.helper.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageBundle {

    private final ResourceBundle messages;

    public MessageBundle(String languageTag) {
        Locale locale = languageTag != null ? new Locale(languageTag) : Locale.ENGLISH;
        this.messages = ResourceBundle.getBundle("localization/messages", locale);
    }

    public String get(String message) {
        return messages.getString(message);
    }

    public final String get(final String key, final Object... args) {
        return MessageFormat.format(get(key), args);
    }

}