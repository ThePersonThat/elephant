package edu.sumdu.tss.elephant.cucumber.pages;

import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabasePage {
    @FindBy(xpath = "//a[@class='nav-link active']")
    private List<WebElement> sections;

    @FindBy(xpath = "//*[text()='select * from table1']")
    private WebElement sqlQuery;

    @FindBy(xpath = "//div[@class='result']")
    private List<WebElement> sqlResults;

    @FindBy(xpath = "//button[text()='Run']")
    private WebElement runButton;

    @FindBy(xpath = "//*[@id=\"editor\"]/textarea")
    private WebElement sqlQueryInput;

    @FindBy(xpath = "//td[@class='user_table']")
    private List<WebElement> tables;

    @FindBy(tagName = "tr")
    private List<WebElement> tableRows;

    @FindBy(xpath = "//ul[@class='pagination']")
    private WebElement pagination;

    @FindBy(xpath = "//li[@class='page-item']")
    private List<WebElement> paginationPages;

    public DatabasePage(WebDriverContext driverContext) {
        PageFactory.initElements(driverContext.getWebDriver(), this);
    }

    public void isDatabasePageShowed() {
        assertEquals(5, sections.size());
    }

    public void goToSection(String section) {
        sections.stream()
                .filter(element -> element.getText().contains(section))
                .findFirst()
                .ifPresent(WebElement::click);
    }

    public void enterSqlQuery(String query) {
        sqlQueryInput.sendKeys(query);
    }

    public void isSqlSectionShowed() {
        assertTrue(sqlQuery.isDisplayed());
    }

    public void checkCountResultsOfSqlQuery(int count) {
        assertEquals(count, sqlResults.size());
    }

    public void clickExecuteQueryButton() {
        runButton.click();
    }

    public void checkCountTables(int count) {
        assertEquals(count, tables.size());
    }

    public void goToTable(int tableIndex) {
        tables.get(tableIndex).findElement(By.tagName("a")).click();
    }

    public void checkCountTableRows(int count) {
        assertEquals(count, tableRows.size() - 1);
    }

    public void isPaginationShowed() {
        assertTrue(pagination.isDisplayed());
    }

    public void checkCountPaginationPages(int count) {
        assertEquals(count, paginationPages.size());
    }

    public void goToTablePaginationPage(int pageIndex) {
        paginationPages.get(pageIndex).click();
    }

    public void isPageActive(int pageIndex) {
        assertTrue(paginationPages.get(pageIndex).getAttribute("class").contains("active"));
    }

    public void idShouldStartFrom(int start) {
        assertEquals(Integer.toString(start), tableRows.get(0).getText());
    }
}
