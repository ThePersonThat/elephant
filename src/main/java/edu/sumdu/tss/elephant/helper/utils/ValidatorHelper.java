package edu.sumdu.tss.elephant.helper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorHelper {
    /**
     * Validate user password
     *
     * @param password to validate
     * @return is password valid?
     * @author https://java2blog.com/validate-password-java/
     */
    public static boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-z0-9]).{8,20}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isValidMail(String mail) {
        Pattern pattern = Pattern.compile("^.+@.+$");
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }
}
