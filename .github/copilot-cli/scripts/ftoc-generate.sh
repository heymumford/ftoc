#!/bin/bash

# Script to generate sample feature file with best practices

# Display usage if no arguments or help flag provided
if [ $# -eq 0 ] || [ "$1" == "--help" ]; then
    echo "Usage: ftoc:generate [options] <output-file>"
    echo "Options:"
    echo "  --template <type>    Template type (basic, detailed, api-test, ui-test)"
    echo "  --tags <tags>        Tags to include (comma-separated)"
    echo "  --scenarios <num>    Number of scenarios to generate (default: 3)"
    exit 0
fi

# Default values
TEMPLATE="basic"
TAGS="@P1,@API,@Regression"
SCENARIOS=3
OUTPUT_FILE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --template)
            TEMPLATE="$2"
            shift 2
            ;;
        --tags)
            TAGS="$2"
            shift 2
            ;;
        --scenarios)
            SCENARIOS="$2"
            shift 2
            ;;
        *)
            # Last argument is the output file
            OUTPUT_FILE="$1"
            shift
            ;;
    esac
done

# Validate output file
if [ -z "$OUTPUT_FILE" ]; then
    echo "Error: Output file not specified."
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p "$(dirname "$OUTPUT_FILE")"

# Convert tags string to array
IFS=',' read -ra TAG_ARRAY <<< "$TAGS"

# Generate feature file content based on template
case $TEMPLATE in
    basic)
        echo "Generating basic feature file template..."
        
        # Start with feature header
        cat > "$OUTPUT_FILE" << EOF
${TAG_ARRAY[0]} ${TAG_ARRAY[1]} ${TAG_ARRAY[2]}
Feature: Sample Feature

  As a user
  I want to test functionality
  So that I can ensure everything works properly

EOF
        
        # Generate scenarios
        for ((i=1; i<=$SCENARIOS; i++)); do
            cat >> "$OUTPUT_FILE" << EOF
  
  Scenario: Sample Scenario $i
    Given I have initial conditions
    When I perform an action
    Then I should see expected results

EOF
        done
        ;;
        
    detailed)
        echo "Generating detailed feature file template..."
        
        # Start with feature header
        cat > "$OUTPUT_FILE" << EOF
${TAG_ARRAY[0]} ${TAG_ARRAY[1]} ${TAG_ARRAY[2]}
Feature: Sample Feature with Detailed Scenarios

  As a user
  I want to test functionality in detail
  So that I can ensure everything works properly

  Background:
    Given the system is prepared for testing
    And all prerequisites are met

EOF
        
        # Generate regular scenario
        cat >> "$OUTPUT_FILE" << EOF
  
  @Important
  Scenario: Detailed Test Scenario
    Given I have specific initial conditions
    When I perform a complex action
    And I perform a secondary action
    Then I should see specific results
    And I should see secondary results
    But I should not see error messages

EOF
        
        # Generate scenario outline
        cat >> "$OUTPUT_FILE" << EOF
  
  @Parameterized
  Scenario Outline: Parameterized Test with Multiple Examples
    Given I have a <resource_type> with id <id>
    When I request details for this <resource_type>
    Then I should receive a <status_code> response
    And the response should contain <expected_fields>

    Examples:
      | resource_type | id       | status_code | expected_fields       |
      | user          | "12345"  | 200         | "name,email,role"     |
      | product       | "ABX123" | 200         | "title,price,stock"   |
      | order         | "ORD999" | 404         | "error,description"   |

EOF

        # Generate additional scenarios if needed
        if [ "$SCENARIOS" -gt 2 ]; then
            for ((i=3; i<=$SCENARIOS; i++)); do
                cat >> "$OUTPUT_FILE" << EOF
  
  Scenario: Additional Scenario $i
    Given I have specific condition $i
    When I perform action $i
    Then I should see result $i

EOF
            done
        fi
        ;;
        
    api-test)
        echo "Generating API test feature file template..."
        
        # Start with feature header
        cat > "$OUTPUT_FILE" << EOF
${TAG_ARRAY[0]} ${TAG_ARRAY[1]} ${TAG_ARRAY[2]} @API
Feature: API Testing Feature

  As an API consumer
  I want to test the API endpoints
  So that I can ensure they work correctly

  Background:
    Given the API base URL is "https://api.example.com/v1"
    And I set request headers:
      | Content-Type | application/json |
      | Accept       | application/json |

