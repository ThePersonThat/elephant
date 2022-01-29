package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.BasePage;
import edu.sumdu.tss.elephant.cucumber.steps.hooks.Hooks;
import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import edu.sumdu.tss.elephant.integration.utils.EmailMessageManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.openqa.selenium.WebDriver;

import javax.mail.internet.MimeMessage;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseSteps {
    private final WebDriver webDriver;
    private final BasePage basePage;
    private String savedUrl;
    private int lastStatusCode;

    public BaseSteps(BasePage basePage, WebDriverContext driverContext) {
        this.webDriver = driverContext.getWebDriver();
        this.basePage = basePage;
    }

    @Then("should show {string} message")
    public void shouldShowMessage(String message) {
        basePage.checkIfMessageIsShowed(message);
    }

    @Given("user on the {string} page")
    public void givenUserOnPage(String url) {
        userGoToPage(url);
    }

    @When("user go to the {string} page")
    public void userGoToPage(String url) {
        webDriver.navigate().to(Hooks.getPath() + "/" + url);
    }

    @Then("the {string} page should be open")
    public void theLoginPageShouldBeOpen(String url) {
        assertEquals(Hooks.getPath() + "/" + url, webDriver.getCurrentUrl());
    }

    @Then("email {string} should contain {int} messages where the {int} message with subject {string}")
    public void theUserEmailShouldContainsMessageWithSubject(String email, int count, int indexMessage, String subject) {
        EmailMessageManager manager = Hooks.getMailManager();
        MimeMessage message = manager.getCountMessage(email, count)[indexMessage - 1];
        manager.checkMessage(message, subject);
    }

    @Then("go to url from email {string} by {int} message")
    public void goToUrlFromEmailByMessage(String email, int indexMessage) {
        String url = getUrlFromMessage(email, indexMessage);
        webDriver.navigate().to(url);
    }

    @Then("user saves url from email {string} by {int} message")
    public void userSavesUrlFromEmailByMessageFromMessages(String email, int indexMessage) {
        savedUrl = getUrlFromMessage(email, indexMessage);
    }

    @SneakyThrows
    @When("user go to page by saved link")
    public void goToPageBySavedLink() {
        if (savedUrl.isEmpty()) {
            throw new IllegalStateException("Saved link is empty");
        }

        webDriver.get(savedUrl);

        HttpURLConnection cn = (HttpURLConnection) new URL(savedUrl).openConnection();
        cn.setRequestMethod("HEAD");
        cn.connect();
        lastStatusCode = cn.getResponseCode();

        savedUrl = "";
    }

    @Then("user should get the {int} status code")
    public void userShouldGetTheError(int statusCode) {
        assertEquals(statusCode, lastStatusCode);
    }

    private String getUrlFromMessage(String email, int indexMessage) {
        EmailMessageManager manager = Hooks.getMailManager();
        MimeMessage message = manager.getMessageByIndex(email, indexMessage - 1);

        return manager.getUrlFromMessage(message);
    }

    @Then("should show {string} label")
    public void checkIfLabelIsShowed(String text) {
        basePage.checkIfLabelIsShowed(text);
    }
}
