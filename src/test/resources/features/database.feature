Feature: Database
  Testing the Database page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    And user clicks the create new database button

  Scenario: Database page should be showed
    Then the database page is showed up

  Scenario: Sql section should be showed
    Given user go to "SQL console" section
    Then the sql section is showed up

  Scenario: User executes one query
    Given user go to "SQL console" section
    When user enters "create table test (id integer)" sql query
    And user clicks the execute query button
    Then should show 1 sql results

  Scenario: User executes one query
    Given user go to "SQL console" section
    When user enters "create table test (id integer); select * from test;" sql query
    And user clicks the execute query button
    Then should show 2 sql results

  Scenario: User executes empty query
    Given user go to "SQL console" section
    When user enters "" sql query
    And user clicks the execute query button
    Then should show "Empty query error" message

  Scenario: Showing tables
    Given user creates 1 tables with 0 rows
    When user go to "Database Explorer" section
    Then should show 1 tables

  Scenario: Showing label a database does not have any tables
    When user go to "Database Explorer" section
    Then should show "Database does not have any tables" label


  Scenario: Showing label a table does not have any rows
    Given user creates 1 tables with 0 rows
    When user go to "Database Explorer" section
    And user go to the 1 table
    Then should show "Table does not have any rows" label

  Scenario: Showing data from table
    Given user creates 1 tables with 2 rows
    When user go to "Database Explorer" section
    And user go to the 1 table
    Then should show 2 rows

  Scenario: Showing pagination
    Given user creates 1 tables with 11 rows
    When user go to "Database Explorer" section
    And user go to the 1 table
    Then the pagination should be showed
    And should be pagination for 11 rows

  Scenario: Using pagination
    Given user creates 1 tables with 42 rows
    When user go to "Database Explorer" section
    And user go to the 1 table
    And user clicks all pagination buttons of 42 rows
