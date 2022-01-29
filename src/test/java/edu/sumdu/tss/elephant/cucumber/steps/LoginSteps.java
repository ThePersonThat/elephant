package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class LoginSteps {
    private final LoginPage page;

    public LoginSteps(LoginPage page) {
        this.page = page;
    }

    @When("user enters {string} as email and {string} as password")
    public void userEntersAsEmailAndAsPassword(String email, String password) {
        page.fillLoginForm(email, password);
    }

    @When("user clicks the login button")
    public void userClicksLoginButton() {
        page.clickLoginButton();
    }

    @Given("the login page is showed up")
    public void theLoginPageIsShowedUp() {
        page.checkIfLoginPageIsShowed();
    }

    @When("user clicks the forgot password button")
    public void userClicksForgotPasswordButton() {
        page.clickForgotPasswordButton();
    }
}
