Feature: Login
  Testing the Login page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user logouts
    And user go to the "login" page

  Scenario: Login page should be open
    Then the "login" page should be open
    And the login page is showed up

  Scenario: Login with valid credentials
    When user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password
    When user clicks the login button
    Then the "home" page should be open
    And the home page is showed up

  Scenario Outline: Login with not existing credentials
    When user enters "<email>" as email and "<password>" as password
    When user clicks the login button
    Then should show "User or password not known" message

    Examples:
      | email               | password              |
      | notexsting@mail.com | alex12H!fdsaf         |
      | alex@jpeg.com       | alex12H!fdsafNotExist |

  Scenario Outline: Go to the auth pages if user is logged in
    Given user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password
    And user clicks the login button
    When user go to the "<page>" page
    Then the "home" page should be open
    And the home page is showed up

    Examples:
      | page         |
      | registration |
      | login        |

  Scenario: User go to the forgot password page
    When user clicks the forgot password button