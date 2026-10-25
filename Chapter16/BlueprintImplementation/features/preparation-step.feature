# Board B2 — Preparation step (Example Mapping "Help for a preparation step", 2026-10-24).
# Increment 0: slices B2-S01 (request help) and B1-S04 (Grandma Avatar answers). Rules 4, 6, 7, 9, 10 are planned
# for increment 1 (they need operations not built yet). @todo marks a scenario that is not active yet.
Feature: Help for a preparation step
  As a Cook, I want to ask the community and/or Grandma Avatar for help with one preparation step of a recipe,
  and receive Grandma Avatar's answer, so that I get help in preparing my meal.

  Background:
    Given Recipe Catalog knows "Sunday scones" with 8 steps

  @B2-S01 @R1 @ex.1
  Scenario: A cook asks what "warm" means in step 3 of Sunday scones
    When the cook asks the community and Grandma Avatar about step 3 of "Sunday scones": "What means 'warm'?"
    Then the help request is created
    And "Help requested" is published for it

  @B2-S01 @R1 @ex.2
  Scenario: A request without a step is rejected
    When the cook asks Grandma Avatar about "Sunday scones" without naming a step
    Then the request is rejected
    And the cook has no help request

  @B2-S01 @R2 @ex.1
  Scenario: A step the recipe does not have is rejected
    When the cook asks Grandma Avatar about step 9 of "Sunday scones": "What means 'golden'?"
    Then the request is rejected because the step is not in the recipe
    And the cook has no help request

  @B2-S01 @R3 @ex.1
  Scenario: A request addressed to the community and Grandma Avatar is created
    When the cook asks the community and Grandma Avatar about step 3 of "Sunday scones": "What means 'warm'?"
    Then the help request is created, addressed to the community and Grandma Avatar

  @B2-S01 @R3 @ex.2
  Scenario: A Chef cannot be asked about a preparation step
    When the cook asks Chef A about step 3 of "Sunday scones": "What means 'warm'?"
    Then the request is refused: Chefs are only allowed for Meal plan
    And the cook has no help request

  @B2-S01 @R3 @ex.3
  Scenario: A request without an addressee is rejected
    When the cook asks nobody about step 3 of "Sunday scones": "What means 'warm'?"
    Then the request is rejected
    And the cook has no help request

  @B2-S01 @R8 @ex.1
  Scenario: A request without a question is rejected
    When the cook asks Grandma Avatar about step 3 of "Sunday scones" without a question
    Then the request is rejected
    And the cook has no help request

  @B2-S01 @no-rule
  Scenario: Several open requests of one cook for the same recipe and step are allowed
    Given the cook asked Grandma Avatar about step 3 of "Sunday scones": "What means 'warm'?"
    When the cook asks Grandma Avatar about step 3 of "Sunday scones": "How long does it rest?"
    Then the cook has 2 open help requests

  @B1-S04 @R5 @ex.1
  Scenario: Grandma Avatar's answer about step 3 is recorded for the request
    Given the cook asked Grandma Avatar about step 3 of "Sunday scones": "What means 'warm'?"
    When Grandma Avatar answers about step 3 of "Sunday scones": "Put the bowl next to the heater."
    Then the help is recorded for the request
    And "Help provided" is published for it

  @B1-S04 @R5 @ex.2
  Scenario: An answer about another step is refused
    Given the cook asked Grandma Avatar about step 3 of "Sunday scones": "What means 'warm'?"
    When Grandma Avatar answers about step 4 of "Sunday scones": "Bake it for 15 minutes."
    Then the answer is refused and no help is recorded
