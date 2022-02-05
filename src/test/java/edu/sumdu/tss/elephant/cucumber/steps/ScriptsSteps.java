package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.ScriptsPage;
import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

public class ScriptsSteps {
    private final ScriptsPage page;
    private WebDriver webDriver;

    public ScriptsSteps(ScriptsPage page, WebDriverContext context) {
        this.page = page;
        this.webDriver = context.getWebDriver();
    }

    @Then("the scripts page is showed up")
    public void checkIfScriptsPageIsShowed() {
        page.checkIfPageIsShowed();
    }

    @Then("user uploads {string} file")
    public void uploadFile(String filepath) {
        String path = getClass().getClassLoader().getResource(filepath).getPath();
        page.setFile(path);
    }

    @Then("user clicks upload button")
    public void clickUploadButton() {
        page.clickUploadButton();
    }
}