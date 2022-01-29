Feature: Home
  Testing the Home page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user on the "home" page

  Scenario: Should show home page
    Then the "home" page should be open
    And the home page is showed up

  Scenario: Logout
    When user logouts
    Then user go to the "login" page

  Scenario: Resend email
    When user clicks the resend button
    Then email "alex@jpeg.com" should contain 2 messages where the 2 message with subject "Elephant: Welcome to the club buddy"

  Scenario: The create database button is disabled if user has the not-checked role
    Then the database create button is "disabled"

  Scenario: The view of home page when user has the not-checked role
    Then should show the greeting block

  Scenario: should show message if an user does not have any databases
    Given user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    Then should show "Currently you have no one database" label

  Scenario: create new database and showing it
    Given user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    When user clicks the create new database button
    And user go to the "home" page
    Then user should have 1 databases

  Scenario: deleting database
    Given user on the "profile" page
    And user upgrades to "basic" level
    And user go to the "home" page
    When user clicks the create new database button
    And user go to the "home" page
    Then user should have 1 databases
    When user deletes the 1 database
    Then user should have 0 databases