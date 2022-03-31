package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.Assert.assertTrue;

public class SQLConsolePage {
    @FindBy(xpath = "//button[text()='Run']")
    private WebElement runButton;

    @FindBy(xpath = "//div[@id='editor']")
    private WebElement consoleLine;

    @FindBy(xpath = "//strong[@style='color: red;']")
    private WebElement limitMessage;


    private WebDriver webDriver;

    public SQLConsolePage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
        this.webDriver = driverContext.getWebDriver();
    }

    public void checkIfPageIsShowed() {
        assertTrue(runButton.isDisplayed());
    }

    public void checkIfLimitMessageIsShowedUp() {
        assertTrue(limitMessage.isDisplayed());
    }

    public void clickRunButton() {
        runButton.click();
    }

    public void changeQuery(String query) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("editor.setValue('" + query + "')");
    }

}
