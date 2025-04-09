#!/bin/bash
#
# FTOC Build Orchestrator
# -----------------------
# Single point of truth for all build instructions, versioning, and release management
#
# Usage: ./build-orchestrator.sh [command] [options]

set -e

# Directory setup
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_LOG_DIR="$PROJECT_ROOT/target/build-logs"
SUMMARY_FILE="$BUILD_LOG_DIR/build-summary.txt"

# Ensure we're running from the project root
cd "$PROJECT_ROOT"

# Ensure the build log directory exists
mkdir -p "$BUILD_LOG_DIR"

# Terminal colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
RESET='\033[0m'
BOLD='\033[1m'

# Timestamp for logging
timestamp() {
  date "+%Y-%m-%d %H:%M:%S"
}

# Log message to console and file
log() {
  local level=$1
  local message=$2
  local color=$RESET
  local prefix=""
  
  case $level in
    INFO)  color=$GREEN; prefix="INFO ";;
    WARN)  color=$YELLOW; prefix="WARN ";;
    ERROR) color=$RED; prefix="ERROR";;
    *) prefix="$level ";;
  esac
  
  echo -e "${color}${prefix}$(timestamp) - ${message}${RESET}"
  echo "[$prefix] $(timestamp) - $message" >> "$BUILD_LOG_DIR/build.log"
}

# Display header
display_header() {
  echo -e "${BLUE}${BOLD}"
  echo -e "╔══════════════════════════════════════════════════╗"
  echo -e "║              FTOC BUILD ORCHESTRATOR             ║"
  echo -e "╚══════════════════════════════════════════════════╝${RESET}"
  echo ""
  echo -e "Build started at $(timestamp)"
  echo -e "Working directory: $(pwd)"
  echo ""
}

# Display usage information
display_usage() {
  echo -e "${BOLD}Usage:${RESET}"
  echo -e "  ${GREEN}./util/build-orchestrator.sh${RESET} [command] [options]"
  echo ""
  echo -e "${BOLD}Commands:${RESET}"
  echo -e "  ${GREEN}build${RESET}              Build the project (default)"
  echo -e "  ${GREEN}clean${RESET}              Clean build artifacts"
  echo -e "  ${GREEN}test${RESET}               Run all tests"
  echo -e "  ${GREEN}unit-test${RESET}          Run unit tests only"
  echo -e "  ${GREEN}integration-test${RESET}   Run integration tests only"
  echo -e "  ${GREEN}cucumber-test${RESET}      Run Cucumber tests only"
  echo -e "  ${GREEN}release${RESET} [type]     Create a release (patch|minor|major)"
  echo -e "  ${GREEN}version${RESET} [command]  Version management (get|set|show)"
  echo -e "  ${GREEN}deploy${RESET}             Deploy build artifacts"
  echo -e "  ${GREEN}docs${RESET}               Generate documentation"
  echo -e "  ${GREEN}help${RESET}               Display this help message"
  echo ""
  echo -e "${BOLD}Options:${RESET}"
  echo -e "  ${GREEN}--skip-tests${RESET}       Skip running tests"
  echo -e "  ${GREEN}--verbose${RESET}          Verbose output"
  echo -e "  ${GREEN}--dry-run${RESET}          Show what would be done without making changes"
  echo ""
  echo -e "${BOLD}Examples:${RESET}"
  echo -e "  ${GREEN}./util/build-orchestrator.sh build${RESET}"
  echo -e "  ${GREEN}./util/build-orchestrator.sh release patch${RESET}"
  echo -e "  ${GREEN}./util/build-orchestrator.sh version set 1.2.3${RESET}"
  echo ""
}

# Get current version from POM
get_current_version() {
  xmlstarlet sel -N pom="http://maven.apache.org/POM/4.0.0" -t -v "/pom:project/pom:version" pom.xml
}

# Get build number (using git commit count)
get_build_number() {
  git rev-list --count HEAD
}

