package utils;

import org.openqa.selenium.WebElement;

public class SeleniumUtils {

    public static String getWebElementText(WebElement element) {
        return element.getAttribute("innerHTML");
    }
}
