package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.RegistrationPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RegistrationSteps {
    private final RegistrationPage page;

    public RegistrationSteps(RegistrationPage page) {
        this.page = page;
    }

    @Then("the registration page is showed up")
    public void registrationPageIsShowedUp() {
        page.checkIfRegistrationPageIsShowed();
    }

    @When("^user enters \"([^\"]*)\" as email and \"([^\"]*)\" as password and \"([^\"]*)\" as confirmation password$")
    public void userEntersEmailAndPasswordAndConfirmationPassword(String email, String password, String confirmationPassword) {
        page.fillRegistrationForm(email, password, confirmationPassword);
    }

    @When("user clicks the sing up button")
    public void clickSingUpButton() {
        page.clickSingUpButton();
    }

    @Then("should show the email error message")
    public void shouldShowEmailErrorMessage() {
        page.checkIfEmailErrorIsShowed();
    }

    @Then("should show the confirmation error message")
    public void shouldShowConfirmationErrorMessage() {
        page.checkIfPasswordErrorIsShowed();
    }

    @Then("the password should be {word}")
    public void passwordShouldBeHidden(String word) {
        String type = word.equals("hidden") ? "password" : "text";
        page.checkIfPasswordHidden(type);
    }

    @When("user clicks the hiding button")
    public void clickHidingButton() {
        page.clickHidingButton();
    }
}
