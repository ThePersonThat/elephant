package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.ResetPasswordPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ResetPasswordSteps {
    private final ResetPasswordPage page;

    public ResetPasswordSteps(ResetPasswordPage page) {
        this.page = page;
    }

    @Then("the reset-password page is showed up")
    public void resetPasswordPageIsShowedUp() {
        page.checkIfResetPasswordPageIsShowed();
    }

    @When("^user enters \"([^\"]*)\" as email$")
    public void userEntersAsEmail(String email) {
        page.fillResetForm(email);
    }

    @When("click the send link button")
    public void clickSendLinkButton() {
        page.clickSendLinkButton();
    }

    @Then("the reset page is showed up")
    public void resetPageIsShowed() {
        page.checkIfResetPageIsShowed();
    }

    @When("user enters {string} as password and {string} as confirmation password")
    public void userEntersAsPasswordAndAsConfirmationPassword(String password, String confirmationPassword) {
        page.fillResetPasswordForm(password, confirmationPassword);
    }

    @When("user clicks the change password button")
    public void clickChangePasswordButton() {
        page.clickChangePasswordButton();
    }
}
