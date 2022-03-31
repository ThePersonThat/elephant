Feature: SQL Console
  Testing the SQL Console page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex1@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    And user clicks the create new database button
    And user go to "SQL console" section

  Scenario: SQL console page should be showed
    Then the SQL Console page is showed up


  Scenario: Load the file with wrong extension
    Then user enters "SELECT * FROM generate_series(1,501)" as query
    And user clicks run button
    Then limit message is showed up
