Feature: Profile
  Testing the Profile page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user on the "profile" page

  Scenario: Upgrade user to the pro level
    Given user on the "profile" page
    When user upgrades to "pro" level
    Then should show "Role has been changed" message

  Scenario: Upgrade pro user to the basic level
    Given user on the "profile" page
    When user upgrades to "pro" level
    Then should show "Role has been changed" message
    When user upgrades to "basic" level
    Then should show "Role has been changed" message

  Scenario: The keys should be showed
    Given user on the "profile" page
    Then the public key and private key should be showed

  Scenario: Change a database password
    Given user on the "profile" page
    When user enters the "helloWorld" as database password
    And user clicks the change database password button
    Then should show "DB user password was changed" message

  Scenario: User regenerates api keys
    When user upgrades to "pro" level
    Then user regenerates api keys