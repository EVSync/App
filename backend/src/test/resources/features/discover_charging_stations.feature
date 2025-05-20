# src/test/resources/features/discover_charging_stations.feature
Feature: Discover Charging Stations
  As a consumer
  I want to find available charging stations near my location
  So I can charge my car when needed

  Scenario: Find available stations near user location
    Given Joana is logged in as a consumer with location services enabled
    And there are 3 charging stations within 5km radius
    When Joana searches for charging stations near her current location
    Then she should see a list of 3 available stations
    And each station should display its location and availability status