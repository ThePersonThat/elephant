package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.ProfilePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProfileSteps {
    private ProfilePage page;

    public ProfileSteps(ProfilePage page) {
        this.page = page;
    }

    @Given("user upgrades to {string} level")
    public void upgradeUser(String level) {
        if (level.equals("pro")) {
            page.clickProButton();
        } else {
            page.clickBasicButton();
        }
    }

    @Then("the public key and private key should be showed")
    public void checkIfKeysAreShowed() {
        page.areKeysShowed();
    }

    @When("user enters the {string} as database password")
    public void enterDatabasePassword(String password) {
        page.enterDbPassword(password);
    }

    @When("user clicks the change database password button")
    public void clickChangeDbPasswordButton() {
        page.clickChangeDbPasswordButton();
    }

    @Then("user regenerates api keys")
    public void regenerateApiKeys() {
        page.regenerateApisAndCheckIfItChanged();
    }
}