# Get git hash
get_git_hash() {
  git rev-parse --short HEAD
}

# Get git branch
get_git_branch() {
  git rev-parse --abbrev-ref HEAD
}

# Get latest git tag
get_latest_git_tag() {
  git describe --tags --abbrev=0 2>/dev/null || echo "none"
}

# Get changes since last tag
get_changes_since_last_tag() {
  local latest_tag=$(get_latest_git_tag)
  
  if [ "$latest_tag" = "none" ]; then
    git log --pretty=format:"%h %s" -n 10
  else
    git log --pretty=format:"%h %s" $latest_tag..HEAD
  fi
}

# Get compiler version
get_compiler_version() {
  mvn -v | grep "Java version" | awk '{print $3}'
}

# Validate semantic version format
validate_version() {
  local version=$1
  if ! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    log ERROR "Invalid version format. Use semantic versioning (x.y.z)"
    exit 1
  fi
}

# Increment version parts
increment_version() {
  local version=$1
  local part=$2
  
  IFS='.' read -r -a version_parts <<< "$version"
  local major="${version_parts[0]}"
  local minor="${version_parts[1]}"
  local patch="${version_parts[2]}"
  
  case $part in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
  esac
  
  echo "$major.$minor.$patch"
}

# Update version in all required files
update_version() {
  local new_version=$1
  local current_version=$(get_current_version)
  
  log INFO "Updating version: $current_version → $new_version"
  
  # Update POM file (source of truth)
  xmlstarlet ed -N pom="http://maven.apache.org/POM/4.0.0" \
    -u "/pom:project/pom:version" -v "$new_version" pom.xml > pom.xml.new \
    && mv pom.xml.new pom.xml
  
  log INFO "Updated pom.xml with new version: $new_version"
  
  # Commit version change
  git add pom.xml
  git commit -m "Update version to $new_version" -q || true
  
  log INFO "Committed version change to git"
}

# Tag version in git
tag_version() {
  local version=$1
  local tag_exists=$(git tag -l "v$version")
  
  if [ -z "$tag_exists" ]; then
    log INFO "Creating git tag: v$version"
    git tag -a "v$version" -m "Version $version"
    
    # Push tag to remote if requested
    if [ "$PUSH_TO_REMOTE" = "true" ]; then
      git push origin "v$version"
      log INFO "Pushed tag v$version to remote"
    else
      log INFO "Created git tag v$version (not pushed to remote)"
    fi
  else
    log WARN "Git tag v$version already exists"
  fi
}

# Generate version information for summary
generate_version_info() {
  {
    echo "VERSION INFORMATION"
    echo "===================="
    echo "Version:        $(get_current_version)"
    echo "Build Number:   $(get_build_number)"
    echo "Full ID:        $(get_current_version).$(get_build_number)"
    echo "Git Branch:     $(get_git_branch)"
    echo "Git Commit:     $(get_git_hash)"
    echo "Latest Tag:     $(get_latest_git_tag)"
    echo "Java Version:   $(get_compiler_version)"
    echo "Build Date:     $(date "+%Y-%m-%d %H:%M:%S")"
    echo ""
  } > "$SUMMARY_FILE"
}

# Run Maven clean
run_clean() {
  log INFO "Cleaning build artifacts"
  mvn clean -B > "$BUILD_LOG_DIR/clean.log" 2>&1
  if [ $? -eq 0 ]; then
    log INFO "Clean completed successfully"
  else
    log ERROR "Clean failed. See $BUILD_LOG_DIR/clean.log for details"
    exit 1
  fi
}

# Run Maven compile
run_compile() {
  log INFO "Compiling source code"
  mvn compile -B > "$BUILD_LOG_DIR/compile.log" 2>&1
  if [ $? -eq 0 ]; then
    log INFO "Compilation completed successfully"
  else
    log ERROR "Compilation failed. See $BUILD_LOG_DIR/compile.log for details"
    exit 1
  fi
}

