Feature: Discovery of charging stations
  As an electric vehicle driver
  I want to find available charging stations near me
  So I can charge my car when needed

  Background: 
    Given the user Joana is logged in
    And location services are enabled

  Scenario: Find available stations near location
    When Joana searches for charging stations near latitude "-25.5" and longitude "-30.5" within "5" km
    Then she should see a list of available stations
    And the list should contain station "Station A" at "-25.5,-30.0"
    And the list should not contain any occupied stations