# Conventional Commits Guide

## Overview

This project uses the [Conventional Commits](https://www.conventionalcommits.org/) specification for commit messages. This enables automated changelog generation and semantic versioning.

## Commit Message Format

Each commit message consists of a **header**, an optional **body**, and an optional **footer**.

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Header

The header is mandatory and consists of:

- **type**: Describes the kind of change (see below)
- **scope**: Optional, describes what part of the codebase the commit affects
- **subject**: A short description of the change

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Changes that do not affect the meaning of the code (formatting, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **perf**: Code changes that improve performance
- **test**: Adding or correcting tests
- **build**: Changes to the build system or dependencies
- **ci**: Changes to CI configuration files and scripts
- **chore**: Other changes that don't modify src or test files

### Examples

```
feat(converter): add support for Kelvin temperature scale
```

```
fix(parser): handle negative temperatures correctly
```

```
docs(readme): update installation instructions
```

```
refactor(core): improve temperature conversion algorithm
```

## Breaking Changes

Breaking changes should be indicated by adding a `!` after the type/scope:

```
feat(api)!: change the parameters for temperature conversion
```

And include a `BREAKING CHANGE:` footer:

```
BREAKING CHANGE: The API now requires temperature unit to be specified explicitly
```

## Automated Changelog

Following these conventions allows our CI system to automatically generate meaningful changelogs by categorizing changes based on commit types:

- **Added**: `feat` commits
- **Fixed**: `fix` commits
- **Changed**: `refactor`, `perf`, and other commits
- **Documentation**: `docs` commits
- **Security**: `security` commits
