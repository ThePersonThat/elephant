Feature: Reset password
  Testing the Reset password page and related functionality or pages

  Background:
    Given user on the "registration" page
    And user enters "alex@jpeg.com" as email and "alex12H!fdsaf" as password and "alex12H!fdsaf" as confirmation password
    And user clicks the sing up button
    And user logouts
    And user go to the "login" page
    And user clicks the forgot password button

  Scenario: Reset password page should open
    Then the "login/reset-password" page should be open
    And the reset-password page is showed up

  Scenario: Send a reset password email
    When user enters "alex@jpeg.com" as email
    And click the send link button
    Then email "alex@jpeg.com" should contain 2 messages where the 2 message with subject "Elephant: Reset password"

  Scenario Outline: Reset password with invalid email
    When user enters "<email>" as email
    And click the send link button
    Then should show "email Is it a valid mail?" message

    Examples:
      | email    |
      |          |
      | alex.com |
      | alex@com |

  Scenario: Go to the reset password page from an email
    When user enters "alex@jpeg.com" as email
    And click the send link button
    Then email "alex@jpeg.com" should contain 2 messages where the 2 message with subject "Elephant: Reset password"
    Then go to url from email "alex@jpeg.com" by 2 message
    Then the reset page is showed up

  Scenario: Trying to reset password by expired link
    When user enters "alex@jpeg.com" as email
    And click the send link button
    Then email "alex@jpeg.com" should contain 2 messages where the 2 message with subject "Elephant: Reset password"
    Then user saves url from email "alex@jpeg.com" by 2 message
    Then user go to the "login" page
    And user clicks the forgot password button
    When user enters "alex@jpeg.com" as email
    And click the send link button
    And user go to page by saved link
    Then user should get the 403 status code

  Scenario: Trying to reset password by new link
    When user enters "alex@jpeg.com" as email
    And click the send link button
    Then email "alex@jpeg.com" should contain 2 messages where the 2 message with subject "Elephant: Reset password"
    When user go to the "login" page
    And user clicks the forgot password button
    And user enters "alex@jpeg.com" as email
    And click the send link button
    Then email "alex@jpeg.com" should contain 3 messages where the 3 message with subject "Elephant: Reset password"
    Then go to url from email "alex@jpeg.com" by 2 message
    Then the reset page is showed up

  Scenario: Reset password
    When user enters "alex@jpeg.com" as email
    And click the send link button
    Then go to url from email "alex@jpeg.com" by 2 message
    When user enters "alex12H!fdsafA" as password and "alex12H!fdsafA" as confirmation password
    And user clicks the change password button
    Then the "login" page should be open
    And the login page is showed up
    And should show "Password has been reset" message

  Scenario Outline: Reset password with invalid password
    Given user enters "alex@jpeg.com" as email
    And click the send link button
    Then go to url from email "alex@jpeg.com" by 2 message
    When user enters "<password>" as password and "<password>" as confirmation password
    And user clicks the change password button
    Then should show "password Password should be at least 8 symbols, with at least 1 digit, 1 uppercase letter and 1 non alpha-num symbol" message

    Examples:
      | password      |
      | aLex@e2       |
      | abaDgf!fdr    |
      | abfe14!fr     |
      | adbe124Dw     |
      | alex12H!fdsaf |

  Scenario: Trying to change password after changing password
    Given user enters "alex@jpeg.com" as email
    And click the send link button
    And user saves url from email "alex@jpeg.com" by 2 message
    Then go to url from email "alex@jpeg.com" by 2 message
    And user enters "alex12H!fdsafA" as password and "alex12H!fdsafA" as confirmation password
    And user clicks the change password button
    And user go to page by saved link
    Then user should get the 403 status code
