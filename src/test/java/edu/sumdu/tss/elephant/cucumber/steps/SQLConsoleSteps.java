package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.SQLConsolePage;
import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import io.cucumber.java.en.Then;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class SQLConsoleSteps {
    private final SQLConsolePage page;
    private WebDriver webDriver;



    public SQLConsoleSteps(SQLConsolePage page, WebDriverContext context) {
        this.page = page;
        this.webDriver = context.getWebDriver();
    }

    @Then("the SQL Console page is showed up")
    public void checkIfSQLConsolePageIsShowed() {
        page.checkIfPageIsShowed();
    }

    @Then("user enters {string} as query")
    public void userEntersAsQuery(String query) {
        page.changeQuery(query);
    }

    @Then("user clicks run button")
    public void clickUploadButton() {
        page.clickRunButton();
    }

    @Then("limit message is showed up")
    public void theLimitMessageIsShowedUp() {
        page.checkIfLimitMessageIsShowedUp();
    }





}
