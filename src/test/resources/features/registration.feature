Feature: Registration
  Testing the registration page

  Background:
    Given user on the "registration" page

  Scenario: Go to the registration page
    Then the "registration" page should be open
    And the registration page is showed up

  Scenario: Registration with valid credentials
    When user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    Then the "home" page should be open
    And the home page is showed up

  Scenario Outline: Registration with invalid email
    When user enters "<email>" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    Then should show the email error message

    Examples:
      | email    |
      |          |
      | alex.com |
      | alex@com |

  Scenario: Registration with existing email
    Given user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user logouts
    And user go to the "registration" page
    When user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    Then should show "Login (email) already taken" message

  Scenario Outline: Registration with invalid password
    When user enters "alex@jpeg.com" as email and "<password>" as password and "<password>" as confirmation password
    And user clicks the sing up button
    Then should show "password Password should be at least 8 symbols, with at least 1 digit, 1 uppercase letter and 1 non alpha-num symbol" message

    Examples:
      | password   |
      | 1234567    |
      | abaDgf!fdr |
      | abfe14!fr  |
      | adbe124Dw  |
      |            |

  Scenario Outline: Registration with invalid confirmation password
    When user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "<confirmation>" as confirmation password
    And user clicks the sing up button
    Then should show the confirmation error message

    Examples:
      | confirmation    |
      | alex12H!fdsaf24 |
      |                 |

  Scenario: Showing and hiding password
    When user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    Then the password should be hidden
    When user clicks the hiding button
    Then the password should be visible
    When user clicks the hiding button
    Then the password should be hidden