# Run unit tests
run_unit_tests() {
  log INFO "Running unit tests"
  mvn test -B -Dtest=*Test -DexcludedGroups=Integration,Cucumber > "$BUILD_LOG_DIR/unit-tests.log" 2>&1
  UNIT_TEST_RESULT=$?
  
  # Save test results to summary
  if [ $UNIT_TEST_RESULT -eq 0 ]; then
    log INFO "Unit tests completed successfully"
    UNIT_TEST_STATUS="PASSED"
  else
    log WARN "Unit tests failed. See $BUILD_LOG_DIR/unit-tests.log for details"
    UNIT_TEST_STATUS="FAILED"
  fi
  
  # Extract test counts
  UNIT_TEST_COUNT=$(grep -o "Tests run: [0-9]*" "$BUILD_LOG_DIR/unit-tests.log" | awk '{sum+=$3} END {print sum}')
  UNIT_TEST_FAILURES=$(grep -o "Failures: [0-9]*" "$BUILD_LOG_DIR/unit-tests.log" | awk '{sum+=$2} END {print sum}')
  UNIT_TEST_ERRORS=$(grep -o "Errors: [0-9]*" "$BUILD_LOG_DIR/unit-tests.log" | awk '{sum+=$2} END {print sum}')
  UNIT_TEST_SKIPPED=$(grep -o "Skipped: [0-9]*" "$BUILD_LOG_DIR/unit-tests.log" | awk '{sum+=$2} END {print sum}')
  
  return $UNIT_TEST_RESULT
}

# Run integration tests
run_integration_tests() {
  log INFO "Running integration tests"
  mvn verify -B -Dtest=*IT -Dgroups=Integration > "$BUILD_LOG_DIR/integration-tests.log" 2>&1
  INTEGRATION_TEST_RESULT=$?
  
  # Save test results to summary
  if [ $INTEGRATION_TEST_RESULT -eq 0 ]; then
    log INFO "Integration tests completed successfully"
    INTEGRATION_TEST_STATUS="PASSED"
  else
    log WARN "Integration tests failed. See $BUILD_LOG_DIR/integration-tests.log for details"
    INTEGRATION_TEST_STATUS="FAILED"
  fi
  
  # Extract test counts
  INTEGRATION_TEST_COUNT=$(grep -o "Tests run: [0-9]*" "$BUILD_LOG_DIR/integration-tests.log" | awk '{sum+=$3} END {print sum}')
  INTEGRATION_TEST_FAILURES=$(grep -o "Failures: [0-9]*" "$BUILD_LOG_DIR/integration-tests.log" | awk '{sum+=$2} END {print sum}')
  INTEGRATION_TEST_ERRORS=$(grep -o "Errors: [0-9]*" "$BUILD_LOG_DIR/integration-tests.log" | awk '{sum+=$2} END {print sum}')
  INTEGRATION_TEST_SKIPPED=$(grep -o "Skipped: [0-9]*" "$BUILD_LOG_DIR/integration-tests.log" | awk '{sum+=$2} END {print sum}')
  
  return $INTEGRATION_TEST_RESULT
}

