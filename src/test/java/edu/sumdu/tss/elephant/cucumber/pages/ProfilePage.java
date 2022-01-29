package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProfilePage {
    @FindBy(xpath = "//button[@class='w-100 btn btn-lg btn-outline-primary']")
    private List<WebElement> upgradeButtons;

    @FindBy(id = "public-api")
    private WebElement publicKey;

    @FindBy(id = "private-api")
    private WebElement privateKey;

    @FindBy(id = "db-password")
    private WebElement dbPasswordInput;

    @FindBy(xpath = "/html/body/div/div/main/div[4]/form/div[2]/button")
    private WebElement changePasswordButton;

    @FindBy(xpath = "//button[text()='Re-generate']")
    private WebElement changeApiKeys;

    private WebDriver webDriver;

    public ProfilePage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
        this.webDriver = driverContext.getWebDriver();
    }

    public void areKeysShowed() {
        assertTrue(publicKey.isDisplayed());
        assertTrue(privateKey.isDisplayed());
    }

    public void clickBasicButton() {
        makeClick(upgradeButtons.get(0));
    }

    public void clickProButton() {
        makeClick(upgradeButtons.get(1));
    }

    public void clickChangeDbPasswordButton() {
        makeClick(changePasswordButton);
    }

    public void enterDbPassword(String password) {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView();", changePasswordButton);
        changePasswordButton.sendKeys(password);
    }

    public void regenerateApisAndCheckIfItChanged() {
        String publicApi = publicKey.getDomAttribute("value");
        String privateApi = privateKey.getDomAttribute("value");

        makeClick(changeApiKeys);

        assertNotEquals(publicApi, publicKey.getDomAttribute("value"));
        assertNotEquals(privateApi, privateKey.getDomAttribute("value"));
    }

    private void makeClick(WebElement element) {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", element);
    }
}
