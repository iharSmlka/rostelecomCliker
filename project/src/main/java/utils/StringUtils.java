package utils;

public class StringUtils {

    public static String getOnlyNumbs(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (Character.isDigit(string.charAt(i))) {
                builder.append(string.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String getLastFourNumbs(String string) {
        if (string == null) {
            return null;
        }
        if (string.length() <= 4) {
            return string;
        }
        return string.substring(string.length() - 4);
    }
}
