package edu.sumdu.tss.elephant.cucumber.steps;

import edu.sumdu.tss.elephant.cucumber.pages.DatabasePage;
import edu.sumdu.tss.elephant.cucumber.steps.hooks.WebDriverContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

public class DatabaseSteps {
    private final DatabasePage page;
    private WebDriver webDriver;

    public DatabaseSteps(DatabasePage page, WebDriverContext context) {
        this.page = page;
        this.webDriver = context.getWebDriver();
    }

    @When("user go to {string} section")
    public void goToSection(String section) {
        page.goToSection(section);
    }

    @Then("the database page is showed up")
    public void checkIfDatabasePageIsShowed() {
        page.isDatabasePageShowed();
    }

    @Then("the sql section is showed up")
    public void checkIfSQlSectionIsShowed() {
        page.isSqlSectionShowed();
    }

    @When("user enters {string} sql query")
    public void userEntersSqlQuery(String query) {
        page.enterSqlQuery(query);
    }

    @When("user clicks the execute query button")
    public void userClicksExecuteQueryButton() {
        page.clickExecuteQueryButton();
    }

    @Then("should show {int} sql results")
    public void checkCountSqlResults(int count) {
        page.checkCountResultsOfSqlQuery(count);
    }

    @Then("should show {int} tables")
    public void checkCountTables(int count) {
        page.checkCountTables(count);
    }

    @Then("should show {int} rows")
    public void checkCountRecordsOnTable(int count) {
        page.checkCountTableRows(count);
    }

    @When("user go to the {int} table")
    public void goToTable(int tableIndex) {
        page.goToTable(tableIndex - 1);
    }

    @Then("the pagination should be showed")
    public void checkIfPaginationIsShowed() {
        page.isPaginationShowed();
    }

    @When("user go to the {int} table page")
    public void goToTablePage(int pageIndex) {
        page.goToTablePaginationPage(pageIndex - 1);
    }

    @Then("should be pagination for {int} rows")
    public void checkCountPaginationPages(int count) {
        int countPages = (int) Math.ceil(count / 10.0);
        page.checkCountPaginationPages(countPages);
    }

    @Then("user clicks all pagination buttons of {int} rows")
    public void clickAllPaginationButtons(int count) {
        int countPages = (int) Math.ceil(count / 10.0);

        for (int i = 0; i < countPages; i++) {
            page.goToTablePaginationPage(i);
            page.isPageActive(i);
            page.idShouldStartFrom((i * 10) + 1);
        }
    }

    @When("user creates {int} tables with {int} rows")
    public void createTablesWithData(int countTables, int countRecords) {
        goToSection("SQL console");
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < countTables; i++) {
            String tableName = "test" + i;
            query.append(String.format("create table %s (id integer);\n", tableName));

            for (int j = 0; j < countRecords; j++) {
                query.append(String.format("insert into %s values (%s);\n", tableName, j + 1));
            }
        }

        page.enterSqlQuery(query.toString());
        page.clickExecuteQueryButton();
    }
}