# Run Cucumber tests
run_cucumber_tests() {
  log INFO "Running Cucumber tests"
  mvn verify -B -Dtest=RunCucumberTest > "$BUILD_LOG_DIR/cucumber-tests.log" 2>&1
  CUCUMBER_TEST_RESULT=$?
  
  # Save test results to summary
  if [ $CUCUMBER_TEST_RESULT -eq 0 ]; then
    log INFO "Cucumber tests completed successfully"
    CUCUMBER_TEST_STATUS="PASSED"
  else
    log WARN "Cucumber tests failed. See $BUILD_LOG_DIR/cucumber-tests.log for details"
    CUCUMBER_TEST_STATUS="FAILED"
  fi
  
  # Extract test counts (different format for Cucumber)
  CUCUMBER_TEST_COUNT=$(grep -o "[0-9]* Scenarios" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$1} END {print sum}')
  CUCUMBER_TEST_PASSED=$(grep -o "[0-9]* passed" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$1} END {print sum}')
  CUCUMBER_TEST_FAILED=$(grep -o "[0-9]* failed" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$1} END {print sum}')
  CUCUMBER_TEST_SKIPPED=$(grep -o "[0-9]* skipped" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$1} END {print sum}')
  
  # If can't extract counts, try different format
  if [ -z "$CUCUMBER_TEST_COUNT" ]; then
    CUCUMBER_TEST_COUNT=$(grep -o "Tests run: [0-9]*" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$3} END {print sum}')
    CUCUMBER_TEST_FAILURES=$(grep -o "Failures: [0-9]*" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$2} END {print sum}')
    CUCUMBER_TEST_ERRORS=$(grep -o "Errors: [0-9]*" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$2} END {print sum}')
    CUCUMBER_TEST_SKIPPED=$(grep -o "Skipped: [0-9]*" "$BUILD_LOG_DIR/cucumber-tests.log" | awk '{sum+=$2} END {print sum}')
    CUCUMBER_TEST_PASSED=$((CUCUMBER_TEST_COUNT - CUCUMBER_TEST_FAILURES - CUCUMBER_TEST_ERRORS - CUCUMBER_TEST_SKIPPED))
  fi
  
  return $CUCUMBER_TEST_RESULT
}

# Run all tests
run_all_tests() {
  log INFO "Running all tests"
  
  # Run unit tests and capture result
  run_unit_tests
  UNIT_RESULT=$?
  
  # Run integration tests and capture result
  run_integration_tests
  INTEGRATION_RESULT=$?
  
  # Run cucumber tests and capture result
  run_cucumber_tests
  CUCUMBER_RESULT=$?
  
  # Return overall test result
  ALL_TESTS_RESULT=0
  if [ $UNIT_RESULT -ne 0 ] || [ $INTEGRATION_RESULT -ne 0 ] || [ $CUCUMBER_RESULT -ne 0 ]; then
    ALL_TESTS_RESULT=1
  fi
  
  return $ALL_TESTS_RESULT
}

# Package the application
run_package() {
  log INFO "Packaging application"
  mvn package -B -DskipTests > "$BUILD_LOG_DIR/package.log" 2>&1
  if [ $? -eq 0 ]; then
    log INFO "Packaging completed successfully"
    
    # Get JAR file information
    JAR_FILE=$(find target -name "*jar-with-dependencies.jar" | head -n 1)
    if [ -n "$JAR_FILE" ]; then
      JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
      log INFO "Generated artifact: $JAR_FILE ($JAR_SIZE)"
    else
      log WARN "No JAR artifact found after packaging"
    fi
  else
    log ERROR "Packaging failed. See $BUILD_LOG_DIR/package.log for details"
    exit 1
  fi
}

