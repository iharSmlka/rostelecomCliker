package selenium_utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public interface SeleniumClient {
    SeleniumClient get(String url);
    SeleniumClient focus(WebElement element);
    SeleniumClient focus(By by);
    SeleniumClient unFocus();
    SeleniumClient scrollToFocus();
    SeleniumClient scrollToBottom();
    SeleniumClient submitFocus();
    SeleniumClient clickOnFocus();
    SeleniumClient sleepSecs(int sec);
    SeleniumClient sendKeysToFocus(String keys);
    SeleniumClient clearFocus();
    WebElement getFocus();
    WebElement getElementFromFocus(By by);
    List<WebElement> getElementsFromFocus(By by);
    void close();
}
