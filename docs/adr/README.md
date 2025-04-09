# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for the FTOC project.

## What are ADRs?

Architecture Decision Records are documents that capture important architectural decisions made along with their context and consequences.

## Why ADRs?

We use ADRs to:

- Document the architectural decisions in our project
- Provide context about why decisions were made
- Help onboard new team members
- Track the evolution of the architecture over time

## ADR Template

Each ADR follows this format:

```
# ADR NNNN: Title

## Status

[Proposed | Accepted | Deprecated | Superseded]

## Context

[What is the issue that we're seeing that is motivating this decision or change]

## Decision

[What is the change that we're proposing and/or doing]

## Consequences

[What becomes easier or more difficult to do because of this change]
```

## Current ADRs

1. [ADR-0001: Initial Architecture](0001-initial-architecture.md)
2. [ADR-0002: Feature File Parsing Approach](0002-feature-file-parsing.md)
3. [ADR-0003: Output Format Strategy](0003-output-formats.md)
4. [ADR-0004: Tag Analysis Approach](0004-tag-analysis.md)
5. [ADR-0005: Test-Driven Development Approach](0005-test-driven-development.md)

## Creating New ADRs

When you need to create a new ADR:

1. Copy the template
2. Name it with the next sequential number
3. Fill in the sections
4. Add it to the list above
5. Submit a PR for review