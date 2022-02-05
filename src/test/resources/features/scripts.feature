Feature: Scripts
  Testing the Scripts page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    And user clicks the create new database button
    And user go to "Scripts" section

  Scenario: Scripts page should be showed
    Then the scripts page is showed up


  Scenario: Load the file with wrong extension
    Then user uploads "wrong_ex.txt" file
    And user clicks upload button
    Then should show "Wrong extension. Must be .sql" message
