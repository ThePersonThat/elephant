package edu.sumdu.tss.elephant.cucumber.steps.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebDriverContext {
    private WebDriver webDriver;

    @Before
    public void setupDriver() {
        System.out.println("Initialize the webdriver...");
        webDriver = getBrowser();
    }

    @After
    public void closeDriver() {
        webDriver.quit();
        System.out.println("The webdriver has been stopped");
    }

    private WebDriver getBrowser() {
        if (webDriver != null) {
            return webDriver;
        }

        String browser = System.getProperty("browser");
        String res = System.getProperty("resolution");
        WebDriver driver;

        if (browser == null && res == null) {
            WebDriverManager.chromedriver().browserVersion("95").setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--kiosk");

            return new ChromeDriver(options);
        }

        String[] resolution = res.split("x");
        int width = Integer.parseInt(resolution[0]);
        int height = Integer.parseInt(resolution[1]);

        switch (browser) {
            case "chrome" -> driver = getChromeDriver();
            case "firefox" -> driver = getFirefoxDriver();
            case "edge" -> driver = getEdgeDriver();
            default -> throw new IllegalStateException("browser not found");
        }

        driver.manage().window().setSize(new Dimension(width, height));

        return driver;
    }

    private WebDriver getChromeDriver() {
        WebDriverManager.chromedriver().browserVersion("95").setup();

        return new ChromeDriver();
    }

    private WebDriver getFirefoxDriver() {
        WebDriverManager.firefoxdriver().browserVersion("92").setup();


        return new FirefoxDriver();
    }

    private WebDriver getEdgeDriver() {
        WebDriverManager.edgedriver().browserVersion("95").setup();

        return new EdgeDriver();
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }
}
