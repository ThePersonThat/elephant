package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.jupiter.api.Assertions.*;

public class BasePage {
    @FindBy(className = "alert")
    private WebElement blockMessage;

    @FindBy(id = "email")
    private WebElement emailInput;

    private WebDriver webDriver;

    public BasePage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
        this.webDriver = driverContext.getWebDriver();
    }

    public void checkIfMessageIsShowed(String message) {
        assertTrue(blockMessage.isDisplayed());
        assertEquals(message, blockMessage.getText());
    }

    public void checkIfLabelIsShowed(String text) {
        String xpath = String.format("//*[text()='%s']", text);
        webDriver.findElement(By.xpath(xpath));
    }
}
