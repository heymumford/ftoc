# Contributing

Thanks for taking the time to contribute.

## Prerequisites

- Java 11+
- Maven 3.8+

## Building

```bash
mvn clean package
```

This compiles the source, runs all tests, and produces the executable JAR in `target/`.

## Running Tests

```bash
mvn test
```

For a full verification pass including integration checks:

```bash
mvn verify
```

## Code Style

The project enforces Checkstyle rules during the build. Fix any violations before submitting:

```bash
mvn checkstyle:check
```

## Pull Requests

1. Fork the repository and create a feature branch from `main`.
2. Keep PRs focused and small when possible.
3. Include context in the PR description: what changed and why.
4. Add or update tests when behavior changes.
5. Run `mvn verify` locally and confirm a clean build before opening the PR.
6. CI must pass before merge.

## Reporting Issues

- Search existing issues and discussions before opening a new one.
- If you plan a large change, open an issue first to align on direction.

## License

By contributing, you agree that your contributions will be licensed under this repository's MIT license.