# Generate test summary section for build report
generate_test_summary() {
  {
    echo "TEST RESULTS SUMMARY"
    echo "===================="
    echo ""
    
    # Unit tests
    echo "Unit Tests: ${UNIT_TEST_STATUS:-NOT RUN}"
    if [ -n "$UNIT_TEST_COUNT" ]; then
      echo "  Tests: $UNIT_TEST_COUNT"
      echo "  Passed: $((UNIT_TEST_COUNT - UNIT_TEST_FAILURES - UNIT_TEST_ERRORS - UNIT_TEST_SKIPPED))"
      echo "  Failed: $((UNIT_TEST_FAILURES + UNIT_TEST_ERRORS))"
      echo "  Skipped: $UNIT_TEST_SKIPPED"
    fi
    echo ""
    
    # Integration tests
    echo "Integration Tests: ${INTEGRATION_TEST_STATUS:-NOT RUN}"
    if [ -n "$INTEGRATION_TEST_COUNT" ]; then
      echo "  Tests: $INTEGRATION_TEST_COUNT"
      echo "  Passed: $((INTEGRATION_TEST_COUNT - INTEGRATION_TEST_FAILURES - INTEGRATION_TEST_ERRORS - INTEGRATION_TEST_SKIPPED))"
      echo "  Failed: $((INTEGRATION_TEST_FAILURES + INTEGRATION_TEST_ERRORS))"
      echo "  Skipped: $INTEGRATION_TEST_SKIPPED"
    fi
    echo ""
    
    # Cucumber tests
    echo "Cucumber Tests: ${CUCUMBER_TEST_STATUS:-NOT RUN}"
    if [ -n "$CUCUMBER_TEST_COUNT" ]; then
      echo "  Scenarios: $CUCUMBER_TEST_COUNT"
      echo "  Passed: ${CUCUMBER_TEST_PASSED:-0}"
      echo "  Failed: ${CUCUMBER_TEST_FAILED:-0}"
      echo "  Skipped: ${CUCUMBER_TEST_SKIPPED:-0}"
    fi
    echo ""
    
    # Overall summary
    echo "Overall Test Result: ${ALL_TESTS_RESULT:-NOT RUN}"
    echo ""
  } >> "$SUMMARY_FILE"
}

# Generate build artifact summary
generate_build_summary() {
  # Get JAR file information
  JAR_FILE=$(find target -name "*jar-with-dependencies.jar" | head -n 1)
  
  {
    echo "BUILD ARTIFACTS"
    echo "==============="
    echo ""
    
    if [ -n "$JAR_FILE" ]; then
      JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
      
      echo "Main Application JAR:"
      echo "  Path: $JAR_FILE"
      echo "  Size: $JAR_SIZE"
      echo "  SHA256: $(sha256sum "$JAR_FILE" | cut -d' ' -f1)"
      echo ""
    fi
    
    # List any other artifacts that were created
    echo "Additional Artifacts:"
    find target -name "*.jar" -not -name "*-with-dependencies.jar" | while read -r artifact; do
      echo "  - $(basename "$artifact") ($(du -h "$artifact" | cut -f1))"
    done
    echo ""
  } >> "$SUMMARY_FILE"
}

# Generate changes section
generate_changes_section() {
  {
    echo "CHANGES SINCE LAST RELEASE"
    echo "=========================="
    echo ""
    echo "$(get_changes_since_last_tag)"
    echo ""
  } >> "$SUMMARY_FILE"
}

# Generate complete build summary
generate_complete_summary() {
  # Start with version info
  generate_version_info
  
  # Add test results
  generate_test_summary
  
  # Add build artifacts
  generate_build_summary
  
  # Add changes since last release
  generate_changes_section
  
  # Print summary to console
  log INFO "Build summary generated at: $SUMMARY_FILE"
  cat "$SUMMARY_FILE"
}

# Run a full build
run_full_build() {
  log INFO "Running full build process"
  
  # Step 1: Clean
  run_clean
  
  # Step 2: Compile
  run_compile
  
  # Step 3: Run tests (if not skipped)
  if [ "$SKIP_TESTS" = "true" ]; then
    log INFO "Skipping tests as requested"
  else
    run_all_tests
    ALL_TESTS_RESULT=$?
  fi
  
  # Step 4: Package
  run_package
  
  # Step 5: Generate summary
  generate_complete_summary
  
  log INFO "Build completed successfully"
  
  # Return test result status if tests were run
  if [ "$SKIP_TESTS" = "true" ]; then
    return 0
  else
    return $ALL_TESTS_RESULT
  fi
}

