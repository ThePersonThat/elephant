package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HomePage {
    @FindBy(xpath = "/html/body/header/div/a")
    private WebElement logoutButton;

    @FindBy(xpath = "/html/body/div/div/main/div[2]/div/h1")
    private WebElement label;

    @FindBy(xpath = "/html/body/div/div/main/div[2]/div/a")
    private WebElement resendButton;

    @FindBy(xpath = "/html/body/div/div/main/div[3]/button")
    private WebElement createDatabaseButton;

    @FindBy(xpath = "/html/body/div/div/main/div[2]/div")
    private WebElement greetingBlock;

    @FindBy(xpath = "//*[@id=\"sidebarMenu\"]/div/ul/li[1]/a")
    private WebElement dashboardButton;

    @FindBy(xpath = "//*[@id=\"sidebarMenu\"]/div/ul/li[2]/a")
    private WebElement profileButton;

    @FindBy(xpath = "//div[@class='card-body border mb-1']")
    private List<WebElement> databases;

    private WebDriver webDriver;

    public HomePage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
        this.webDriver = driverContext.getWebDriver();
    }

    public void checkIfHomePageIsShowed() {
        assertTrue(label.isDisplayed());
    }

    public void logout() {
        logoutButton.click();
    }

    public void clickResendButton() {
        resendButton.click();
    }

    public void isCreateDatabaseHasProperty(String property) {
        assertTrue(createDatabaseButton.getAttribute("class").contains(property));
    }

    public void isGreetingBlockShowed() {
        assertTrue(greetingBlock.isDisplayed());
    }

    public void clickCreateNewDbButton() {
        webDriver.findElement(By.xpath("//*[text()='Create new database']")).click();
    }

    public void checkCountDatabase(int count) {
        assertEquals(count, databases.size());
    }

    public void deleteDatabase(int databaseIndex) {
        databases.get(databaseIndex).findElement(By.xpath("//button[@class='btn']")).click();
    }

    public void goToDatabase(int databaseIndex) {
        databases.get(databaseIndex).findElement(By.tagName("a")).click();
    }
}
