package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import static org.junit.jupiter.api.Assertions.*;

public class RegistrationPage {
    @FindBy(tagName = "form")
    private WebElement form;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "conformation")
    private WebElement confirmationInput;

    @FindBy(xpath = "/html/body/main/div/div/div[2]/form/button")
    private WebElement singUpButton;

    @FindBy(id = "basic-addon2")
    private WebElement hidingButton;

    public RegistrationPage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
    }

    public void checkIfRegistrationPageIsShowed() {
        assertTrue(form.isDisplayed());
        assertEquals("/registration", form.getDomAttribute("action"));
    }

    public void fillRegistrationForm(String email, String password, String confirmationPassword) {
        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        confirmationInput.sendKeys(confirmationPassword);
    }

    public void clickSingUpButton() {
        singUpButton.click();
    }

    public void checkIfPasswordErrorIsShowed() {
        checkIfErrorIsShowed(confirmationInput);
    }

    public void checkIfEmailErrorIsShowed() {
        checkIfErrorIsShowed(emailInput);
    }

    public void checkIfPasswordHidden(String type) {
        assertEquals(type, passwordInput.getDomAttribute("type"));
    }

    public void clickHidingButton() {
        hidingButton.click();
    }

    private void checkIfErrorIsShowed(WebElement webElement) {
        assertFalse(webElement.getAttribute("validationMessage").isEmpty());
    }
}
