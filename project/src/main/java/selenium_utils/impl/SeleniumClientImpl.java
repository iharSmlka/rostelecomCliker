package selenium_utils.impl;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import selenium_utils.SeleniumClient;

import java.util.List;

public class SeleniumClientImpl implements SeleniumClient {

    private final WebDriver driver;
    private WebElement focus;

    public SeleniumClientImpl() {
        driver = WebDriverCreator.getInstance().createNewWebDriverInstance();
    }

    public SeleniumClient get(String url) {
        driver.get(url);
        return this;
    }

    @Override
    public SeleniumClient focus(WebElement element) {
        focus = element;
        return this;
    }

    public SeleniumClient focus(By by) {
        focus = focus == null ? driver.findElement(by) : focus.findElement(by);
        return this;
    }

    public SeleniumClient unFocus() {
        focus = null;
        return this;
    }

    public SeleniumClient scrollToFocus() {
        if (focus == null) {
            throw new IllegalStateException("Невозможно проскроллить: фокуса нет");
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", focus);
        return this;
    }

    @Override
    public SeleniumClient scrollToBottom() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, Math.max(document.documentElement.scrollHeight, document.body.scrollHeight, document.documentElement.clientHeight));");
        return this;
    }

    @Override
    public SeleniumClient submitFocus() {
        if (focus == null) {
            throw new IllegalStateException("Невозможно submit-нуть: фокуса нет");
        }
        focus.submit();
        return this;
    }

    public SeleniumClient clickOnFocus() {
        if (focus == null) {
            throw new IllegalStateException("Невозможно кликнуть: фокуса нет");
        }
        focus.click();
        return this;
    }

    public SeleniumClient sleepSecs(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException ignore) {};
        return this;
    }

    @Override
    public SeleniumClient sendKeysToFocus(String keys) {
        if (focus == null) {
            throw new IllegalStateException("Невозможно отправить строку: фокуса нет");
        }
        focus.sendKeys(keys);
        return this;
    }

    public WebElement getFocus() {
        return focus;
    }

    public WebElement getElementFromFocus(By by) {
        return focus != null ? focus.findElement(by) : driver.findElement(by);
    }

    public List<WebElement> getElementsFromFocus(By by) {
        return focus != null ? focus.findElements(by) : driver.findElements(by);
    }

    @Override
    public void close() {
        unFocus();
        driver.quit();
    }
}
