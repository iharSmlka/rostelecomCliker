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
}
