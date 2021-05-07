package utils;

public class StringUtils {

    public static String getPhoneNumb(String string) {
        if (string == null || string.length() <= 1) {
            return "";
        }
        String val = string.charAt(0) == '7' ? string.substring(1) : string;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < val.length(); i++) {
            if (Character.isDigit(val.charAt(i))) {
                builder.append(val.charAt(i));
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
