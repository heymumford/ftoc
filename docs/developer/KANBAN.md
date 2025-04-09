# FTOC Development Kanban Board

## Backlog
- [ ] Add command line option for output format (plain text, markdown, HTML)
- [ ] Add command line option for filtering scenarios by tag
- [ ] Add detection and warnings for common anti-patterns in feature files
- [ ] Implement configuration file support for customizable warnings
- [ ] Expand test suite with more complex feature file examples
- [ ] Create documentation for tag best practices
- [ ] Add JUnit report integration
- [ ] Create GitHub Actions workflow for CI/CD

## In Progress
- [ ] Complete TOC generation with proper formatting
- [ ] Improve tag concordance report with more detailed statistics
- [ ] Enhance warning system for "dumb" tags detection

## GitHub Copilot Integration
- [x] Setup Copilot-friendly repository structure
  - [x] Create .github/copilot directory with prompts and examples
  - [x] Document code patterns for Copilot
- [x] Configure Copilot Code Review automation
  - [x] Setup GitHub Actions workflow for Copilot code review
  - [x] Configure review settings and patterns
- [x] Create Copilot-optimized PR templates
  - [x] Add Copilot-specific markers for better summaries
  - [x] Include common task templates
- [x] Add Copilot IDE integrations
  - [x] Create VS Code settings for better Copilot experience
  - [ ] Setup JetBrains IDE configurations for Copilot
- [x] Implement Copilot developer onboarding
  - [x] Create documentation with example prompts
  - [x] Add task-specific examples
- [ ] Create Copilot CLI integrations
  - [ ] Setup script for Copilot CLI
  - [ ] Add custom project aliases
- [x] Implement advanced Copilot features
  - [x] Configure Copilot Workspace for the project
  - [ ] Explore Copilot Extension development
  - [ ] Setup Copilot Edits modes for common tasks

## Architecture Improvements
- [ ] Create proper separation of concerns (Parser, Processor, Reporter)
- [ ] Implement plugin system for extensibility
- [ ] Add more comprehensive error handling
- [ ] Create interfaces for major components to allow for testing with mocks
- [ ] Implement strategy pattern for different output formats

## Testing Approach
- [ ] Create sample feature files with various complexity for testing
- [ ] Add unit tests for core components
- [ ] Add integration tests for end-to-end functionality
- [ ] Implement property-based testing for edge cases
- [ ] Add performance tests for large feature file repositories

## Done
- [x] Basic project structure with Maven
- [x] Initial command-line argument parsing
- [x] Feature file discovery functionality
- [x] Basic tag concordance reporting
- [x] Version reporting from properties
- [x] Initial Cucumber test setup
- [x] Project directory reorganization and cleanup