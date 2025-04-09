Feature: FTOC CLI Testing
  Tests for the FTOC Command Line Interface

  Background:
    * def getCommand = function(args) { return ftocCommand + ' ' + args }

  @SystemTest @CLI
  Scenario: Run FTOC with version flag
    * def command = getCommand('--version')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains 'FTOC'
    * match result.stdout contains /\d+\.\d+\.\d+/

  @SystemTest @CLI
  Scenario: Run FTOC with help flag
    * def command = getCommand('--help')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains 'Usage:'
    * match result.stdout contains '--directory'
    * match result.stdout contains '--format'

  @SystemTest @CLI @TOC
  Scenario: Generate TOC in plain text format
    * def command = getCommand('--directory ' + featuresPath + ' --format text')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains 'TABLE OF CONTENTS'
    * match result.stdout contains 'Feature:'
    * match result.stdout contains 'Scenario:'

  @SystemTest @CLI @TOC
  Scenario: Generate TOC in markdown format
    * def command = getCommand('--directory ' + featuresPath + ' --format markdown')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains '## TABLE OF CONTENTS'
    * match result.stdout contains '### Feature:'
    * match result.stdout contains '- Scenario:'

  @SystemTest @CLI @TOC
  Scenario: Generate TOC with tag filtering
    * def command = getCommand('--directory ' + featuresPath + ' --include-tags P0')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains '@P0'
    * match result.stdout contains 'FILTERS APPLIED'
    * match result.stdout contains 'Include tags: @P0'

  @SystemTest @CLI @TagAnalysis
  Scenario: Run tag quality analysis 
    * def command = getCommand('--directory ' + featuresPath + ' --analyze-tag-quality')
    * def result = karate.exec(command)
    * match result.exitCode == 0
    * match result.stdout contains 'TAG QUALITY REPORT' || result.stdout contains 'Tag Quality Report'

  @SystemTest @CLI @Error
  Scenario: Run with invalid directory
    * def command = getCommand('--directory /invalid/directory/path')
    * def result = karate.exec(command)
    * match result.exitCode != 0
    * match result.stderr contains 'Error'