EOF
        
        # Generate scenarios
        cat >> "$OUTPUT_FILE" << EOF
  
  @GET
  Scenario: Get resource details
    Given the endpoint is "/resources/{id}"
    And the path parameter id is "12345"
    When I send a GET request
    Then the response status should be 200
    And the response should match schema "resource.json"
    And the response should contain:
      | id    | 12345              |
      | name  | Sample Resource    |
      | type  | example            |

  
  @POST
  Scenario: Create new resource
    Given the endpoint is "/resources"
    And the request body is:
      """
      {
        "name": "New Resource",
        "type": "example",
        "attributes": {
          "color": "blue",
          "size": "medium"
        }
      }
      """
    When I send a POST request
    Then the response status should be 201
    And the response header "Location" should contain "/resources/"
    And the response should contain:
      | name  | New Resource       |
      | type  | example            |

EOF

        # Generate additional scenarios if needed
        if [ "$SCENARIOS" -gt 2 ]; then
            cat >> "$OUTPUT_FILE" << EOF
  
  @PUT
  Scenario: Update existing resource
    Given the endpoint is "/resources/{id}"
    And the path parameter id is "12345"
    And the request body is:
      """
      {
        "name": "Updated Resource",
        "type": "example",
        "attributes": {
          "color": "red",
          "size": "large"
        }
      }
      """
    When I send a PUT request
    Then the response status should be 200
    And the response should contain:
      | name  | Updated Resource   |
      | type  | example            |

EOF
        fi

        if [ "$SCENARIOS" -gt 3 ]; then
            cat >> "$OUTPUT_FILE" << EOF
  
  @DELETE
  Scenario: Delete existing resource
    Given the endpoint is "/resources/{id}"
    And the path parameter id is "12345"
    When I send a DELETE request
    Then the response status should be 204

EOF
        fi
        ;;
        
    ui-test)
        echo "Generating UI test feature file template..."
        
        # Start with feature header
        cat > "$OUTPUT_FILE" << EOF
${TAG_ARRAY[0]} ${TAG_ARRAY[1]} ${TAG_ARRAY[2]} @UI
Feature: User Interface Testing Feature

  As a user
  I want to test the UI functionality
  So that I can ensure it works correctly

  Background:
    Given I am on the application home page
    And I am logged in as a standard user

EOF
        
        # Generate scenarios
        cat >> "$OUTPUT_FILE" << EOF
  
  @Navigation
  Scenario: Navigate through main sections
    When I click on the "Products" menu item
    Then I should see the products page
    And the page title should be "Products"
    When I click on the "Dashboard" menu item
    Then I should see the dashboard page
    And the page should display summary statistics

  
  @Forms
  Scenario: Submit a form with validation
    When I navigate to the "Contact" page
    And I fill in the contact form:
      | Field     | Value               |
      | Name      | John Doe            |
      | Email     | john.doe@example.com|
      | Subject   | Test Subject        |
      | Message   | This is a test message |
    And I click the "Submit" button
    Then I should see a success message
    And the form should be cleared

EOF

        # Generate additional scenarios if needed
        if [ "$SCENARIOS" -gt 2 ]; then
            cat >> "$OUTPUT_FILE" << EOF
  
  @Search
  Scenario: Search for items and filter results
    When I navigate to the "Search" page
    And I enter "test" in the search box
    And I click the "Search" button
    Then I should see search results
    When I filter results by "Category" as "Documentation"
    Then I should only see items in the "Documentation" category
    And I should see at least 1 result

EOF
        fi

        if [ "$SCENARIOS" -gt 3 ]; then
            cat >> "$OUTPUT_FILE" << EOF
  
  @Responsive
  Scenario: Verify responsive design
    When I resize the browser to mobile dimensions
    Then the menu should collapse into a hamburger icon
    And the layout should adjust to single column
    When I click the hamburger icon
    Then the menu should expand
    When I resize the browser to tablet dimensions
    Then the layout should adjust to two columns

EOF
        fi
        ;;
        
    *)
        echo "Error: Unknown template type: $TEMPLATE"
        echo "Available templates: basic, detailed, api-test, ui-test"
        exit 1
        ;;
esac

echo "Feature file generated successfully: $OUTPUT_FILE"