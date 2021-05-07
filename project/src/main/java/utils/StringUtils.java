package utils;

public class StringUtils {

    public static String getPhoneNumb(String string) {
        if (string == null || string.length() <= 1) {
            return "";
        }
        String val = getOnlyNumbs(string);
        return val.charAt(0) == '7' ? val.substring(1) : val;
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

    private static String getOnlyNumbs(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                builder.append(str.charAt(i));
            }
        }
        return builder.toString();
    }
}
