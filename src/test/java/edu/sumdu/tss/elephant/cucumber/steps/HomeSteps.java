package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.HomePage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class HomeSteps {
    private final HomePage page;

    public HomeSteps(HomePage page) {
        this.page = page;
    }

    @Then("the home page is showed up")
    public void homePageIsShowedUp() {
        page.checkIfHomePageIsShowed();
    }

    @When("user logouts")
    public void userLogout() {
        page.logout();
    }

    @When("user clicks the resend button")
    public void resendButton() {
        page.clickResendButton();
    }

    @Then("the database create button is {string}")
    public void checkCreateDbButton(String property) {
        page.isCreateDatabaseHasProperty(property);
    }

    @Then("should show the greeting block")
    public void checkIfGreetingBlockIsShowed() {
        page.isGreetingBlockShowed();
    }

    @Then("user should have {int} databases")
    public void checkCountDatabases(int count) {
        page.checkCountDatabase(count);
    }

    @When("user clicks the create new database button")
    public void clickCreateDatabaseButton() {
        page.clickCreateNewDbButton();
    }

    @When("user deletes the {int} database")
    public void clickDeleteDatabaseButton(int index) {
        page.deleteDatabase(index - 1);
    }

    @When("user go to the {int} database")
    public void userGoToDatabase(int index) {
        page.goToDatabase(index - 1);
    }
}
