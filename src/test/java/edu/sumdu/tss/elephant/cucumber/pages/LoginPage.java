package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginPage {
    @FindBy(xpath = "//*[@id=\"floatingInput\"]")
    private WebElement emailInput;

    @FindBy(xpath = "//*[@id=\"floatingPassword\"]")
    private WebElement passwordInput;

    @FindBy(xpath = "/html/body/main/div[1]/div/div[2]/form/button")
    private WebElement loginButton;

    @FindBy(tagName = "form")
    private WebElement form;

    @FindBy(xpath = "/html/body/main/div[1]/div/div[2]/form/p/a")
    private WebElement forgotPasswordButton;

    public LoginPage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
    }

    public void fillLoginForm(String email, String password) {
        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
    }

    public void clickLoginButton() {
        loginButton.click();
    }

    public void checkIfLoginPageIsShowed() {
        assertTrue(form.isDisplayed());
        assertEquals("login", form.getDomAttribute("action"));
    }

    public void clickForgotPasswordButton() {
        forgotPasswordButton.click();
    }
}
