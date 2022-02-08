package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.Assert.assertTrue;

public class ScriptsPage {
    @FindBy(xpath = "//button[text()='Upload new script']")
    private WebElement uploadButton;

    @FindBy(xpath = "//input[@type='file']")
    private WebElement chooseFileButton;

    public ScriptsPage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
    }

    public void checkIfPageIsShowed() {
        assertTrue(uploadButton.isDisplayed());
    }

    public void setFile(String filepath) {
        chooseFileButton.sendKeys(filepath);
    }

    public void clickUploadButton() {
        uploadButton.click();
    }
}
