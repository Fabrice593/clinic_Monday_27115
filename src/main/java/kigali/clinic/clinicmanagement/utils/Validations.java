package kigali.clinic.clinicmanagement.utils;

import java.math.BigInteger;
import java.util.regex.Pattern;

public final class Validations {

    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern INTEGER_TEXT = Pattern.compile("-?\\d+");

    private Validations() {
    }

    public static void ensure(boolean condition, String violationMessage) {
        if (!condition) {
            throw ApiResponses.badRequest(violationMessage);
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String normalize(String value) {
        return value == null ? null : WHITESPACE_RUN.matcher(value.trim()).replaceAll(" ");
    }

    public static boolean containsDigit(String value) {
        return value != null && HAS_DIGIT.matcher(value).matches();
    }

    public static boolean isNotPositiveNumber(String value) {
        return value != null
                && INTEGER_TEXT.matcher(value).matches()
                && new BigInteger(value).signum() <= 0;
    }
}
