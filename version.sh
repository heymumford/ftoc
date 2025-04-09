#!/bin/bash
#
# Ftoc Version Management System
# ------------------------------
# Manages version numbers across the project

set -e

# Terminal colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
RESET='\033[0m'
BOLD='\033[1m'

# Display header
display_header() {
    echo -e "${BLUE}${BOLD}"
    echo -e "╔═══════════════════════════════════╗"
    echo -e "║       FTOC VERSION MANAGER        ║"
    echo -e "╚═══════════════════════════════════╝${RESET}"
    echo
}

# Display usage
display_usage() {
    echo -e "${BOLD}Usage:${RESET}"
    echo -e "  ${GREEN}./version.sh${RESET} [command]"
    echo
    echo -e "${BOLD}Commands:${RESET}"
    echo -e "  ${GREEN}get${RESET}         Display current version"
    echo -e "  ${GREEN}summary${RESET}     Display detailed version and build information"
    echo -e "  ${GREEN}patch${RESET}       Increment patch version (x.y.Z)"
    echo -e "  ${GREEN}minor${RESET}       Increment minor version (x.Y.0)"
    echo -e "  ${GREEN}major${RESET}       Increment major version (X.0.0)"
    echo -e "  ${GREEN}set${RESET} x.y.z   Set specific version"
    echo -e "  ${GREEN}help${RESET}        Display this help message"
    echo
}

# Get current version from POM file (single source of truth)
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

# Get timestamp
get_timestamp() {
    date "+%Y-%m-%d %H:%M:%S"
}

# Display detailed summary
display_summary() {
    local version=$(get_current_version)
    local build=$(get_build_number)
    local hash=$(get_git_hash)
    local branch=$(get_git_branch)
    local timestamp=$(get_timestamp)
    local tag=$(git describe --tags --exact-match 2>/dev/null || echo "None")
    
    echo -e "${BOLD}┌─ Version Information ────────────────────────────────────┐${RESET}"
    echo -e "${BOLD}│${RESET} "
    echo -e "${BOLD}│${RESET} ${CYAN}Version:${RESET}     ${BOLD}${version}${RESET}"
    echo -e "${BOLD}│${RESET} ${CYAN}Build:${RESET}       ${build}"
    echo -e "${BOLD}│${RESET} ${CYAN}Full ID:${RESET}     ${version}.${build}"
    echo -e "${BOLD}│${RESET} "
    echo -e "${BOLD}│${RESET} ${MAGENTA}Git Branch:${RESET}  ${branch}"
    echo -e "${BOLD}│${RESET} ${MAGENTA}Git Commit:${RESET}  ${hash}"
    echo -e "${BOLD}│${RESET} ${MAGENTA}Git Tag:${RESET}     ${tag}"
    echo -e "${BOLD}│${RESET} "
    echo -e "${BOLD}│${RESET} ${YELLOW}Timestamp:${RESET}   ${timestamp}"
    echo -e "${BOLD}│${RESET} "
    echo -e "${BOLD}└───────────────────────────────────────────────────────────┘${RESET}"
}

# Update version in all required files
update_version() {
    local new_version=$1
    local current_version=$(get_current_version)
    
    echo -e "${YELLOW}Updating version: ${RESET}${current_version} → ${BOLD}${new_version}${RESET}"
    
    # Update POM file (source of truth)
    xmlstarlet ed -N pom="http://maven.apache.org/POM/4.0.0" \
        -u "/pom:project/pom:version" -v "$new_version" pom.xml > pom.xml.new \
        && mv pom.xml.new pom.xml
    
    echo -e "  ${GREEN}✓${RESET} Updated pom.xml"
    
    # Create a git tag for this version
    echo -e "  ${GREEN}✓${RESET} Version update complete"
}

# Validate semantic version format
validate_version() {
    local version=$1
    if ! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo -e "${RED}Error: Invalid version format. Use semantic versioning (x.y.z)${RESET}"
        exit 1
    fi
}

# Run tests to ensure everything works with new version
run_tests() {
    echo -e "${YELLOW}Running tests to verify version update...${RESET}"
    if mvn test -q > /dev/null; then
        echo -e "  ${GREEN}✓${RESET} Tests passed"
    else
        echo -e "${RED}Error: Tests failed after version update${RESET}"
        exit 1
    fi
}

# Run GitHub Actions workflow locally for minor/major version changes
run_actions_workflow() {
    echo -e "${YELLOW}Running GitHub Actions workflow locally...${RESET}"
    if act -j build --container-architecture linux/amd64 -s GITHUB_TOKEN="$(git config github.token || echo 'dummy-token')" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✓${RESET} GitHub Actions workflow passed"
    else
        echo -e "${RED}Error: GitHub Actions workflow failed${RESET}"
        echo -e "${YELLOW}Please investigate the pipeline issues before proceeding${RESET}"
        exit 1
    fi
}

# Create git tag for the version
tag_version() {
    local version=$1
    local tag_exists=$(git tag -l "v$version")
    
    if [ -z "$tag_exists" ]; then
        echo -e "${YELLOW}Creating git tag: ${RESET}v${version}"
        git tag -a "v$version" -m "Version $version"
        echo -e "  ${GREEN}✓${RESET} Created git tag v$version"
    else
        echo -e "${YELLOW}Warning: Git tag v$version already exists${RESET}"
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

# Main function
main() {
    display_header
    
    local command=${1:-help}
    local current_version=$(get_current_version)
    
    case $command in
        get)
            echo -e "${BOLD}Current version:${RESET} $current_version"
            echo -e "${BOLD}Build number:${RESET}   $(get_build_number)"
            echo -e "${BOLD}Full ID:${RESET}        $current_version.$(get_build_number)"
            ;;
        summary)
            display_summary
            ;;
        patch)
            local new_version=$(increment_version "$current_version" "patch")
            update_version "$new_version"
            run_tests
            tag_version "$new_version"
            # Display summary after update
            echo -e "\n${BOLD}Updated Version Summary:${RESET}"
            display_summary
            ;;
        minor)
            local new_version=$(increment_version "$current_version" "minor")
            update_version "$new_version"
            run_tests
            run_actions_workflow
            tag_version "$new_version"
            # Display summary after update
            echo -e "\n${BOLD}Updated Version Summary:${RESET}"
            display_summary
            ;;
        major)
            local new_version=$(increment_version "$current_version" "major")
            update_version "$new_version"
            run_tests
            run_actions_workflow
            tag_version "$new_version"
            # Display summary after update
            echo -e "\n${BOLD}Updated Version Summary:${RESET}"
            display_summary
            ;;
        set)
            local new_version=$2
            if [ -z "$new_version" ]; then
                echo -e "${RED}Error: Missing version argument${RESET}"
                display_usage
                exit 1
            fi
            validate_version "$new_version"
            update_version "$new_version"
            run_tests
            tag_version "$new_version"
            # Display summary after update
            echo -e "\n${BOLD}Updated Version Summary:${RESET}"
            display_summary
            ;;
        help|*)
            display_usage
            ;;
    esac
}

# Execute main function
main "$@"