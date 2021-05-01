package selenium_utils.impl;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;

public class WebDriverCreator {

    private static final WebDriverCreator instance = new WebDriverCreator();
    private final List<WebDriver> webDrivers = new ArrayList<>();

    private WebDriverCreator() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
    }

    public static WebDriverCreator getInstance() {
        return instance;
    }

    public WebDriver createNewWebDriverInstance() {
        WebDriver driver = new ChromeDriver();
        webDrivers.add(driver);
        return driver;
    }

    public void closeAllDrivers() {
        for (WebDriver driver : webDrivers) {
            driver.quit();
        }
    }
}