# Create a release
create_release() {
  local release_type=$1
  
  if [ -z "$release_type" ]; then
    log ERROR "Release type not specified (patch|minor|major)"
    display_usage
    exit 1
  fi
  
  local current_version=$(get_current_version)
  local new_version=""
  
  case $release_type in
    patch|minor|major)
      new_version=$(increment_version "$current_version" "$release_type")
      ;;
    *)
      log ERROR "Invalid release type: $release_type (must be patch, minor, or major)"
      exit 1
      ;;
  esac
  
  log INFO "Creating $release_type release: $current_version → $new_version"
  
  # Ensure working directory is clean
  if [ -n "$(git status --porcelain)" ]; then
    log ERROR "Working directory is not clean. Commit or stash changes before creating a release."
    exit 1
  fi
  
  # Update version
  update_version "$new_version"
  
  # Run a full build to verify everything works with the new version
  run_full_build
  BUILD_RESULT=$?
  
  if [ $BUILD_RESULT -ne 0 ]; then
    log ERROR "Build failed with the new version. Rolling back."
    git reset --hard HEAD~1
    exit 1
  fi
  
  # Create git tag
  tag_version "$new_version"
  
  # Push changes if requested
  if [ "$PUSH_TO_REMOTE" = "true" ]; then
    log INFO "Pushing changes to remote"
    git push origin "$(get_git_branch)"
    git push origin "v$new_version"
  fi
  
  log INFO "Release $new_version created successfully"
}

# Generate documentation
generate_docs() {
  log INFO "Generating documentation"
  mvn javadoc:javadoc -B > "$BUILD_LOG_DIR/javadoc.log" 2>&1
  
  if [ $? -eq 0 ]; then
    log INFO "Documentation generated successfully"
    log INFO "JavaDoc is available at: target/site/apidocs/index.html"
  else
    log ERROR "Documentation generation failed. See $BUILD_LOG_DIR/javadoc.log for details"
    exit 1
  fi
}

# Deploy artifacts
deploy_artifacts() {
  log INFO "Deploying artifacts"
  # This would typically push to a Maven repository, artifact registry, etc.
  log WARN "Deployment not configured. Placeholder for future implementation."
}

# Main function
main() {
  display_header
  
  # Default values
  SKIP_TESTS=false
  VERBOSE=false
  DRY_RUN=false
  PUSH_TO_REMOTE=false
  
  # Parse command and options
  local command=${1:-build}
  shift || true
  
  # Parse options
  while [ $# -gt 0 ]; do
    case "$1" in
      --skip-tests)
        SKIP_TESTS=true
        ;;
      --verbose)
        VERBOSE=true
        ;;
      --dry-run)
        DRY_RUN=true
        ;;
      --push)
        PUSH_TO_REMOTE=true
        ;;
      *)
        break
        ;;
    esac
    shift
  done
  
  log INFO "Running command: $command"
  log INFO "Options: SKIP_TESTS=$SKIP_TESTS, VERBOSE=$VERBOSE, DRY_RUN=$DRY_RUN, PUSH_TO_REMOTE=$PUSH_TO_REMOTE"
  
  # Execute the requested command
  case $command in
    build)
      run_full_build
      ;;
    clean)
      run_clean
      ;;
    test)
      run_all_tests
      generate_test_summary
      ;;
    unit-test)
      run_unit_tests
      ;;
    integration-test)
      run_integration_tests
      ;;
    cucumber-test)
      run_cucumber_tests
      ;;
    release)
      create_release "$1"
      ;;
    version)
      local version_command=${1:-show}
      case $version_command in
        get)
          echo "$(get_current_version)"
          ;;
        set)
          local new_version=$2
          validate_version "$new_version"
          update_version "$new_version"
          ;;
        show)
          generate_version_info
          cat "$SUMMARY_FILE"
          ;;
        *)
          log ERROR "Unknown version command: $version_command"
          display_usage
          exit 1
          ;;
      esac
      ;;
    deploy)
      deploy_artifacts
      ;;
    docs)
      generate_docs
      ;;
    help|*)
      display_usage
      ;;
  esac
}

# Execute main function with all arguments
main "$@"