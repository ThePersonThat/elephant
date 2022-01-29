package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.jupiter.api.Assertions.*;

public class ResetPasswordPage {
    @FindBy(tagName = "form")
    private WebElement form;

    @FindBy(xpath = "/html/body/main/div/div/div/form/div[2]/button")
    private WebElement sendLinkButton;

    @FindBy(id = "web-email")
    private WebElement emailInput;

    @FindBy(xpath = "/html/body/main/div/div/div/form/h3")
    private WebElement chooseNewPasswordLabel;

    @FindBy(id = "web-password")
    private WebElement passwordInput;

    @FindBy(id = "web-password-c")
    private WebElement passwordConfirmationInput;

    @FindBy(xpath = "/html/body/main/div/div/div/form/div[3]/button")
    private WebElement changePasswordButton;

    public ResetPasswordPage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
    }

    public void checkIfResetPasswordPageIsShowed() {
        assertTrue(form.isDisplayed());
        assertEquals("/login/reset-password", form.getDomAttribute("action"));
    }

    public void fillResetForm(String email) {
        emailInput.sendKeys(email);
    }

    public void clickSendLinkButton() {
        sendLinkButton.click();
    }

    public void checkIfResetPageIsShowed() {
        assertTrue(chooseNewPasswordLabel.isDisplayed());
    }

    public void fillResetPasswordForm(String password, String confirmationPassword) {
        passwordInput.sendKeys(password);
        passwordConfirmationInput.sendKeys(confirmationPassword);
    }

    public void clickChangePasswordButton() {
        changePasswordButton.click();
    }
}
